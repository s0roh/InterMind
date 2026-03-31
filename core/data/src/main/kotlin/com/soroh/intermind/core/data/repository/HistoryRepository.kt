package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.dto.history.TrainingHistoryItem

/**
 * Data layer interface for the history feature.
 */
interface HistoryRepository {

    suspend fun getTrainingHistory(): List<TrainingHistoryItem>
}