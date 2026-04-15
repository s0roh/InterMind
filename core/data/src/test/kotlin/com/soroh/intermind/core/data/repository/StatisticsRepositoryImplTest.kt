package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.dto.statistic.AggregatedModeStatistic
import com.soroh.intermind.core.data.dto.statistic.CardDueDateDto
import com.soroh.intermind.core.data.dto.statistic.CardPhase
import com.soroh.intermind.core.data.dto.statistic.CardPhaseStat
import com.soroh.intermind.core.data.dto.statistic.CardWithProgressDto
import com.soroh.intermind.core.data.dto.statistic.DueDateDto
import com.soroh.intermind.core.data.dto.statistic.ForecastStat
import com.soroh.intermind.core.data.dto.statistic.ModeStatDto
import com.soroh.intermind.core.data.dto.statistic.ProgressPhaseDto
import com.soroh.intermind.core.data.dto.statistic.TrainingSessionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatisticsRepositoryImplTest {
    @Test
    fun `TrainingSessionDto creates correctly`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            modesStat = mapOf(
                "CHOICE" to ModeStatDto(total = 10, correct = 8),
                "INPUT" to ModeStatDto(total = 5, correct = 3)
            )
        )

        assertEquals("session-1", dto.id)
        assertEquals("deck-1", dto.deckId)
        assertEquals(2, dto.modesStat.size)
        assertEquals(10, dto.modesStat["CHOICE"]?.total)
        assertEquals(8, dto.modesStat["CHOICE"]?.correct)
    }

    @Test
    fun `TrainingSessionDto handles empty modes stat`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1"
        )

        assertTrue(dto.modesStat.isEmpty())
    }

    @Test
    fun `ModeStatDto calculates incorrect correctly`() {
        val dto = ModeStatDto(total = 10, correct = 7)

        assertEquals(10, dto.total)
        assertEquals(7, dto.correct)
        assertEquals(3, dto.incorrect)
    }

    @Test
    fun `ModeStatDto handles all correct`() {
        val dto = ModeStatDto(total = 5, correct = 5)

        assertEquals(0, dto.incorrect)
    }

    @Test
    fun `ModeStatDto handles zero correct`() {
        val dto = ModeStatDto(total = 5, correct = 0)

        assertEquals(5, dto.incorrect)
    }

    @Test
    fun `ModeStatDto handles zero total`() {
        val dto = ModeStatDto(total = 0, correct = 0)

        assertEquals(0, dto.total)
        assertEquals(0, dto.correct)
        assertEquals(0, dto.incorrect)
    }

    @Test
    fun `ModeStatDto default values are correct`() {
        val dto = ModeStatDto()

        assertEquals(0, dto.total)
        assertEquals(0, dto.correct)
        assertEquals(0, dto.incorrect)
    }

    @Test
    fun `AggregatedModeStatistic creates correctly`() {
        val stat = AggregatedModeStatistic(
            modeName = "CHOICE",
            totalCards = 20,
            correctAnswers = 15,
            incorrectAnswers = 5
        )

        assertEquals("CHOICE", stat.modeName)
        assertEquals(20, stat.totalCards)
        assertEquals(15, stat.correctAnswers)
        assertEquals(5, stat.incorrectAnswers)
    }

    @Test
    fun `aggregateModesStats combines multiple sessions correctly`() {
        val sessions = listOf(
            TrainingSessionDto(
                id = "session-1",
                deckId = "deck-1",
                modesStat = mapOf(
                    "CHOICE" to ModeStatDto(total = 10, correct = 8),
                    "INPUT" to ModeStatDto(total = 5, correct = 3)
                )
            ),
            TrainingSessionDto(
                id = "session-2",
                deckId = "deck-1",
                modesStat = mapOf(
                    "CHOICE" to ModeStatDto(total = 15, correct = 12),
                    "INPUT" to ModeStatDto(total = 8, correct = 5)
                )
            )
        )

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

        val result = aggregatedMap.map { (mode, stat) ->
            AggregatedModeStatistic(
                modeName = mode,
                totalCards = stat.total,
                correctAnswers = stat.correct,
                incorrectAnswers = stat.incorrect
            )
        }.sortedByDescending { it.totalCards }

        assertEquals(2, result.size)
        assertEquals("CHOICE", result[0].modeName)
        assertEquals(25, result[0].totalCards)
        assertEquals(20, result[0].correctAnswers)
        assertEquals(5, result[0].incorrectAnswers)
        assertEquals("INPUT", result[1].modeName)
        assertEquals(13, result[1].totalCards)
        assertEquals(8, result[1].correctAnswers)
        assertEquals(5, result[1].incorrectAnswers)
    }

    @Test
    fun `aggregateModesStats handles single session`() {
        val sessions = listOf(
            TrainingSessionDto(
                id = "session-1",
                deckId = "deck-1",
                modesStat = mapOf(
                    "CHOICE" to ModeStatDto(total = 10, correct = 7)
                )
            )
        )

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

        val result = aggregatedMap.map { (mode, stat) ->
            AggregatedModeStatistic(
                modeName = mode,
                totalCards = stat.total,
                correctAnswers = stat.correct,
                incorrectAnswers = stat.incorrect
            )
        }

        assertEquals(1, result.size)
        assertEquals(10, result[0].totalCards)
        assertEquals(7, result[0].correctAnswers)
    }

    @Test
    fun `aggregateModesStats handles empty sessions`() {
        val sessions = emptyList<TrainingSessionDto>()

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

        val result = aggregatedMap.map { (mode, stat) ->
            AggregatedModeStatistic(
                modeName = mode,
                totalCards = stat.total,
                correctAnswers = stat.correct,
                incorrectAnswers = stat.incorrect
            )
        }

        assertTrue(result.isEmpty())
    }

    @Test
    fun `aggregateModesStats handles sessions with different modes`() {
        val sessions = listOf(
            TrainingSessionDto(
                id = "session-1",
                deckId = "deck-1",
                modesStat = mapOf(
                    "CHOICE" to ModeStatDto(total = 10, correct = 8)
                )
            ),
            TrainingSessionDto(
                id = "session-2",
                deckId = "deck-1",
                modesStat = mapOf(
                    "INPUT" to ModeStatDto(total = 5, correct = 3)
                )
            )
        )

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

        val result = aggregatedMap.map { (mode, stat) ->
            AggregatedModeStatistic(
                modeName = mode,
                totalCards = stat.total,
                correctAnswers = stat.correct,
                incorrectAnswers = stat.incorrect
            )
        }.sortedByDescending { it.totalCards }

        assertEquals(2, result.size)
        assertEquals("CHOICE", result[0].modeName)
        assertEquals("INPUT", result[1].modeName)
    }

    @Test
    fun `CardPhaseStat creates correctly`() {
        val stat = CardPhaseStat(
            phase = CardPhase.NEW,
            count = 15
        )

        assertEquals(CardPhase.NEW, stat.phase)
        assertEquals(15, stat.count)
    }

    @Test
    fun `CardPhaseStat handles zero count`() {
        val stat = CardPhaseStat(
            phase = CardPhase.GRADUATED,
            count = 0
        )

        assertEquals(CardPhase.GRADUATED, stat.phase)
        assertEquals(0, stat.count)
    }

    @Test
    fun `CardPhase enum has correct values`() {
        assertEquals(3, CardPhase.entries.size)
        assertEquals(CardPhase.NEW, CardPhase.valueOf("NEW"))
        assertEquals(CardPhase.LEARNING, CardPhase.valueOf("LEARNING"))
        assertEquals(CardPhase.GRADUATED, CardPhase.valueOf("GRADUATED"))
    }

    @Test
    fun `CardWithProgressDto creates correctly`() {
        val dto = CardWithProgressDto(
            id = "card-1",
            progress = listOf(
                ProgressPhaseDto(phase = 2)
            )
        )

        assertEquals("card-1", dto.id)
        assertEquals(1, dto.progress?.size)
        assertEquals(2, dto.progress?.firstOrNull()?.phase)
    }

    @Test
    fun `CardWithProgressDto handles null progress`() {
        val dto = CardWithProgressDto(
            id = "card-1",
            progress = null
        )

        assertEquals("card-1", dto.id)
        assertNull(dto.progress)
    }

    @Test
    fun `CardWithProgressDto handles empty progress`() {
        val dto = CardWithProgressDto(
            id = "card-1",
            progress = emptyList()
        )

        assertTrue(dto.progress?.isEmpty() == true)
    }

    @Test
    fun `ProgressPhaseDto creates correctly`() {
        val dto = ProgressPhaseDto(phase = 2)

        assertEquals(2, dto.phase)
    }

    @Test
    fun `calculateCardPhaseStats groups phases correctly`() {
        val response = listOf(
            CardWithProgressDto(id = "card-1", progress = listOf(ProgressPhaseDto(phase = 0))),
            CardWithProgressDto(id = "card-2", progress = listOf(ProgressPhaseDto(phase = 0))),
            CardWithProgressDto(id = "card-3", progress = listOf(ProgressPhaseDto(phase = 1))),
            CardWithProgressDto(id = "card-4", progress = listOf(ProgressPhaseDto(phase = 2))),
            CardWithProgressDto(id = "card-5", progress = listOf(ProgressPhaseDto(phase = 2))),
            CardWithProgressDto(id = "card-6", progress = listOf(ProgressPhaseDto(phase = 3)))
        )

        val phases = response.map { card ->
            card.progress?.firstOrNull()?.phase ?: 0
        }

        val counts = phases.groupingBy { it }.eachCount()

        val result = listOf(
            CardPhaseStat(CardPhase.NEW, counts[0] ?: 0),
            CardPhaseStat(CardPhase.LEARNING, counts[1] ?: 0),
            CardPhaseStat(CardPhase.GRADUATED, counts.filterKeys { it >= 2 }.values.sum())
        )

        assertEquals(3, result.size)
        assertEquals(2, result[0].count)
        assertEquals(1, result[1].count)
        assertEquals(3, result[2].count)
    }

    @Test
    fun `calculateCardPhaseStats handles empty response`() {
        val response = emptyList<CardWithProgressDto>()

        val phases = response.map { card ->
            card.progress?.firstOrNull()?.phase ?: 0
        }

        val counts = phases.groupingBy { it }.eachCount()

        val result = listOf(
            CardPhaseStat(CardPhase.NEW, counts[0] ?: 0),
            CardPhaseStat(CardPhase.LEARNING, counts[1] ?: 0),
            CardPhaseStat(CardPhase.GRADUATED, counts.filterKeys { it >= 2 }.values.sum())
        )

        assertEquals(0, result[0].count)
        assertEquals(0, result[1].count)
        assertEquals(0, result[2].count)
    }

    @Test
    fun `calculateCardPhaseStats handles cards with null progress`() {
        val response = listOf(
            CardWithProgressDto(id = "card-1", progress = null),
            CardWithProgressDto(id = "card-2", progress = listOf(ProgressPhaseDto(phase = 2)))
        )

        val phases = response.map { card ->
            card.progress?.firstOrNull()?.phase ?: 0
        }

        val counts = phases.groupingBy { it }.eachCount()

        val result = listOf(
            CardPhaseStat(CardPhase.NEW, counts[0] ?: 0),
            CardPhaseStat(CardPhase.LEARNING, counts[1] ?: 0),
            CardPhaseStat(CardPhase.GRADUATED, counts.filterKeys { it >= 2 }.values.sum())
        )

        assertEquals(1, result[0].count)
        assertEquals(0, result[1].count)
        assertEquals(1, result[2].count)
    }

    @Test
    fun `DueDateDto creates correctly`() {
        val dto = DueDateDto(dueDate = "2024-04-15T10:00:00+03:00")

        assertEquals("2024-04-15T10:00:00+03:00", dto.dueDate)
    }

    @Test
    fun `CardDueDateDto creates correctly`() {
        val dto = CardDueDateDto(
            progress = listOf(
                DueDateDto(dueDate = "2024-04-15T10:00:00+03:00"),
                DueDateDto(dueDate = "2024-04-16T12:00:00+03:00")
            )
        )

        assertEquals(2, dto.progress?.size)
    }

    @Test
    fun `CardDueDateDto handles null progress`() {
        val dto = CardDueDateDto(progress = null)

        assertNull(dto.progress)
    }

    @Test
    fun `ForecastStat creates correctly`() {
        val stat = ForecastStat(
            date = "15.04",
            count = 10
        )

        assertEquals("15.04", stat.date)
        assertEquals(10, stat.count)
    }

    @Test
    fun `ForecastStat handles zero count`() {
        val stat = ForecastStat(
            date = "20.04",
            count = 0
        )

        assertEquals(0, stat.count)
    }

    @Test
    fun `generateForecast returns 11 days forecast`() {
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM")

        val forecast = (0..10).map { dayOffset ->
            val targetDate = now.plusDays(dayOffset.toLong())
            ForecastStat(
                date = targetDate.format(formatter),
                count = 0
            )
        }

        assertEquals(11, forecast.size)
        assertEquals(now.format(formatter), forecast[0].date)
        assertEquals(now.plusDays(10).format(formatter), forecast[10].date)
    }

    @Test
    fun `generateForecast formats dates correctly`() {
        val formatter = DateTimeFormatter.ofPattern("dd.MM")
        val testDate = LocalDate.of(2024, 4, 15)

        val formatted = testDate.format(formatter)

        assertEquals("15.04", formatted)
    }

    @Test
    fun `filterDueDates excludes past dates`() {
        val now = LocalDate.now()
        val response = listOf(
            CardDueDateDto(
                progress = listOf(
                    DueDateDto(dueDate = now.minusDays(1).atStartOfDay().toString()),
                    DueDateDto(dueDate = now.atStartOfDay().toString()),
                    DueDateDto(dueDate = now.plusDays(1).atStartOfDay().toString()),
                    DueDateDto(dueDate = now.plusDays(5).atStartOfDay().toString())
                )
            )
        )

        val dueDates = response.flatMap { it.progress ?: emptyList() }
            .map { java.time.LocalDateTime.parse(it.dueDate).toLocalDate() }
            .filter { !it.isBefore(now) }

        assertEquals(3, dueDates.size)
        assertFalse(now.minusDays(1) in dueDates)
        assertTrue(now in dueDates)
        assertTrue(now.plusDays(1) in dueDates)
    }

    @Test
    fun `countDueDatesForDate counts correctly`() {
        val now = LocalDate.now()
        val targetDate = now.plusDays(2)

        val dueDates = listOf(
            now.plusDays(1),
            targetDate,
            targetDate,
            targetDate,
            now.plusDays(5)
        )

        val count = dueDates.count { it == targetDate }

        assertEquals(3, count)
    }

    @Test
    fun `full forecast pipeline works correctly`() {
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM")
        val futureDate1 = now.plusDays(2)
        val futureDate2 = now.plusDays(5)

        val response = listOf(
            CardDueDateDto(
                progress = listOf(
                    DueDateDto(dueDate = futureDate1.atStartOfDay().toString()),
                    DueDateDto(dueDate = futureDate1.atStartOfDay().toString()),
                    DueDateDto(dueDate = futureDate2.atStartOfDay().toString())
                )
            )
        )

        val dueDates = response.flatMap { it.progress ?: emptyList() }
            .map { java.time.LocalDateTime.parse(it.dueDate).toLocalDate() }
            .filter { !it.isBefore(now) }

        val forecast = (0..10).map { dayOffset ->
            val targetDate = now.plusDays(dayOffset.toLong())
            ForecastStat(
                date = targetDate.format(formatter),
                count = dueDates.count { it == targetDate }
            )
        }

        assertEquals(11, forecast.size)
        assertEquals(2, forecast[2].count)
        assertEquals(1, forecast[5].count)
        assertEquals(0, forecast[0].count)
    }

    @Test
    fun `ModeStatDto handles large numbers`() {
        val dto = ModeStatDto(
            total = Int.MAX_VALUE,
            correct = Int.MAX_VALUE - 100
        )

        assertEquals(Int.MAX_VALUE, dto.total)
        assertEquals(100, dto.incorrect)
    }

    @Test
    fun `aggregation handles many modes`() {
        val sessions = (1..10).map { sessionNum ->
            TrainingSessionDto(
                id = "session-$sessionNum",
                deckId = "deck-1",
                modesStat = mapOf(
                    "CHOICE" to ModeStatDto(total = sessionNum, correct = sessionNum - 1),
                    "INPUT" to ModeStatDto(total = sessionNum, correct = sessionNum - 2),
                    "TRUE_FALSE" to ModeStatDto(total = sessionNum, correct = sessionNum)
                )
            )
        }

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

        assertEquals(3, aggregatedMap.size)
        assertEquals(55, aggregatedMap["CHOICE"]?.total)
        assertEquals(45, aggregatedMap["CHOICE"]?.correct)
        assertEquals(55, aggregatedMap["TRUE_FALSE"]?.total)
        assertEquals(55, aggregatedMap["TRUE_FALSE"]?.correct)
    }

    @Test
    fun `sorting aggregated stats by totalCards descending`() {
        val aggregatedMap = mapOf(
            "CHOICE" to ModeStatDto(total = 10, correct = 8),
            "INPUT" to ModeStatDto(total = 25, correct = 20),
            "TRUE_FALSE" to ModeStatDto(total = 15, correct = 12)
        )

        val result = aggregatedMap.map { (mode, stat) ->
            AggregatedModeStatistic(
                modeName = mode,
                totalCards = stat.total,
                correctAnswers = stat.correct,
                incorrectAnswers = stat.incorrect
            )
        }.sortedByDescending { it.totalCards }

        assertEquals("INPUT", result[0].modeName)
        assertEquals("TRUE_FALSE", result[1].modeName)
        assertEquals("CHOICE", result[2].modeName)
    }

    private fun <T> assertNull(value: T?) {
        assertEquals(null, value)
    }
}
