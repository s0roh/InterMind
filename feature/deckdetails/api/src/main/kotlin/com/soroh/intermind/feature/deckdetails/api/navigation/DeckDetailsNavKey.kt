package com.soroh.intermind.feature.deckdetails.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class DeckDetailsNavKey(
    val deckId: String
): NavKey