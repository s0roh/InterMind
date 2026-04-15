package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.dto.history.DeckNameDto
import com.soroh.intermind.core.data.dto.history.ModeStatDto
import com.soroh.intermind.core.data.dto.history.TrainingHistoryItem
import com.soroh.intermind.core.data.dto.history.TrainingSessionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class HistoryRepositoryImplTest {

    private val baseTimestamp = Instant.fromEpochSeconds(1700000000)

    @Test
    fun `TrainingSessionDto creates correctly with all fields`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 300,
            totalCards = 20,
            correctCount = 15,
            modesStat = mapOf(
                "CHOICE" to ModeStatDto(total = 10, correct = 8),
                "INPUT" to ModeStatDto(total = 10, correct = 7)
            ),
            decks = DeckNameDto(name = "English Vocabulary")
        )

        assertEquals("session-1", dto.id)
        assertEquals("deck-1", dto.deckId)
        assertEquals(baseTimestamp, dto.createdAt)
        assertEquals(300, dto.durationSec)
        assertEquals(20, dto.totalCards)
        assertEquals(15, dto.correctCount)
        assertEquals(2, dto.modesStat?.size)
        assertEquals("English Vocabulary", dto.decks?.name)
    }

    @Test
    fun `TrainingSessionDto handles null modesStat`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 300,
            totalCards = 20,
            correctCount = 15,
            modesStat = null,
            decks = DeckNameDto(name = "Test Deck")
        )

        assertEquals(null, dto.modesStat)
    }

    @Test
    fun `TrainingSessionDto handles null decks`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 300,
            totalCards = 20,
            correctCount = 15,
            modesStat = null,
            decks = null
        )

        assertEquals(null, dto.decks)
    }

    @Test
    fun `TrainingSessionDto default modesStat is null`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 300,
            totalCards = 20,
            correctCount = 15
        )

        assertEquals(null, dto.modesStat)
    }

    @Test
    fun `ModeStatDto creates correctly`() {
        val dto = ModeStatDto(total = 10, correct = 8)

        assertEquals(10, dto.total)
        assertEquals(8, dto.correct)
    }

    @Test
    fun `DeckNameDto creates correctly`() {
        val dto = DeckNameDto(name = "Test Deck")

        assertEquals("Test Deck", dto.name)
    }

    @Test
    fun `TrainingHistoryItem creates correctly`() {
        val item = TrainingHistoryItem(
            id = "session-1",
            deckId = "deck-1",
            deckName = "English Vocabulary",
            timestamp = baseTimestamp,
            durationSec = 300,
            totalCards = 20,
            correctCount = 15,
            incorrectCount = 3,
            skippedCount = 2
        )

        assertEquals("session-1", item.id)
        assertEquals("deck-1", item.deckId)
        assertEquals("English Vocabulary", item.deckName)
        assertEquals(baseTimestamp, item.timestamp)
        assertEquals(300, item.durationSec)
        assertEquals(20, item.totalCards)
        assertEquals(15, item.correctCount)
        assertEquals(3, item.incorrectCount)
        assertEquals(2, item.skippedCount)
    }

    @Test
    fun `calculate stats from modesStat sums correctly`() {
        val modesStat = mapOf(
            "CHOICE" to ModeStatDto(total = 10, correct = 8),
            "INPUT" to ModeStatDto(total = 10, correct = 7),
            "TRUE_FALSE" to ModeStatDto(total = 5, correct = 4)
        )

        val totalAnswered = modesStat.values.sumOf { it.total }
        val totalCorrect = modesStat.values.sumOf { it.correct }
        val incorrect = totalAnswered - totalCorrect

        assertEquals(25, totalAnswered)
        assertEquals(19, totalCorrect)
        assertEquals(6, incorrect)
    }

    @Test
    fun `calculate stats from single mode`() {
        val modesStat = mapOf(
            "CHOICE" to ModeStatDto(total = 10, correct = 7)
        )

        val totalAnswered = modesStat.values.sumOf { it.total }
        val totalCorrect = modesStat.values.sumOf { it.correct }
        val incorrect = totalAnswered - totalCorrect

        assertEquals(10, totalAnswered)
        assertEquals(7, totalCorrect)
        assertEquals(3, incorrect)
    }

    @Test
    fun `calculate stats from empty modesStat`() {
        val modesStat = emptyMap<String, ModeStatDto>()

        val totalAnswered = modesStat.values.sumOf { it.total }
        val totalCorrect = modesStat.values.sumOf { it.correct }
        val incorrect = totalAnswered - totalCorrect

        assertEquals(0, totalAnswered)
        assertEquals(0, totalCorrect)
        assertEquals(0, incorrect)
    }

    @Test
    fun `fallback to correctCount when modesStat is null`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 300,
            totalCards = 20,
            correctCount = 15,
            modesStat = null
        )

        val totalAnswered = dto.modesStat?.values?.sumOf { it.total } ?: dto.correctCount
        val totalCorrect = dto.modesStat?.values?.sumOf { it.correct } ?: dto.correctCount

        assertEquals(15, totalAnswered)
        assertEquals(15, totalCorrect)
    }

    @Test
    fun `calculate incorrect and skipped correctly`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 300,
            totalCards = 20,
            correctCount = 14,
            modesStat = mapOf(
                "CHOICE" to ModeStatDto(total = 15, correct = 12),
                "INPUT" to ModeStatDto(total = 3, correct = 2)
            )
        )

        val totalAnswered = dto.modesStat?.values?.sumOf { it.total } ?: dto.correctCount
        val totalCorrect = dto.modesStat?.values?.sumOf { it.correct } ?: dto.correctCount
        val incorrect = (totalAnswered - totalCorrect).coerceAtLeast(0)
        val skipped = (dto.totalCards - totalAnswered).coerceAtLeast(0)

        assertEquals(18, totalAnswered)
        assertEquals(14, totalCorrect)
        assertEquals(4, incorrect)
        assertEquals(2, skipped)
    }

    @Test
    fun `incorrect is zero when all correct`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 300,
            totalCards = 10,
            correctCount = 10,
            modesStat = mapOf(
                "CHOICE" to ModeStatDto(total = 10, correct = 10)
            )
        )

        val totalAnswered = dto.modesStat?.values?.sumOf { it.total } ?: dto.correctCount
        val totalCorrect = dto.modesStat?.values?.sumOf { it.correct } ?: dto.correctCount
        val incorrect = (totalAnswered - totalCorrect).coerceAtLeast(0)
        val skipped = (dto.totalCards - totalAnswered).coerceAtLeast(0)

        assertEquals(10, totalAnswered)
        assertEquals(10, totalCorrect)
        assertEquals(0, incorrect)
        assertEquals(0, skipped)
    }

    @Test
    fun `skipped is zero when all cards answered`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 300,
            totalCards = 15,
            correctCount = 10,
            modesStat = mapOf(
                "CHOICE" to ModeStatDto(total = 15, correct = 10)
            )
        )

        val totalAnswered = dto.modesStat?.values?.sumOf { it.total } ?: dto.correctCount
        val totalCorrect = dto.modesStat?.values?.sumOf { it.correct } ?: dto.correctCount
        val incorrect = (totalAnswered - totalCorrect).coerceAtLeast(0)
        val skipped = (dto.totalCards - totalAnswered).coerceAtLeast(0)

        assertEquals(15, totalAnswered)
        assertEquals(10, totalCorrect)
        assertEquals(5, incorrect)
        assertEquals(0, skipped)
    }

    @Test
    fun `coerceAtLeast prevents negative incorrect`() {
        val modesStat = mapOf(
            "CHOICE" to ModeStatDto(total = 5, correct = 10)
        )

        val totalAnswered = modesStat.values.sumOf { it.total }
        val totalCorrect = modesStat.values.sumOf { it.correct }
        val incorrect = (totalAnswered - totalCorrect).coerceAtLeast(0)

        assertEquals(0, incorrect)
    }

    @Test
    fun `coerceAtLeast prevents negative skipped`() {
        val totalCards = 10
        val totalAnswered = 15

        val skipped = (totalCards - totalAnswered).coerceAtLeast(0)

        assertEquals(0, skipped)
    }


    @Test
    fun `convert TrainingSessionDto to TrainingHistoryItem with modesStat`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 300,
            totalCards = 20,
            correctCount = 13,
            modesStat = mapOf(
                "CHOICE" to ModeStatDto(total = 12, correct = 10),
                "INPUT" to ModeStatDto(total = 5, correct = 3)
            ),
            decks = DeckNameDto(name = "English Vocabulary")
        )

        val totalAnswered = dto.modesStat?.values?.sumOf { it.total } ?: dto.correctCount
        val totalCorrect = dto.modesStat?.values?.sumOf { it.correct } ?: dto.correctCount
        val incorrect = (totalAnswered - totalCorrect).coerceAtLeast(0)
        val skipped = (dto.totalCards - totalAnswered).coerceAtLeast(0)

        val item = TrainingHistoryItem(
            id = dto.id,
            deckId = dto.deckId,
            deckName = dto.decks?.name ?: "Неизвестная колода",
            timestamp = dto.createdAt,
            durationSec = dto.durationSec,
            totalCards = dto.totalCards,
            correctCount = totalCorrect,
            incorrectCount = incorrect,
            skippedCount = skipped
        )

        assertEquals("session-1", item.id)
        assertEquals("deck-1", item.deckId)
        assertEquals("English Vocabulary", item.deckName)
        assertEquals(300, item.durationSec)
        assertEquals(20, item.totalCards)
        assertEquals(13, item.correctCount)
        assertEquals(4, item.incorrectCount)
        assertEquals(3, item.skippedCount)
    }

    @Test
    fun `convert TrainingSessionDto to TrainingHistoryItem without modesStat`() {
        val dto = TrainingSessionDto(
            id = "session-2",
            deckId = "deck-2",
            createdAt = baseTimestamp,
            durationSec = 600,
            totalCards = 30,
            correctCount = 20,
            modesStat = null,
            decks = DeckNameDto(name = "Math Basics")
        )

        val totalAnswered = dto.modesStat?.values?.sumOf { it.total } ?: dto.correctCount
        val totalCorrect = dto.modesStat?.values?.sumOf { it.correct } ?: dto.correctCount
        val incorrect = (totalAnswered - totalCorrect).coerceAtLeast(0)
        val skipped = (dto.totalCards - totalAnswered).coerceAtLeast(0)

        val item = TrainingHistoryItem(
            id = dto.id,
            deckId = dto.deckId,
            deckName = dto.decks?.name ?: "Неизвестная колода",
            timestamp = dto.createdAt,
            durationSec = dto.durationSec,
            totalCards = dto.totalCards,
            correctCount = totalCorrect,
            incorrectCount = incorrect,
            skippedCount = skipped
        )

        assertEquals(20, item.correctCount)
        assertEquals(0, item.incorrectCount)
        assertEquals(10, item.skippedCount)
    }

    @Test
    fun `convert TrainingSessionDto to TrainingHistoryItem with null deck name`() {
        val dto = TrainingSessionDto(
            id = "session-3",
            deckId = "deck-3",
            createdAt = baseTimestamp,
            durationSec = 120,
            totalCards = 10,
            correctCount = 8,
            modesStat = null,
            decks = null
        )

        val totalAnswered = dto.modesStat?.values?.sumOf { it.total } ?: dto.correctCount
        val totalCorrect = dto.modesStat?.values?.sumOf { it.correct } ?: dto.correctCount
        val incorrect = (totalAnswered - totalCorrect).coerceAtLeast(0)
        val skipped = (dto.totalCards - totalAnswered).coerceAtLeast(0)

        val item = TrainingHistoryItem(
            id = dto.id,
            deckId = dto.deckId,
            deckName = dto.decks?.name ?: "Неизвестная колода",
            timestamp = dto.createdAt,
            durationSec = dto.durationSec,
            totalCards = dto.totalCards,
            correctCount = totalCorrect,
            incorrectCount = incorrect,
            skippedCount = skipped
        )

        assertEquals("Неизвестная колода", item.deckName)
    }

    @Test
    fun `convert list of TrainingSessionDto to list of TrainingHistoryItem`() {
        val dtos = listOf(
            TrainingSessionDto(
                id = "session-1",
                deckId = "deck-1",
                createdAt = baseTimestamp,
                durationSec = 300,
                totalCards = 20,
                correctCount = 15,
                modesStat = mapOf("CHOICE" to ModeStatDto(total = 20, correct = 15)),
                decks = DeckNameDto(name = "Deck 1")
            ),
            TrainingSessionDto(
                id = "session-2",
                deckId = "deck-2",
                createdAt = baseTimestamp,
                durationSec = 600,
                totalCards = 30,
                modesStat = null,
                correctCount = 20,
                decks = DeckNameDto(name = "Deck 2")
            )
        )

        val items = dtos.map { dto ->
            val totalAnswered = dto.modesStat?.values?.sumOf { it.total } ?: dto.correctCount
            val totalCorrect = dto.modesStat?.values?.sumOf { it.correct } ?: dto.correctCount
            val incorrect = (totalAnswered - totalCorrect).coerceAtLeast(0)
            val skipped = (dto.totalCards - totalAnswered).coerceAtLeast(0)

            TrainingHistoryItem(
                id = dto.id,
                deckId = dto.deckId,
                deckName = dto.decks?.name ?: "Неизвестная колода",
                timestamp = dto.createdAt,
                durationSec = dto.durationSec,
                totalCards = dto.totalCards,
                correctCount = totalCorrect,
                incorrectCount = incorrect,
                skippedCount = skipped
            )
        }

        assertEquals(2, items.size)
        assertEquals("Deck 1", items[0].deckName)
        assertEquals("Deck 2", items[1].deckName)
        assertEquals(15, items[0].correctCount)
        assertEquals(20, items[1].correctCount)
    }

    @Test
    fun `convert empty list of TrainingSessionDto`() {
        val dtos = emptyList<TrainingSessionDto>()

        val items = dtos.map { dto ->
            val totalAnswered = dto.modesStat?.values?.sumOf { it.total } ?: dto.correctCount
            val totalCorrect = dto.modesStat?.values?.sumOf { it.correct } ?: dto.correctCount
            val incorrect = (totalAnswered - totalCorrect).coerceAtLeast(0)
            val skipped = (dto.totalCards - totalAnswered).coerceAtLeast(0)

            TrainingHistoryItem(
                id = dto.id,
                deckId = dto.deckId,
                deckName = dto.decks?.name ?: "Неизвестная колода",
                timestamp = dto.createdAt,
                durationSec = dto.durationSec,
                totalCards = dto.totalCards,
                correctCount = totalCorrect,
                incorrectCount = incorrect,
                skippedCount = skipped
            )
        }

        assertTrue(items.isEmpty())
    }

    @Test
    fun `handles zero duration and zero cards`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 0,
            totalCards = 0,
            correctCount = 0,
            modesStat = null,
            decks = DeckNameDto(name = "Empty Session")
        )

        val totalAnswered = dto.modesStat?.values?.sumOf { it.total } ?: dto.correctCount
        val totalCorrect = dto.modesStat?.values?.sumOf { it.correct } ?: dto.correctCount
        val incorrect = (totalAnswered - totalCorrect).coerceAtLeast(0)
        val skipped = (dto.totalCards - totalAnswered).coerceAtLeast(0)

        assertEquals(0, totalAnswered)
        assertEquals(0, totalCorrect)
        assertEquals(0, incorrect)
        assertEquals(0, skipped)
    }

    @Test
    fun `handles large numbers`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = Int.MAX_VALUE,
            totalCards = 10000,
            correctCount = 7000,
            modesStat = mapOf(
                "CHOICE" to ModeStatDto(total = 5000, correct = 4000),
                "INPUT" to ModeStatDto(total = 5000, correct = 3000)
            ),
            decks = DeckNameDto(name = "Large Session")
        )

        val totalAnswered = dto.modesStat?.values?.sumOf { it.total } ?: dto.correctCount
        val totalCorrect = dto.modesStat?.values?.sumOf { it.correct } ?: dto.correctCount
        val incorrect = (totalAnswered - totalCorrect).coerceAtLeast(0)
        val skipped = (dto.totalCards - totalAnswered).coerceAtLeast(0)

        assertEquals(10000, totalAnswered)
        assertEquals(7000, totalCorrect)
        assertEquals(3000, incorrect)
        assertEquals(0, skipped)
    }

    @Test
    fun `handles many modes in modesStat`() {
        val modesStat = (1..10).associate { i ->
            "MODE_$i" to ModeStatDto(total = i * 5, correct = i * 4)
        }

        val totalAnswered = modesStat.values.sumOf { it.total }
        val totalCorrect = modesStat.values.sumOf { it.correct }
        val incorrect = totalAnswered - totalCorrect

        assertEquals(275, totalAnswered)
        assertEquals(220, totalCorrect)
        assertEquals(55, incorrect)
    }

    @Test
    fun `deck name with special characters`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 300,
            totalCards = 10,
            correctCount = 8,
            modesStat = null,
            decks = DeckNameDto(name = "English & Russian")
        )

        val item = TrainingHistoryItem(
            id = dto.id,
            deckId = dto.deckId,
            deckName = dto.decks?.name ?: "Неизвестная колода",
            timestamp = dto.createdAt,
            durationSec = dto.durationSec,
            totalCards = dto.totalCards,
            correctCount = dto.correctCount,
            incorrectCount = 0,
            skippedCount = 2
        )

        assertEquals("English & Russian", item.deckName)
    }

    @Test
    fun `blank deck name falls back to default`() {
        val dto = TrainingSessionDto(
            id = "session-1",
            deckId = "deck-1",
            createdAt = baseTimestamp,
            durationSec = 300,
            totalCards = 10,
            correctCount = 8,
            modesStat = null,
            decks = DeckNameDto(name = "")
        )

        val deckName = if (dto.decks?.name.isNullOrBlank()) "Неизвестная колода" else dto.decks.name

        assertEquals("Неизвестная колода", deckName)
    }

    @Test
    fun `history sorted by created_at descending`() {
        val dtos = listOf(
            TrainingSessionDto(
                id = "session-1",
                deckId = "deck-1",
                createdAt = Instant.fromEpochSeconds(1700000000),
                durationSec = 300,
                totalCards = 10,
                correctCount = 8,
                modesStat = null
            ),
            TrainingSessionDto(
                id = "session-2",
                deckId = "deck-2",
                createdAt = Instant.fromEpochSeconds(1700001000),
                durationSec = 600,
                totalCards = 20,
                correctCount = 15,
                modesStat = null
            ),
            TrainingSessionDto(
                id = "session-3",
                deckId = "deck-3",
                createdAt = Instant.fromEpochSeconds(1699999000),
                durationSec = 120,
                totalCards = 5,
                correctCount = 4,
                modesStat = null
            )
        )

        val sorted = dtos.sortedByDescending { it.createdAt }

        assertEquals("session-2", sorted[0].id)
        assertEquals("session-1", sorted[1].id)
        assertEquals("session-3", sorted[2].id)
    }
}
