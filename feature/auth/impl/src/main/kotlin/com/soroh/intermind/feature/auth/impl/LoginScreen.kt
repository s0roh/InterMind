package com.soroh.intermind.feature.auth.impl

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soroh.intermind.core.designsystem.component.ThemeDevicePreviews
import com.soroh.intermind.core.designsystem.theme.InterMindTheme
import com.soroh.intermind.feature.auth.api.R
import com.soroh.intermind.feature.auth.impl.components.AuthActionButton
import com.soroh.intermind.feature.auth.impl.components.AuthContent
import com.soroh.intermind.feature.auth.impl.components.AuthDivider
import com.soroh.intermind.feature.auth.impl.components.AuthFooter
import com.soroh.intermind.feature.auth.impl.components.AuthForm
import com.soroh.intermind.feature.auth.impl.components.AuthTextButton
import com.soroh.intermind.feature.auth.impl.components.GoogleSignInButton
import com.soroh.intermind.feature.auth.impl.model.AuthField
import com.soroh.intermind.feature.auth.impl.util.GoogleAuthUiClient
import kotlinx.coroutines.launch

@Composable
internal fun LoginScreen(
    onNavigateToRegistration: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignInClick = viewModel::signIn,
        onGoogleSignInClick = {
            coroutineScope.launch {
                val idToken = GoogleAuthUiClient.signIn(context)
                if (idToken != null) {
                    viewModel.loginWithGoogle(idToken)
                }
            }
        },
        onNavigateToRegistration = onNavigateToRegistration,
        onNavigateToForgotPassword = onNavigateToForgotPassword
    )
}

@Composable
private fun LoginScreen(
    modifier: Modifier = Modifier,
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNavigateToRegistration: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    AuthContent(
        modifier = modifier,
        title = stringResource(R.string.feature_auth_api_login_title),
        subtitle = stringResource(R.string.feature_auth_api_login_subtitle),
        content = {
            GoogleSignInButton(onGoogleSignInClick)

            AuthDivider()

            AuthForm(
                fields = listOf(
                    AuthField.Email(
                        value = state.email,
                        onValueChange = onEmailChange,
                        isError = state.emailError != null,
                        supportingText = state.emailError
                    ),
                    AuthField.Password(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        isVisible = isPasswordVisible,
                        onVisibilityChange = {
                            isPasswordVisible = !isPasswordVisible
                        },
                        isError = state.passwordError != null,
                        supportingText = state.passwordError
                    )
                )
            )

            Spacer(Modifier.height(32.dp))

            AuthActionButton(
                text = stringResource(R.string.feature_auth_api_login_button),
                isLoading = state.isLoading,
                enabled = state.email.isNotBlank() && state.password.isNotBlank(),
                onClick = onSignInClick
            )

            AuthTextButton(
                text = stringResource(R.string.feature_auth_api_login_forgot_password),
                onClick = onNavigateToForgotPassword
            )
        }
    ) {
        AuthFooter(
            text = stringResource(R.string.feature_auth_api_login_no_account),
            actionText = stringResource(R.string.feature_auth_api_login_sign_up),
            onActionClick = onNavigateToRegistration
        )
    }
}

@ThemeDevicePreviews
@Composable
private fun LoginScreenPreview() {
    InterMindTheme {
        Surface {
            LoginScreen(
                state = AuthUiState(
                    email = "user@example.com",
                    password = "password123",
                    isLoading = false,
                    emailError = null,
                    passwordError = null
                ),
                onEmailChange = {},
                onPasswordChange = {},
                onSignInClick = {},
                onGoogleSignInClick = {},
                onNavigateToRegistration = {},
                onNavigateToForgotPassword = {}
            )
        }
    }
}

@ThemeDevicePreviews
@Composable
private fun LoginScreenErrorPreview() {
    InterMindTheme {
        Surface {
            LoginScreen(
                state = AuthUiState(
                    email = "invalid-email",
                    password = "123",
                    isLoading = false,
                    emailError = "Неверный формат email",
                    passwordError = "Пароль должен содержать минимум 6 символов"
                ),
                onEmailChange = {},
                onPasswordChange = {},
                onSignInClick = {},
                onGoogleSignInClick = {},
                onNavigateToRegistration = {},
                onNavigateToForgotPassword = {}
            )
        }
    }
}

@ThemeDevicePreviews
@Composable
private fun LoginScreenLoadingPreview() {
    InterMindTheme {
        Surface {
            LoginScreen(
                state = AuthUiState(
                    email = "user@example.com",
                    password = "password123",
                    isLoading = true,
                    emailError = null,
                    passwordError = null
                ),
                onEmailChange = {},
                onPasswordChange = {},
                onSignInClick = {},
                onGoogleSignInClick = {},
                onNavigateToRegistration = {},
                onNavigateToForgotPassword = {}
            )
        }
    }
}