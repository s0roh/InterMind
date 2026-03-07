package com.soroh.intermind.feature.profile.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.Navigator
import com.soroh.intermind.feature.profile.api.navigation.ProfileNavKey
import com.soroh.intermind.feature.profile.impl.ProfileScreen

fun EntryProviderScope<NavKey>.profileEntry(navigator: Navigator) {
    entry<ProfileNavKey> {
        ProfileScreen()
    }
}
