package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.dto.history.TrainingHistoryItem
import com.soroh.intermind.core.data.dto.history.TrainingSessionDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

/**
 * Implements a [HistoryRepository]
 */
class HistoryRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : HistoryRepository {

    private val trainingSessionsTable
        get() = supabase.postgrest["training_sessions"]

    override suspend fun getTrainingHistory(): List<TrainingHistoryItem> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()

        val dtoList = trainingSessionsTable
            .select(columns = Columns.raw("id, deck_id, created_at, duration_sec, total_cards, correct_count, modes_stat, decks(name)")) {
                filter {
                    eq("user_id", userId)
                }
                order(column = "created_at", order = Order.DESCENDING)
            }.decodeList<TrainingSessionDto>()

        return dtoList.map { dto ->
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
    }
}