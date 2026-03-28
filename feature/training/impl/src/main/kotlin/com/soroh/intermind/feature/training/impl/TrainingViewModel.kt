package com.soroh.intermind.feature.training.impl

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.model.ModeStat
import com.soroh.intermind.core.data.model.ObjectiveResult
import com.soroh.intermind.core.data.model.SessionStatistics
import com.soroh.intermind.core.data.model.TrainingItem
import com.soroh.intermind.core.data.model.UserCardProgress
import com.soroh.intermind.core.data.repository.TrainingRepository
import com.soroh.intermind.core.domain.entity.TestType
import com.soroh.intermind.core.domain.entity.TrainingCard
import com.soroh.intermind.feature.training.api.navigation.TrainingNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TrainingViewModel.Factory::class)
class TrainingViewModel @AssistedInject constructor(
    private val trainingRepository: TrainingRepository,
    @Assisted private val key: TrainingNavKey,
) : ViewModel() {

    private val _state = MutableStateFlow<TrainingScreenState>(TrainingScreenState.Initial)
    val state: StateFlow<TrainingScreenState> = _state.asStateFlow()

    private var session: TrainingSession? = null
    private val preloadedPictures = mutableMapOf<String, Uri?>()
    private val dailyLimit = 20

    init {
        startTraining()
    }

    fun startTraining() {
        viewModelScope.launch {
            _state.value = TrainingScreenState.Loading

            trainingRepository.prepareTrainingCards(
                deckId = key.deckId,
                dailyLimit = dailyLimit
            ).onSuccess { items ->

                if (items.isEmpty()) {
                    _state.value = TrainingScreenState.Empty(key.deckId)
                    return@launch
                }

                session = TrainingSession(
                    queue = ArrayDeque(items),
                    total = items.size,
                    startTime = now()
                )

                preloadPictures(items)
                showNextCard()

            }.onFailure { error ->
                _state.value = TrainingScreenState.Error(
                    error.message ?: "Ошибка загрузки тренировочных карточек"
                )
            }
        }
    }

    private fun preloadPictures(items: List<TrainingItem>) {
        items.forEach { item ->
            item.trainingCard.attachment?.let {
                preloadedPictures[item.trainingCard.id] = it.toUri()
            }
        }
    }

    private fun showNextCard() {
        val s = session ?: return

        if (s.queue.isEmpty()) {
            finishTraining()
            return
        }

        val item = s.queue.removeFirst()

        session = s.copy(
            current = item,
            index = s.index + 1,
            cardStartTime = now()
        )

        emitCard(item)
    }

    private fun emitCard(item: TrainingItem) {
        val s = session!!

        _state.value = TrainingScreenState.InProgress(
            currentCard = item.trainingCard,
            currentProgress = item.progress,
            cardNumber = s.index,
            totalCards = s.total,
            isAnswerRevealed = false,
            isCorrect = null,
            selectedAnswer = null,
            preloadedPicture = preloadedPictures[item.trainingCard.id]
        )
    }

    fun submitAnswer(answer: String?) {
        val s = session ?: return
        val item = s.current ?: return
        val state = _state.value as? TrainingScreenState.InProgress ?: return

        if (state.isAnswerRevealed) return

        val attempts = (s.attempts[item.trainingCard.id] ?: 0) + 1

        when (item.trainingCard.testType) {
            TestType.INPUT -> processTextAnswer(item, answer.orEmpty(), attempts)
            else -> processSimpleAnswer(item, answer, attempts)
        }
    }

    private fun processSimpleAnswer(
        item: TrainingItem,
        answer: String?,
        attempts: Int
    ) {
        val isCorrect = checkAnswer(item.trainingCard, answer)
        val accuracy = if (isCorrect) 1.0 / attempts else 0.0

        finalizeAnswer(item, isCorrect, accuracy, attempts, answer)
    }

    private fun processTextAnswer(
        item: TrainingItem,
        text: String,
        attempts: Int
    ) {
        viewModelScope.launch {
            val similarity = trainingRepository.checkFillInTheBlankAnswer(
                userInput = text,
                correctWords = item.trainingCard.missingWords
            )

            val isCorrect = similarity >= 0.80

            finalizeAnswer(item, isCorrect, similarity, attempts, text)
        }
    }

    private fun finalizeAnswer(
        item: TrainingItem,
        isCorrect: Boolean,
        accuracy: Double,
        attempts: Int,
        selectedAnswer: String?
    ) {
        val s = session!!
        val responseTime = now() - s.cardStartTime
        val currentState = _state.value as? TrainingScreenState.InProgress

        val updatedQueue = ArrayDeque(s.queue)
        if (!isCorrect) {
            updatedQueue.addLast(item)
        }

        session = s.copy(
            queue = updatedQueue,
            correct = s.correct + if (isCorrect) 1 else 0,
            mistakes = s.mistakes + if (!isCorrect) 1 else 0,
            attempts = s.attempts + (item.trainingCard.id to attempts),
            modeStats = updateModeStats(s.modeStats, item.trainingCard.testType, isCorrect)
        )

        _state.value = TrainingScreenState.InProgress(
            currentCard = item.trainingCard,
            currentProgress = item.progress,
            cardNumber = s.index,
            totalCards = s.total,
            isAnswerRevealed = true,
            isCorrect = isCorrect,
            selectedAnswer = selectedAnswer,
            preloadedPicture = currentState?.preloadedPicture
        )

        if (isCorrect) {
            viewModelScope.launch {
                trainingRepository.updateCardProgress(
                    deckId = key.deckId,
                    currentProgress = item.progress,
                    result = ObjectiveResult(
                        accuracy = accuracy,
                        responseTimeMs = responseTime,
                        testType = item.trainingCard.testType,
                        attempts = attempts
                    )
                ).getOrThrow()
            }
        }
    }

    private fun updateModeStats(
        stats: Map<TestType, ModeStat>,
        type: TestType,
        correct: Boolean
    ): Map<TestType, ModeStat> {
        val current = stats[type] ?: ModeStat(0, 0)

        return stats + (type to ModeStat(
            correct = current.correct + if (correct) 1 else 0,
            total = current.total + 1
        ))
    }

    fun next() {
        showNextCard()
    }

    fun finishTraining() {
        val s = session ?: return

        val duration = ((now() - s.startTime) / 1000).toInt()

        val attemptsMap = s.attempts
        val totalCards = s.total
        val correctFirstTry = attemptsMap.count { it.value == 1 }
        val mistakesCount = totalCards - correctFirstTry

        viewModelScope.launch {
            trainingRepository.saveSessionResult(
                SessionStatistics(
                    deckId = key.deckId,
                    durationSec = duration,
                    totalCards = totalCards ,
                    correctCount = correctFirstTry,
                    modesStat = s.modeStats.mapKeys { it.key.name }
                )
            )
        }


        _state.value = TrainingScreenState.Finished(
            totalCards = totalCards,
            correctCount = correctFirstTry,
            mistakesCount = mistakesCount,
            durationSec = duration,
            deckId = key.deckId
        )
    }

    private fun now() = System.currentTimeMillis()

    private fun checkAnswer(card: TrainingCard, answer: String?): Boolean {
        return when (card.testType) {
            TestType.TRUE_FALSE -> {
                val displayed = card.displayedAnswer ?: ""
                if (answer.isNullOrEmpty()) {
                    displayed != card.answer
                } else {
                    displayed == card.answer
                }
            }

            else -> answer == card.answer
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(key: TrainingNavKey): TrainingViewModel
    }
}

sealed interface TrainingScreenState {

    data object Initial : TrainingScreenState

    data object Loading : TrainingScreenState

    data class Empty(val deckId: String) : TrainingScreenState

    data class InProgress(
        val currentCard: TrainingCard,
        val currentProgress: UserCardProgress,
        val cardNumber: Int,
        val totalCards: Int,
        val isAnswerRevealed: Boolean = false,
        val selectedAnswer: String? = null,
        val isCorrect: Boolean? = null,
        val preloadedPicture: Uri? = null,
    ) : TrainingScreenState

    data class Finished(
        val totalCards: Int,
        val correctCount: Int,
        val mistakesCount: Int,
        val durationSec: Int,
        val deckId: String,
    ) : TrainingScreenState

    data class Error(val message: String) : TrainingScreenState
}

data class TrainingSession(
    val queue: ArrayDeque<TrainingItem>,
    val current: TrainingItem? = null,
    val total: Int = 0,
    val index: Int = 0,
    val correct: Int = 0,
    val mistakes: Int = 0,
    val startTime: Long = 0L,
    val cardStartTime: Long = 0L,
    val modeStats: Map<TestType, ModeStat> = emptyMap(),
    val attempts: Map<String, Int> = emptyMap()
)
