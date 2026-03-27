package com.soroh.intermind.core.data.dto

import com.soroh.intermind.core.domain.entity.TestType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TrainingModesDto(
    @SerialName("user_id") val userId: String,
    @SerialName("deck_id") val deckId: String,
    @SerialName("modes") val modes: List<TestType>
)