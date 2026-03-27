package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.model.ObjectiveResult
import com.soroh.intermind.core.data.model.SessionStatistics
import com.soroh.intermind.core.data.model.UserCardProgress
import com.soroh.intermind.core.domain.entity.Card
import com.soroh.intermind.core.domain.entity.TestType
import com.soroh.intermind.core.domain.entity.TrainingCard
import com.soroh.intermind.core.domain.entity.TrainingModes

/**
 * Data layer interface for the training feature.
 */
interface TrainingRepository {

    suspend fun prepareTrainingCards(
        deckId: String,
        dailyLimit: Int = 20
    ): Result<List<TrainingItem>>

    /**
     * Обрабатывает результат объективного теста, пересчитывает параметры FSRS
     * и сохраняет (upsert) обновленный прогресс в Supabase.
     */
    suspend fun updateCardProgress(
        currentProgress: UserCardProgress,
        result: ObjectiveResult
    ): Result<UserCardProgress>

    suspend fun saveSessionResult(stats: SessionStatistics): Result<Unit>

    suspend fun checkFillInTheBlankAnswer(
        userInput: String,
        correctWords: List<String>
    ): Double

    /**
     * Сохраняет режимы тренировки.
     *
     * @param trainingModes Режимы тренировки.
     */
    suspend fun saveTrainingModes(trainingModes: TrainingModes): Result<Unit>

    /**
     * Получает режимы тренировки для колоды.
     *
     * @param deckId Идентификатор колоды.
     * @return Режимы тренировки.
     */
    suspend fun getTrainingModes(deckId: String): Result<TrainingModes>

    suspend fun getAverageTime(testType: TestType): Long

    suspend fun updateAverageTime(testType: TestType, responseTimeMs: Long)
}