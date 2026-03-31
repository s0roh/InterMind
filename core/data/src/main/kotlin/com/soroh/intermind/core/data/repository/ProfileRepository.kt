package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.dto.profile.UserProfile
import com.soroh.intermind.core.data.dto.profile.UserSettingsDto

/**
 * Data layer interface for the profile feature.
 */
interface ProfileRepository {

    suspend fun getUserProfile(): UserProfile?

    suspend fun updateSettings(settings: UserSettingsDto)
}