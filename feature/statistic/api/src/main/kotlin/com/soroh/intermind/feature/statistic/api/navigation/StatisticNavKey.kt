package com.soroh.intermind.feature.statistic.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class StatisticNavKey(
    val deckId: String
) : NavKey {
}