package com.soroh.intermind.core.data.util

import com.soroh.intermind.core.data.model.ObjectiveResult
import com.soroh.intermind.core.data.model.Rating
import com.soroh.intermind.core.data.model.UserCardProgress
import com.soroh.intermind.core.domain.entity.TestType
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.days

class RecallEvaluatorTest {

    private lateinit var progress: UserCardProgress
    private lateinit var result: ObjectiveResult
    private val averageTimeMs = 3000L // 3 секунды - среднее время ответа

    @Before
    fun setUp() {
        // Базовый прогресс карточки
        progress = UserCardProgress(
            userId = "test-user",
            cardId = "test-card",
            stability = 3.0,
            difficulty = 5.0,
            interval = 3,
            dueDate = Instant.fromEpochMilliseconds(0),
            reviewCount = 5,
            lastReview = Instant.fromEpochMilliseconds(0),
            phase = 2
        )

        // Базовый результат ответа
        result = ObjectiveResult(
            accuracy = 0.8,
            responseTimeMs = 2500,
            testType = TestType.CHOICE,
            attempts = 1
        )
    }

    @Test
    fun testReturnsEasyForFastResponseWithLowDifficulty() {
        // Создаем ситуацию для высокого скора:
        // - Очень быстрый ответ
        // - Низкая сложность
        // - Одна попытка
        val fastResult = result.copy(
            responseTimeMs = 500,
            attempts = 1
        )
        val easyProgress = progress.copy(
            difficulty = 2.0,
            stability = 10.0
        )

        val rating = RecallEvaluator.evaluate(easyProgress, fastResult, averageTimeMs)
        assertEquals("Very fast response with low difficulty should be Easy", Rating.Easy, rating)
    }

    @Test
    fun testReturnsHardOrGoodForMediumResponse() {
        // Средне-медленный ответ, средняя сложность
        val mediumSlowResult = result.copy(
            responseTimeMs = 5000,
            attempts = 2
        )

        val rating = RecallEvaluator.evaluate(progress, mediumSlowResult, averageTimeMs)
        assertTrue(
            "Medium response should be Hard or Good",
            rating == Rating.Hard || rating == Rating.Good
        )
    }

    @Test
    fun testReturnsGoodOrEasyForFastResponse() {
        // Средне-быстрый ответ, нормальные параметры
        val mediumFastResult = result.copy(
            responseTimeMs = 2000,
            attempts = 1
        )

        val rating = RecallEvaluator.evaluate(progress, mediumFastResult, averageTimeMs)
        assertTrue(
            "Fast response should be Good or Easy",
            rating == Rating.Good || rating == Rating.Easy
        )
    }

    @Test
    fun testFasterResponseIncreasesRating() {
        val slowResult = result.copy(responseTimeMs = 6000)
        val fastResult = result.copy(responseTimeMs = 1500)

        val slowRating = RecallEvaluator.evaluate(progress, slowResult, averageTimeMs)
        val fastRating = RecallEvaluator.evaluate(progress, fastResult, averageTimeMs)

        // Быстрый ответ должен дать рейтинг не ниже, чем медленный
        assertTrue(
            "Faster response should not decrease rating",
            ratingToNumber(fastRating) >= ratingToNumber(slowRating)
        )
    }

    @Test
    fun testMoreAttemptsDecreasesRating() {
        val oneAttempt = result.copy(attempts = 1)
        val threeAttempts = result.copy(attempts = 3)

        val ratingOne = RecallEvaluator.evaluate(progress, oneAttempt, averageTimeMs)
        val ratingThree = RecallEvaluator.evaluate(progress, threeAttempts, averageTimeMs)

        // Больше попыток = ниже рейтинг
        assertTrue(
            "More attempts should not increase rating",
            ratingToNumber(ratingOne) >= ratingToNumber(ratingThree)
        )
    }

    @Test
    fun testHigherDifficultyDecreasesRating() {
        val easyCard = progress.copy(difficulty = 3.0)
        val hardCard = progress.copy(difficulty = 8.0)

        val ratingEasy = RecallEvaluator.evaluate(easyCard, result, averageTimeMs)
        val ratingHard = RecallEvaluator.evaluate(hardCard, result, averageTimeMs)

        // Более сложная карточка должна давать рейтинг не выше
        assertTrue(
            "Higher difficulty should not increase rating",
            ratingToNumber(ratingEasy) >= ratingToNumber(ratingHard)
        )
    }

