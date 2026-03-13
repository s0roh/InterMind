package com.soroh.intermind.feature.auth.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.feature.auth.api.navigation.ResetPasswordNavKey
import com.soroh.intermind.feature.auth.impl.ResetPasswordScreen

fun EntryProviderScope<NavKey>.resetPasswordEntry(backStack: NavBackStack<NavKey>) {
    entry<ResetPasswordNavKey> {
        ResetPasswordScreen(
            onNavigateBack = { backStack.removeAt(backStack.lastIndex) },
            onPasswordResetSuccess = { backStack.removeAt(backStack.lastIndex) }
        )
    }
}
