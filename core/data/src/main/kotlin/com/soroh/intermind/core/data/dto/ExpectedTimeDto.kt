package com.soroh.intermind.core.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ExpectedTimeDto(
    @SerialName("user_id") val userId: String,
    @SerialName("test_type") val testType: String,
    @SerialName("average_time_ms") val averageTimeMs: Long,
    @SerialName("updated_at") val updatedAt: String? = null
)