package com.soroh.intermind.feature.history.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.Navigator
import com.soroh.intermind.feature.history.api.navigation.HistoryNavKey
import com.soroh.intermind.feature.history.impl.HistoryScreen

fun EntryProviderScope<NavKey>.historyEntry(navigator: Navigator) {
    entry<HistoryNavKey> {
        HistoryScreen()
    }
}
