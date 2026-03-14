package com.soroh.intermind.feature.explore.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.Navigator
import com.soroh.intermind.feature.explore.api.navigation.ExploreNavKey
import com.soroh.intermind.feature.explore.impl.ExploreScreen

fun EntryProviderScope<NavKey>.exploreEntry(navigator: Navigator) {
    entry<ExploreNavKey> {
        ExploreScreen()
    }
}
