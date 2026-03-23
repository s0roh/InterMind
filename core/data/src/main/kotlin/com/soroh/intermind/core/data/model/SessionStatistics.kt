package com.soroh.intermind.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionStatistics(
    val deckId: String,
    val durationSec: Int,
    val totalCards: Int,
    val correctCount: Int,
    val modesStat: Map<String, ModeStat>
)

@Serializable
data class ModeStat(val correct: Int, val total: Int)