package com.soroh.intermind.core.data.dto.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSettingsDto(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("push_enabled") val pushEnabled: Boolean,
    @SerialName("notification_threshold") val notificationThreshold: Int,
    @SerialName("preferred_time") val preferredTime: String?,
    @SerialName("timezone") val timezone: String
)

data class UserProfile(
    val id: String,
    val email: String,
    val nickname: String,
    val avatarUrl: String?,
    val settings: UserSettingsDto
)