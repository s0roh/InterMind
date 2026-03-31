package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.dto.statistic.AggregatedModeStatistic
import com.soroh.intermind.core.data.dto.statistic.CardDueDateDto
import com.soroh.intermind.core.data.dto.statistic.CardPhase
import com.soroh.intermind.core.data.dto.statistic.CardPhaseStat
import com.soroh.intermind.core.data.dto.statistic.CardWithProgressDto
import com.soroh.intermind.core.data.dto.statistic.ForecastStat
import com.soroh.intermind.core.data.dto.statistic.ModeStatDto
import com.soroh.intermind.core.data.dto.statistic.TrainingSessionDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.collections.emptyList

class StatisticsRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : StatisticsRepository {

    private val trainingSessionsTable
        get() = supabase.postgrest["training_sessions"]
    private val cardsTable
        get() = supabase.postgrest["cards"]

    override suspend fun getModesStatisticsForDeck(deckId: String): List<AggregatedModeStatistic> {
        return withContext(Dispatchers.IO) {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return@withContext emptyList()

            val sessions = trainingSessionsTable.select(
                columns = Columns.list(
                    "id",
                    "deck_id",
                    "modes_stat"
                )
            ) {
                filter {
                    eq("user_id", userId)
                    eq("deck_id", deckId)
                }

            }.decodeList<TrainingSessionDto>()

            val aggregatedMap = mutableMapOf<String, ModeStatDto>()

            sessions.forEach { session ->
                session.modesStat.forEach { (mode, stat) ->
                    val current = aggregatedMap[mode] ?: ModeStatDto(0, 0)
                    aggregatedMap[mode] = ModeStatDto(
                        total = current.total + stat.total,
                        correct = current.correct + stat.correct
                    )
                }
            }

            aggregatedMap.map { (mode, stat) ->
                AggregatedModeStatistic(
                    modeName = mode,
                    totalCards = stat.total,
                    correctAnswers = stat.correct,
                    incorrectAnswers = stat.incorrect
                )
            }.sortedByDescending { it.totalCards }
        }
    }

    override suspend fun getCardPhasesStats(deckId: String): List<CardPhaseStat> {
        return withContext(Dispatchers.IO) {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext emptyList()

            // Выбираем только колонку phase для всех карт пользователя в этой колоде
            val response = cardsTable.select(
                columns = Columns.list("id", "user_card_progress(phase)")
            ) {
                filter {
                    eq("deck_id", deckId)
                    // Важно: фильтруем вложенный прогресс по нашему userId
                    eq("user_card_progress.user_id", userId)
                }
            }.decodeList<CardWithProgressDto>()

// 2. Группируем результаты
            // Если user_card_progress пустой (null), значит карточка новая (phase 0)
            val phases = response.map { card ->
                card.progress?.firstOrNull()?.phase ?: 0
            }

            val counts = phases.groupingBy { it }.eachCount()

            listOf(
                CardPhaseStat(CardPhase.NEW, counts[0] ?: 0),
                CardPhaseStat(CardPhase.LEARNING, counts[1] ?: 0),
                // Все фазы >= 2 считаются GRADUATED
                CardPhaseStat(CardPhase.GRADUATED, counts.filterKeys { it >= 2 }.values.sum())
            )
        }
    }

    override suspend fun getFutureDueForecast(deckId: String): List<ForecastStat> {
        return withContext(Dispatchers.IO) {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext emptyList()

            val response = cardsTable.select(
                columns = Columns.list("user_card_progress(due_date)")
            ) {
                filter {
                    eq("deck_id", deckId)
                    eq("user_card_progress.user_id", userId)
                }
            }.decodeList<CardDueDateDto>()

            // 2. Парсим и фильтруем только будущие даты
            val now = LocalDate.now()
            val dueDates = response.flatMap { it.progress ?: emptyList() }
                .map { OffsetDateTime.parse(it.dueDate).toLocalDate() }
                .filter { !it.isBefore(now) } // Только сегодня и будущее

            // 3. Создаем сетку на 10 дней вперед, чтобы на графике не было дырок
            val formatter = DateTimeFormatter.ofPattern("dd.MM")
            (0..10).map { dayOffset ->
                val targetDate = now.plusDays(dayOffset.toLong())
                ForecastStat(
                    date = targetDate.format(formatter),
                    count = dueDates.count { it == targetDate }
                )
            }
        }
    }
}