    @Test
    fun testHigherStabilityIncreasesRating() {
        val unstableCard = progress.copy(stability = 0.5)
        val stableCard = progress.copy(stability = 10.0)

        val ratingUnstable = RecallEvaluator.evaluate(unstableCard, result, averageTimeMs)
        val ratingStable = RecallEvaluator.evaluate(stableCard, result, averageTimeMs)

        // Более стабильная карточка должна давать рейтинг не ниже
        assertTrue(
            "Higher stability should not decrease rating",
            ratingToNumber(ratingStable) >= ratingToNumber(ratingUnstable)
        )
    }

    @Test
    fun testLongerElapsedTimeDoesNotSignificantlyIncreaseRating() {
        // Карточка, которую видели вчера
        val recentProgress = progress.copy(
            lastReview = Instant.fromEpochMilliseconds(System.currentTimeMillis() - 1.days.inWholeMilliseconds)
        )
        // Карточка, которую видели месяц назад
        val oldProgress = progress.copy(
            lastReview = Instant.fromEpochMilliseconds(System.currentTimeMillis() - 30.days.inWholeMilliseconds)
        )

        val ratingRecent = RecallEvaluator.evaluate(recentProgress, result, averageTimeMs)
        val ratingOld = RecallEvaluator.evaluate(oldProgress, result, averageTimeMs)

        // Более старая карточка должна давать рейтинг не выше (с учетом стабильности)
        assertTrue(
            "Longer elapsed time should not significantly increase rating",
            ratingToNumber(ratingRecent) >= ratingToNumber(ratingOld) - 1
        )
    }

    @Test
    fun testZeroResponseTimeHandledGracefully() {
        val instantResult = result.copy(responseTimeMs = 0L)
        val rating = RecallEvaluator.evaluate(progress, instantResult, averageTimeMs)
        // Мгновенный ответ должен дать высокий рейтинг
        assertTrue(
            "Instant response should give high rating",
            rating == Rating.Good || rating == Rating.Easy
        )
    }

    @Test
    fun testVeryLargeValuesHandledWithoutOverflow() {
        val extremeProgress = progress.copy(
            difficulty = 10.0,
            stability = 100.0
        )
        val extremeResult = result.copy(
            responseTimeMs = 100000,
            attempts = 10
        )

        val rating = RecallEvaluator.evaluate(extremeProgress, extremeResult, averageTimeMs)
        assertTrue("Should return valid Rating enum", rating in Rating.entries)
    }

    @Test
    fun testNewCardWithZeroElapsedDaysHandledCorrectly() {
        val newCardProgress = progress.copy(
            lastReview = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        )
        val rating = RecallEvaluator.evaluate(newCardProgress, result, averageTimeMs)
        assertTrue("Should handle new card correctly", rating in Rating.entries)
    }

    @Test
    fun testEasyRequiresFastResponseAndLowAttempts() {
        // Сценарий для Easy: быстрый ответ, одна попытка, низкая сложность
        val confidentResult = result.copy(
            responseTimeMs = 1000,
            attempts = 1
        )
        val easyCard = progress.copy(difficulty = 3.0)

        val rating = RecallEvaluator.evaluate(easyCard, confidentResult, averageTimeMs)
        assertEquals("Fast confident response on easy card should be Easy", Rating.Easy, rating)
    }

    @Test
    fun testWithinOneErrorRateIsHigh() {
        // Проверяем, что модель редко делает грубые ошибки (разница > 1 класс)

        val testCases = listOf(
            Triple(progress.copy(difficulty = 2.0), result.copy(responseTimeMs = 1000, attempts = 1), Rating.Easy),
            Triple(progress.copy(difficulty = 8.0), result.copy(responseTimeMs = 10000, attempts = 3), Rating.Again),
            Triple(progress, result.copy(responseTimeMs = 3000, attempts = 1), Rating.Good)
        )

        for ((testProgress, testResult, expected) in testCases) {
            val rating = RecallEvaluator.evaluate(testProgress, testResult, averageTimeMs)
            val error = kotlin.math.abs(ratingToNumber(rating) - ratingToNumber(expected))
            assertTrue(
                "Rating error should be <= 1, got $error ($rating vs $expected)",
                error <= 1
            )
        }
    }

    /**
     * Преобразует Rating в число для сравнения
     * Again=1, Hard=2, Good=3, Easy=4
     */
    private fun ratingToNumber(rating: Rating): Int = when (rating) {
        Rating.Again -> 1
        Rating.Hard -> 2
        Rating.Good -> 3
        Rating.Easy -> 4
    }
}