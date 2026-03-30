package com.soroh.intermind.feature.decks.api.navigation

import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.deeplink.DeepLinkKey
import com.soroh.intermind.feature.explore.api.navigation.ExploreNavKey
import kotlinx.serialization.Serializable

@Serializable
object DecksNavKey: DeepLinkKey {
    override val parent: NavKey = ExploreNavKey
}