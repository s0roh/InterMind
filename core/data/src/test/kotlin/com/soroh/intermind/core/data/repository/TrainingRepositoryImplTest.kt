package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.dto.ExpectedTimeDto
import com.soroh.intermind.core.data.dto.TrainingModesDto
import com.soroh.intermind.core.data.dto.UserCardProgressDto
import com.soroh.intermind.core.data.model.CardPhase
import com.soroh.intermind.core.data.model.ModeStat
import com.soroh.intermind.core.data.model.ObjectiveResult
import com.soroh.intermind.core.data.model.Rating
import com.soroh.intermind.core.data.model.SessionStatistics
import com.soroh.intermind.core.data.model.TrainingItem
import com.soroh.intermind.core.data.model.UserCardProgress
import com.soroh.intermind.core.data.util.generatePartialAnswer
import com.soroh.intermind.core.data.util.levenshteinDistance
import com.soroh.intermind.core.data.util.normalizeText
import com.soroh.intermind.core.domain.entity.TestType
import com.soroh.intermind.core.domain.entity.TrainingCard
import com.soroh.intermind.core.domain.entity.TrainingModes

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class TrainingRepositoryImplTest {
    @Test
    fun `UserCardProgressDto toDomain converts correctly`() {
        val now = Instant.fromEpochSeconds(1700000000)
        val dto = UserCardProgressDto(
            id = "progress-1",
            userId = "user-1",
            cardId = "card-1",
            stability = 5.0,
            difficulty = 3.0,
            interval = 10,
            dueDate = now.toString(),
            reviewCount = 5,
            lastReview = now.toString(),
            phase = CardPhase.Review.value
        )

        val domain = dto.toDomain()

        assertEquals("progress-1", domain.id)
        assertEquals("user-1", domain.userId)
        assertEquals("card-1", domain.cardId)
        assertEquals(5.0, domain.stability)
        assertEquals(3.0, domain.difficulty)
        assertEquals(10, domain.interval)
        assertEquals(5, domain.reviewCount)
        assertEquals(CardPhase.Review.value, domain.phase)
    }

    @Test
    fun `UserCardProgressDto fromDomain creates dto correctly`() {
        val now = Instant.fromEpochSeconds(1700000000)
        val progress = UserCardProgress(
            id = "progress-1",
            userId = "user-1",
            cardId = "card-1",
            stability = 5.0,
            difficulty = 3.0,
            interval = 10,
            dueDate = now,
            reviewCount = 5,
            lastReview = now,
            phase = CardPhase.Review.value
        )

        val dto = UserCardProgressDto.fromDomain(progress)

        assertEquals("progress-1", dto.id)
        assertEquals("user-1", dto.userId)
        assertEquals("card-1", dto.cardId)
        assertEquals(5.0, dto.stability)
        assertEquals(3.0, dto.difficulty)
        assertEquals(10, dto.interval)
        assertEquals(5, dto.reviewCount)
    }

    @Test
    fun `UserCardProgressDto handles default values`() {
        val now = Instant.fromEpochSeconds(1700000000)
        val dto = UserCardProgressDto(
            userId = "user-1",
            cardId = "card-1",
            dueDate = now.toString(),
            lastReview = now.toString()
        )

        assertEquals(2.5, dto.stability)
        assertEquals(2.5, dto.difficulty)
        assertEquals(0, dto.interval)
        assertEquals(0, dto.reviewCount)
        assertEquals(0, dto.phase)
    }

    @Test
    fun `TrainingModesDto converts to TrainingModes`() {
        val dto = TrainingModesDto(
            userId = "user-1",
            deckId = "deck-1",
            modes = listOf(TestType.CHOICE, TestType.INPUT)
        )

        val domain = TrainingModes(
            deckId = dto.deckId,
            modes = dto.modes
        )

        assertEquals("deck-1", domain.deckId)
        assertEquals(2, domain.modes.size)
        assertEquals(TestType.CHOICE, domain.modes[0])
        assertEquals(TestType.INPUT, domain.modes[1])
    }

    @Test
    fun `ExpectedTimeDto converts correctly`() {
        val dto = ExpectedTimeDto(
            userId = "user-1",
            deckId = "deck-1",
            testType = TestType.CHOICE.name,
            averageTimeMs = 5000L
        )

        assertEquals("user-1", dto.userId)
        assertEquals("deck-1", dto.deckId)
        assertEquals("CHOICE", dto.testType)
        assertEquals(5000L, dto.averageTimeMs)
    }

    @Test
    fun `SessionStatistics serializes correctly`() {
        val stats = SessionStatistics(
            userId = "user-1",
            deckId = "deck-1",
            durationSec = 300,
            totalCards = 20,
            correctCount = 15,
            modesStat = mapOf(
                "CHOICE" to ModeStat(correct = 10, total = 12),
                "INPUT" to ModeStat(correct = 5, total = 8)
            )
        )

        assertEquals("user-1", stats.userId)
        assertEquals("deck-1", stats.deckId)
        assertEquals(300, stats.durationSec)
        assertEquals(20, stats.totalCards)
        assertEquals(15, stats.correctCount)
        assertEquals(2, stats.modesStat.size)
        assertEquals(10, stats.modesStat["CHOICE"]?.correct)
        assertEquals(12, stats.modesStat["CHOICE"]?.total)
    }

    @Test
    fun `ModeStat creates correctly`() {
        val modeStat = ModeStat(correct = 8, total = 10)

        assertEquals(8, modeStat.correct)
        assertEquals(10, modeStat.total)
    }

    @Test
    fun `normalizeText converts to lowercase`() {
        val input = "HELLO WORLD"
        val expected = "hello world"

        val result = normalizeText(input)

        assertEquals(expected, result)
    }

    @Test
    fun `normalizeText removes punctuation`() {
        val input = "Hello, World! How are you?"
        val expected = "hello world how are you"

        val result = normalizeText(input)

        assertEquals(expected, result)
    }

    @Test
    fun `normalizeText handles empty string`() {
        val input = ""
        val expected = ""

        val result = normalizeText(input)

        assertEquals(expected, result)
    }

    @Test
    fun `normalizeText handles null-like input`() {
        val input = "   "
        val expected = ""

        val result = normalizeText(input)

        assertEquals(expected, result)
    }

    @Test
    fun `levenshteinDistance returns 0 for identical strings`() {
        val result = levenshteinDistance("hello", "hello")

        assertEquals(0, result)
    }

    @Test
    fun `levenshteinDistance returns correct distance for one substitution`() {
        val result = levenshteinDistance("hello", "hallo")

        assertEquals(1, result)
    }

    @Test
    fun `levenshteinDistance returns correct distance for one insertion`() {
        val result = levenshteinDistance("hello", "helloo")

        assertEquals(1, result)
    }

    @Test
    fun `levenshteinDistance returns correct distance for one deletion`() {
        val result = levenshteinDistance("hello", "hell")

        assertEquals(1, result)
    }

    @Test
    fun `levenshteinDistance returns correct distance for empty string`() {
        val result = levenshteinDistance("", "hello")

        assertEquals(5, result)
    }

    @Test
    fun `levenshteinDistance returns 0 for two empty strings`() {
        val result = levenshteinDistance("", "")

        assertEquals(0, result)
    }

    @Test
    fun `levenshteinDistance handles completely different strings`() {
        val result = levenshteinDistance("abc", "xyz")

        assertEquals(3, result)
    }

    @Test
    fun `generatePartialAnswer creates partial answer with missing words`() {
        val answer = "Kotlin is a programming language"

        val (partialAnswer, missingWords, startIndex) = generatePartialAnswer(answer)

        assertTrue(partialAnswer.contains("___"))
        assertTrue(missingWords.isNotEmpty())
        assertTrue(startIndex >= 0)
    }

    @Test
    fun `generatePartialAnswer preserves original length`() {
        val answer = "Hello world test"

        val (partialAnswer, _, _) = generatePartialAnswer(answer)

        assertEquals(answer.length, partialAnswer.length)
    }

    @Test
    fun `generatePartialAnswer handles single word`() {
        val answer = "Kotlin"

        val (partialAnswer, missingWords, _) = generatePartialAnswer(answer)

        assertTrue(partialAnswer.contains("___"))
        assertTrue(missingWords.isNotEmpty())
    }

    @Test
    fun `TrainingCard for CHOICE mode has wrong answers`() {
        val card = TrainingCard(
            id = "card-1",
            testType = TestType.CHOICE,
            question = "What is Kotlin?",
            answer = "Programming language",
            wrongAnswers = listOf("Database", "Framework", "IDE")
        )

        assertEquals(TestType.CHOICE, card.testType)
        assertEquals(3, card.wrongAnswers.size)
        assertEquals("Programming language", card.answer)
    }

    @Test
    fun `TrainingCard for INPUT mode has partial answer`() {
        val card = TrainingCard(
            id = "card-1",
            testType = TestType.INPUT,
            question = "Complete the sentence",
            answer = "Kotlin is great",
            partialAnswer = "______ is great",
            missingWords = listOf("Kotlin"),
            missingWordStartIndex = 0
        )

        assertEquals(TestType.INPUT, card.testType)
        assertNotNull(card.partialAnswer)
        assertEquals(1, card.missingWords.size)
    }

    @Test
    fun `TrainingCard for TRUE_FALSE mode has displayed answer`() {
        val card = TrainingCard(
            id = "card-1",
            testType = TestType.TRUE_FALSE,
            question = "Is Kotlin a programming language?",
            answer = "Yes",
            displayedAnswer = "Yes"
        )

        assertEquals(TestType.TRUE_FALSE, card.testType)
        assertNotNull(card.displayedAnswer)
    }

    @Test
    fun `TrainingItem creates correctly`() {
        val now = Instant.fromEpochSeconds(1700000000)
        val progress = UserCardProgress(
            id = "progress-1",
            userId = "user-1",
            cardId = "card-1",
            stability = 5.0,
            difficulty = 3.0,
            interval = 10,
            dueDate = now,
            reviewCount = 5,
            lastReview = now,
            phase = CardPhase.Review.value
        )
        val trainingCard = TrainingCard(
            id = "card-1",
            testType = TestType.CHOICE,
            question = "Q",
            answer = "A",
            wrongAnswers = listOf("W1", "W2", "W3")
        )

        val item = TrainingItem(
            trainingCard = trainingCard,
            progress = progress
        )

        assertEquals(trainingCard, item.trainingCard)
        assertEquals(progress, item.progress)
    }

    @Test
    fun `TrainingModes creates with all test types`() {
        val modes = TrainingModes(
            deckId = "deck-1",
            modes = listOf(TestType.CHOICE, TestType.INPUT, TestType.TRUE_FALSE)
        )

        assertEquals("deck-1", modes.deckId)
        assertEquals(3, modes.modes.size)
        assertTrue(TestType.CHOICE in modes.modes)
        assertTrue(TestType.INPUT in modes.modes)
        assertTrue(TestType.TRUE_FALSE in modes.modes)
    }

    @Test
    fun `TrainingModes creates with single mode`() {
        val modes = TrainingModes(
            deckId = "deck-1",
            modes = listOf(TestType.CHOICE)
        )

        assertEquals(1, modes.modes.size)
        assertEquals(TestType.CHOICE, modes.modes[0])
    }

    @Test
    fun `TrainingModes handles empty modes`() {
        val modes = TrainingModes(
            deckId = "deck-1",
            modes = emptyList()
        )

        assertTrue(modes.modes.isEmpty())
    }

    @Test
    fun `Rating values are correct`() {
        assertEquals(1, Rating.Again.value)
        assertEquals(2, Rating.Hard.value)
        assertEquals(3, Rating.Good.value)
        assertEquals(4, Rating.Easy.value)
    }

    @Test
    fun `ObjectiveResult creates correctly`() {
        val result = ObjectiveResult(
            accuracy = 0.85,
            responseTimeMs = 3500,
            testType = TestType.CHOICE,
            attempts = 2
        )

        assertEquals(0.85, result.accuracy)
        assertEquals(3500, result.responseTimeMs)
        assertEquals(TestType.CHOICE, result.testType)
        assertEquals(2, result.attempts)
    }

    @Test
    fun `ObjectiveResult handles perfect score`() {
        val result = ObjectiveResult(
            accuracy = 1.0,
            responseTimeMs = 1000,
            testType = TestType.TRUE_FALSE,
            attempts = 1
        )

        assertEquals(1.0, result.accuracy)
        assertEquals(1, result.attempts)
    }

    @Test
    fun `ObjectiveResult handles zero accuracy`() {
        val result = ObjectiveResult(
            accuracy = 0.0,
            responseTimeMs = 10000,
            testType = TestType.INPUT,
            attempts = 5
        )

        assertEquals(0.0, result.accuracy)
        assertEquals(5, result.attempts)
    }

    @Test
    fun `CardPhase values are correct`() {
        assertEquals(0, CardPhase.Added.value)
        assertEquals(1, CardPhase.ReLearning.value)
        assertEquals(2, CardPhase.Review.value)
    }

    @Test
    fun `getDefaultTime returns correct values for TRUE_FALSE`() {
        val expectedTime = 5000L

        assertEquals(expectedTime, getDefaultTime(TestType.TRUE_FALSE))
    }

    @Test
    fun `getDefaultTime returns correct values for CHOICE`() {
        val expectedTime = 7000L

        assertEquals(expectedTime, getDefaultTime(TestType.CHOICE))
    }

    @Test
    fun `getDefaultTime returns correct values for INPUT`() {
        val expectedTime = 11000L

        assertEquals(expectedTime, getDefaultTime(TestType.INPUT))
    }

    @Test
    fun `EMA calculation updates correctly`() {
        val oldAverage = 5000L
        val newResponseTime = 3000L
        val alpha = 0.2

        val newAverage = (alpha * newResponseTime + (1 - alpha) * oldAverage).toLong()

        assertEquals(4600L, newAverage)
    }

    @Test
    fun `EMA calculation with same value stays stable`() {
        val oldAverage = 5000L
        val newResponseTime = 5000L
        val alpha = 0.2

        val newAverage = (alpha * newResponseTime + (1 - alpha) * oldAverage).toLong()

        assertEquals(5000L, newAverage)
    }

    @Test
    fun `EMA calculation with higher value increases`() {
        val oldAverage = 5000L
        val newResponseTime = 8000L
        val alpha = 0.2

        val newAverage = (alpha * newResponseTime + (1 - alpha) * oldAverage).toLong()

        assertEquals(5600L, newAverage)
    }

    private fun getDefaultTime(testType: TestType): Long = when (testType) {
        TestType.TRUE_FALSE -> 5000L
        TestType.CHOICE -> 7000L
        TestType.INPUT -> 11000L
    }
}
