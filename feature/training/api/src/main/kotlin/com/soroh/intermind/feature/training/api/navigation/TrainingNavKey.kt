package com.soroh.intermind.feature.training.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class TrainingNavKey(
    val deckId: String
): NavKey