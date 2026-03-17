package com.soroh.intermind.feature.addeditdeck.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class AddEditDeckNavKey(
    val deckId: String? = null
): NavKey