package com.soroh.intermind.feature.auth.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.feature.auth.api.navigation.ForgotPasswordNavKey
import com.soroh.intermind.feature.auth.api.navigation.LoginNavKey
import com.soroh.intermind.feature.auth.api.navigation.RegistrationNavKey
import com.soroh.intermind.feature.auth.impl.LoginScreen

fun EntryProviderScope<NavKey>.loginEntry(backStack: NavBackStack<NavKey>) {
    entry<LoginNavKey> {
        LoginScreen(
            onNavigateToRegistration = { backStack.add(RegistrationNavKey) },
            onNavigateToForgotPassword = { backStack.add(ForgotPasswordNavKey) }
        )
    }
}
