package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.model.CardPhase
import com.soroh.intermind.core.data.model.ObjectiveResult
import com.soroh.intermind.core.data.model.Rating
import com.soroh.intermind.core.data.model.SessionStatistics
import com.soroh.intermind.core.data.model.UserCardProgress
import com.soroh.intermind.core.data.util.FSRS
import com.soroh.intermind.core.data.util.generatePartialAnswer
import com.soroh.intermind.core.data.util.levenshteinDistance
import com.soroh.intermind.core.data.util.normalizeText
import com.soroh.intermind.core.domain.entity.Card
import com.soroh.intermind.core.domain.entity.TestType
import com.soroh.intermind.core.domain.entity.TrainingCard
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.max

/**
 * Implements a [TrainingRepository]
 */
class TrainingRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : TrainingRepository {

    private val userCardProgressTable
        get() = supabase.postgrest["user_card_progress"]
    private val cardsTable
        get() = supabase.postgrest["cards"]
    private val trainingSessionsTable
        get() = supabase.postgrest["training_sessions"]

    val fsrs = FSRS()

    override suspend fun prepareTrainingCards(
        deckId: String,
        dailyLimit: Int,
        modes: Set<TestType>
    ): Result<List<TrainingItem>> {
        return runCatching {
            require(modes.isNotEmpty()) { "At least one TestType must be selected" }

            val progressQueue = getTrainingQueue(deckId, dailyLimit).getOrThrow()
            if (progressQueue.isEmpty()) return@runCatching emptyList()

            val queueCardIds = progressQueue.map { it.cardId }

            // Загружаем полные данные только для тех карточек, которые попали в очередь
            val queueCards = getCardsByIds(queueCardIds)

            // Загружаем все ответы этой колоды для дистракторов (неправильных вариантов)
            val allDeckAnswers = cardsTable
                .select(columns = Columns.list("id", "answer")) {
                    filter { eq("deck_id", deckId) }
                }.decodeList<CardAnswerDto>()

            val trainingItems = progressQueue.mapNotNull { progress ->
                val card = queueCards.find { it.id == progress.cardId } ?: return@mapNotNull null

                val randomMode = modes.random()

                val trainingCard = createTrainingCard(card, randomMode, allDeckAnswers)

                TrainingItem(
                    trainingCard = trainingCard,
                    progress = progress
                )
            }

            trainingItems.shuffled()
        }
    }

    private fun createTrainingCard(
        card: Card,
        mode: TestType,
        allAnswers: List<CardAnswerDto>
    ): TrainingCard {
        return when (mode) {
            TestType.CHOICE -> {
                val wrongAnswers = generateWrongAnswers(
                    currentCard = card,
                    allAnswers = allAnswers,
                    count = 3
                )
                TrainingCard(
                    id = card.id,
                    testType = mode,
                    question = card.question,
                    answer = card.answer,
                    attachment = card.attachment,
                    wrongAnswers = wrongAnswers
                )
            }

            TestType.INPUT -> {
                val (partialAnswer, missingWords, startIndex) = generatePartialAnswer(card.answer)
                TrainingCard(
                    id = card.id,
                    testType = mode,
                    question = card.question,
                    answer = card.answer,
                    partialAnswer = partialAnswer,
                    missingWords = missingWords,
                    missingWordStartIndex = startIndex,
                    attachment = card.attachment,
                )
            }

            TestType.TRUE_FALSE -> {
                val isCorrect = (0..1).random() == 1
                val displayedAnswer = if (isCorrect) {
                    card.answer
                } else {
                    generateWrongAnswers(
                        currentCard = card,
                        allAnswers = allAnswers,
                        count = 1
                    ).firstOrNull() ?: card.answer
                }
                TrainingCard(
                    id = card.id,
                    testType = mode,
                    question = card.question,
                    answer = card.answer,
                    displayedAnswer = displayedAnswer,
                    attachment = card.attachment,
                )
            }
        }
    }

    override suspend fun checkFillInTheBlankAnswer(
        userInput: String,
        correctWords: List<String>
    ): Double {
        val normalizedInput = normalizeText(userInput)
        val normalizedCorrect = normalizeText(correctWords.joinToString(" "))

        if (normalizedInput == normalizedCorrect) return 1.0

        val dist = levenshteinDistance(normalizedInput, normalizedCorrect)
        val maxLength = max(normalizedInput.length, normalizedCorrect.length)

        // Вычисляем процент сходства
        val similarity = 1.0 - (dist.toDouble() / maxLength.toDouble())

       return similarity
    }

