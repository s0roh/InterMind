package com.soroh.intermind.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CardAnswerDto(
    @SerialName("id") val id: String,
    @SerialName("answer") val answer: String
)