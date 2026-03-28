package com.soroh.intermind.feature.trainingmodesettings.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class TrainingModeSettingsNavKey(
    val deckId: String
): NavKey