package com.soroh.intermind.feature.auth.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.feature.auth.api.navigation.ForgotPasswordNavKey
import com.soroh.intermind.feature.auth.impl.ForgotPasswordScreen

fun EntryProviderScope<NavKey>.forgotPasswordEntry(backStack: NavBackStack<NavKey>) {
    entry<ForgotPasswordNavKey> {
        ForgotPasswordScreen(
            onNavigateBack = { backStack.removeAt(backStack.lastIndex) }
        )
    }
}
