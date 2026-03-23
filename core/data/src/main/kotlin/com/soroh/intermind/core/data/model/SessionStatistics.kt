package com.soroh.intermind.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionStatistics(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("deck_id") val deckId: String,
    @SerialName("duration_sec") val durationSec: Int,
    @SerialName("total_cards") val totalCards: Int,
    @SerialName("correct_count") val correctCount: Int,
    @SerialName("modes_stat") val modesStat: Map<String, ModeStat>
)

@Serializable
data class ModeStat(val correct: Int, val total: Int)