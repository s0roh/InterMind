package com.soroh.intermind.feature.decks.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.Navigator
import com.soroh.intermind.feature.decks.api.navigation.DecksNavKey
import com.soroh.intermind.feature.decks.impl.DecksScreen

fun EntryProviderScope<NavKey>.decksEntry(navigator: Navigator) {
    entry<DecksNavKey> {
        DecksScreen()
    }
}
