package com.soroh.intermind.feature.auth.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.soroh.intermind.core.navigation.deeplink.DeepLinkKey
import com.soroh.intermind.core.navigation.deeplink.buildSyntheticBackStack
import com.soroh.intermind.feature.auth.api.navigation.RegistrationNavKey
import com.soroh.intermind.feature.auth.impl.navigation.forgotPasswordEntry
import com.soroh.intermind.feature.auth.impl.navigation.loginEntry
import com.soroh.intermind.feature.auth.impl.navigation.registrationEntry
import com.soroh.intermind.feature.auth.impl.navigation.resetPasswordEntry

@Composable
fun AuthScreen(
    deepLinkKey: DeepLinkKey?,
    onPasswordResetSuccess: () -> Unit = {}
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val screenState by viewModel.screenState.collectAsState()

    LaunchedEffect(screenState) {
        if (screenState is AuthState.PasswordResetSuccess) {
            onPasswordResetSuccess()
            viewModel.onPasswordResetHandled()
        }
    }

    val syntheticBackStack: List<NavKey> = remember(deepLinkKey) {
        deepLinkKey?.let { buildSyntheticBackStack(it) } ?: listOf(RegistrationNavKey)
    }

    val backStack = rememberNavBackStack(*syntheticBackStack.toTypedArray())

    NavDisplay(
        modifier = Modifier,
        backStack = backStack,
        entryProvider = entryProvider {
            loginEntry(backStack)
            registrationEntry(backStack)
            forgotPasswordEntry(backStack)
            resetPasswordEntry(backStack)
        }
    )
}