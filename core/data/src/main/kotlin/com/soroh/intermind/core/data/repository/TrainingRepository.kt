package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.model.ObjectiveResult
import com.soroh.intermind.core.data.model.SessionStatistics
import com.soroh.intermind.core.data.model.UserCardProgress
import com.soroh.intermind.core.domain.entity.Card
import com.soroh.intermind.core.domain.entity.TestType
import com.soroh.intermind.core.domain.entity.TrainingCard

/**
 * Data layer interface for the training feature.
 */
interface TrainingRepository {

    suspend fun prepareTrainingCards(
        deckId: String,
        dailyLimit: Int = 20,
        modes: Set<TestType>
    ): Result<List<TrainingItem>>

    /**
     * Обрабатывает результат объективного теста, пересчитывает параметры FSRS
     * и сохраняет (upsert) обновленный прогресс в Supabase.
     */
    suspend fun processCardAnswer(
        currentProgress: UserCardProgress,
        result: ObjectiveResult
    ): Result<Unit>

    suspend fun saveSessionResult(stats: SessionStatistics): Result<Unit>

    suspend fun checkFillInTheBlankAnswer(
        userInput: String,
        correctWords: List<String>
    ): Double
}