    private fun generateWrongAnswers(
        currentCard: Card,
        allAnswers: List<CardAnswerDto>,
        count: Int
    ): List<String> {
        if (currentCard.wrongAnswers.isNotEmpty()) {
            return currentCard.wrongAnswers
        }

        return allAnswers
            .filter { it.id != currentCard.id && it.answer.lowercase() != currentCard.answer.lowercase() }
            .map { it.answer }
            .distinct()
            .shuffled()
            .take(count)
    }

    private suspend fun getTrainingQueue(
        deckId: String,
        dailyLimit: Int
    ): Result<List<UserCardProgress>> {
        return runCatching {
            val userId =
                getCurrentUserId() ?: throw IllegalStateException("User is not authenticated")

            val response = supabase.postgrest.rpc(
                function = "get_training_queue",
                parameters = buildJsonObject {
                    put("p_deck_id", deckId)
                    put("p_user_id", userId)
                    put("p_new_limit", dailyLimit)
                }
            ).decodeList<UserCardProgressDto>()
                .map { it.toDomain() }
            response
        }
    }

    override suspend fun processCardAnswer(
        currentProgress: UserCardProgress,
        result: ObjectiveResult
    ): Result<Unit> {
        return runCatching {
            // Вычисляем новые параметры FSRS на основе объективного результата
            val nextGrade = fsrs.calculateNextState(currentProgress, result)

            // Формируем обновленный прогресс
            val updatedProgress = currentProgress.copy(
                stability = nextGrade.stability,
                difficulty = nextGrade.difficulty,
                interval = nextGrade.interval,
                dueDate = addMillisToNow(nextGrade.durationMillis),
                lastReview = LocalDateTime.now(),
                reviewCount = currentProgress.reviewCount + 1,
                phase = if (nextGrade.choice == Rating.Again) CardPhase.ReLearning.value else CardPhase.Review.value
            )

            val dto = UserCardProgressDto.fromDomain(updatedProgress)
            userCardProgressTable.upsert(dto) {
                onConflict = "user_id,card_id"
            }
        }
    }

    override suspend fun saveSessionResult(stats: SessionStatistics): Result<Unit> {
        return runCatching {
            val userId = getCurrentUserId() ?: throw IllegalStateException()

            val finalStats = stats.copy(userId = userId)

            trainingSessionsTable.insert(finalStats)
        }
    }

    private suspend fun getCardsByIds(cardIds: List<String>): List<Card> {
        if (cardIds.isEmpty()) return emptyList()

        val dtos = cardsTable.select {
            filter {
                CardDto::id.isIn(cardIds)
            }
        }.decodeList<CardDto>()

        return dtos.map { dto ->
            val publicUrl = dto.picturePath?.let { path ->
                supabase.storage["cards_pics"].publicUrl(path)
            }
            dto.toDomain(attachment = publicUrl)
        }
    }

    private suspend fun getCurrentUserId(): String? {
        return supabase.auth.sessionManager.loadSession()?.user?.id
    }

    private fun addMillisToNow(millis: Long): LocalDateTime {
        return LocalDateTime.now().plus(millis, java.time.temporal.ChronoUnit.MILLIS)
    }
}

data class TrainingItem(
    val trainingCard: TrainingCard,
    val progress: UserCardProgress
)

@Serializable
data class UserCardProgressDto(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("card_id") val cardId: String,
    @SerialName("stability") val stability: Double = 2.5,
    @SerialName("difficulty") val difficulty: Double = 2.5,
    @SerialName("interval") val interval: Int = 0,
    @SerialName("due_date") val dueDate: String,
    @SerialName("review_count") val reviewCount: Int = 0,
    @SerialName("last_review") val lastReview: String,
    @SerialName("phase") val phase: Int = 0
) {
    fun toDomain() = UserCardProgress(
        id = id,
        userId = userId,
        cardId = cardId,
        stability = stability,
        difficulty = difficulty,
        interval = interval,
        dueDate = OffsetDateTime.parse(dueDate).toLocalDateTime(),
        reviewCount = reviewCount,
        lastReview = OffsetDateTime.parse(lastReview).toLocalDateTime(),
        phase = phase
    )

    companion object {
        fun fromDomain(progress: UserCardProgress) = UserCardProgressDto(
            id = progress.id,
            userId = progress.userId,
            cardId = progress.cardId,
            stability = progress.stability,
            difficulty = progress.difficulty,
            interval = progress.interval,
            dueDate = progress.dueDate.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString(),
            reviewCount = progress.reviewCount,
            lastReview = progress.lastReview.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString(),
            phase = progress.phase
        )
    }
}

@Serializable
data class CardAnswerDto(
    @SerialName("id") val id: String,
    @SerialName("answer") val answer: String
)