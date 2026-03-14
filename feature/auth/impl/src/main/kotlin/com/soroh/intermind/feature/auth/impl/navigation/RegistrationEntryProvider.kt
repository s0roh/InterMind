package com.soroh.intermind.feature.auth.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.feature.auth.api.navigation.LoginNavKey
import com.soroh.intermind.feature.auth.api.navigation.RegistrationNavKey
import com.soroh.intermind.feature.auth.impl.RegistrationScreen

fun EntryProviderScope<NavKey>.registrationEntry(backStack: NavBackStack<NavKey>) {
    entry<RegistrationNavKey> {
        RegistrationScreen(onNavigateToLogin = { backStack.navigateSingleTop(LoginNavKey) })
    }
}
