package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.dto.profile.UserProfile
import com.soroh.intermind.core.data.dto.profile.UserSettingsDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

/**
 * Implements a [ProfileRepository]
 */
class ProfileRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ProfileRepository {

    private val userSettingsTable
        get() = supabase.postgrest["user_settings"]

    override suspend fun getUserProfile(): UserProfile? {
        val user = supabase.auth.currentUserOrNull() ?: return null

        val settings = userSettingsTable
            .select {
                filter { eq("user_id", user.id) }
            }.decodeSingleOrNull<UserSettingsDto>()
            ?: UserSettingsDto(
                pushEnabled = true,
                notificationThreshold = 10,
                preferredTime = "09:00:00",
                timezone = "UTC"
            )

        return UserProfile(
            id = user.id,
            email = user.email ?: "",
            nickname = user.userMetadata?.get("full_name").toCleanString()
                ?: user.userMetadata?.get("nickname").toCleanString()
                ?: "User",
            avatarUrl = user.userMetadata?.get("avatar_url")?.toCleanString(),
            settings = settings
        )
    }

    override suspend fun updateSettings(settings: UserSettingsDto) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return

        userSettingsTable.upsert(
            settings.copy(userId = userId)
        )
    }

    private fun JsonElement?.toCleanString(): String? = this?.jsonPrimitive?.contentOrNull
}