package com.soroh.intermind.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DeckTrainingStatsDto(
    @SerialName("deck_id") val deckId: String,
    @SerialName("new_count") val newCount: Int,
    @SerialName("review_count") val reviewCount: Int
)