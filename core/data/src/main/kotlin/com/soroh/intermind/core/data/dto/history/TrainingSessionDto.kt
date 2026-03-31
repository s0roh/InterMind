package com.soroh.intermind.core.data.dto.history

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class TrainingSessionDto(
    val id: String,
    @SerialName("deck_id") val deckId: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("duration_sec") val durationSec: Int,
    @SerialName("total_cards") val totalCards: Int,
    @SerialName("correct_count") val correctCount: Int,
    @SerialName("modes_stat") val modesStat: Map<String, ModeStatDto>? = null,
    val decks: DeckNameDto? = null
)

@Serializable
data class ModeStatDto(val total: Int, val correct: Int)

@Serializable
data class DeckNameDto(val name: String)

data class TrainingHistoryItem(
    val id: String,
    val deckId: String,
    val deckName: String,
    val timestamp: Instant,
    val durationSec: Int,
    val totalCards: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val skippedCount: Int
)