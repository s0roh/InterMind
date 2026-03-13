package com.soroh.intermind.feature.auth.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.soroh.intermind.feature.auth.impl.components.AuthScreenContent
import com.soroh.intermind.feature.auth.impl.model.AuthField
import com.soroh.intermind.feature.auth.impl.util.GoogleAuthUiClient
import kotlinx.coroutines.launch

@Composable
internal fun LoginScreen(
    onNavigateToRegistration: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()

) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AuthScreenContent(
        title = "Welcome Back",
        subtitle = "Sign in to continue",
        fields = listOf(
            AuthField.Email(
                value = emailValue,
                onValueChange = { emailValue = it }
            ),
            AuthField.Password(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                isVisible = isPasswordVisible,
                onVisibilityChange = {}
            )
        ),
        buttonText = "Log in",
        onButtonClick = {
            viewModel.signInWithEmail(email = emailValue, password = passwordValue)
        },
        onGoogleClick = {
            coroutineScope.launch {
                val idToken = GoogleAuthUiClient.signIn(context)

                if (idToken != null) {
                    viewModel.loginWithGoogle(idToken)
                }
            }
        },
        bottomText = "Don't have an account? ",
        bottomTextAction = "Sign up",
        onBottomTextClick = onNavigateToRegistration,
        onForgotPasswordClick = onNavigateToForgotPassword,
        isLoading = isLoading
    )
}