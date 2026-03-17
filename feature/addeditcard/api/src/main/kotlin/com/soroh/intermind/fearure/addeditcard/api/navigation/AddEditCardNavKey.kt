package com.soroh.intermind.fearure.addeditcard.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class AddEditCardNavKey(
    val cardId: String? = null,
    val deckId: String
): NavKey