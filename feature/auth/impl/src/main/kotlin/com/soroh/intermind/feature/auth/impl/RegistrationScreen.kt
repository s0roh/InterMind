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
import com.soroh.intermind.feature.auth.impl.components.GoogleSignInButton
import com.soroh.intermind.feature.auth.impl.model.AuthField
import com.soroh.intermind.feature.auth.impl.util.GoogleAuthUiClient
import kotlinx.coroutines.launch

@Composable
internal fun RegistrationScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RegistrationScreen(
        state = state,
        onUsernameChange = viewModel::onUsernameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignUpClick = viewModel::signUp,
        onGoogleSignInClick = {
            coroutineScope.launch {
                val idToken = GoogleAuthUiClient.signIn(context)
                if (idToken != null) {
                    viewModel.loginWithGoogle(idToken)
                }
            }
        },
        onNavigateToLogin = onNavigateToLogin,
    )
}

@Composable
private fun RegistrationScreen(
    modifier: Modifier = Modifier,
    state: AuthUiState,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    AuthContent(
        modifier = modifier,
        title = stringResource(R.string.feature_auth_api_registration_title),
        subtitle = stringResource(R.string.feature_auth_api_registration_subtitle),
        content = {
            GoogleSignInButton(
                onClick = onGoogleSignInClick
            )

            AuthDivider()

            AuthForm(
                fields = listOf(
                    AuthField.Username(
                        value = state.username,
                        onValueChange = onUsernameChange
                    ),
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
                text = stringResource(R.string.feature_auth_api_registration_button),
                isLoading = state.isLoading,
                enabled = state.email.isNotBlank() && state.password.isNotBlank(),
                onClick = onSignUpClick
            )
        }
    ) {
        AuthFooter(
            text = stringResource(R.string.feature_auth_api_registration_have_account),
            actionText = stringResource(R.string.feature_auth_api_registration_login),
            onActionClick = onNavigateToLogin
        )
    }
}

@ThemeDevicePreviews
@Composable
private fun RegistrationScreenPreview() {
    InterMindTheme {
        Surface {
            RegistrationScreen(
                state = AuthUiState(
                    username = "john_doe",
                    email = "john@example.com",
                    password = "password123",
                    isLoading = false,
                    emailError = null,
                    passwordError = null
                ),
                onUsernameChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onSignUpClick = {},
                onGoogleSignInClick = {},
                onNavigateToLogin = {}
            )
        }
    }
}

@ThemeDevicePreviews
@Composable
private fun RegistrationScreenErrorPreview() {
    InterMindTheme {
        Surface {
            RegistrationScreen(
                state = AuthUiState(
                    username = "jo",
                    email = "invalid-email",
                    password = "123",
                    isLoading = false,
                    emailError = "Неверный формат email",
                    passwordError = "Пароль должен содержать минимум 6 символов"
                ),
                onUsernameChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onSignUpClick = {},
                onGoogleSignInClick = {},
                onNavigateToLogin = {}
            )
        }
    }
}

@ThemeDevicePreviews
@Composable
private fun RegistrationScreenLoadingPreview() {
    InterMindTheme {
        Surface {
            RegistrationScreen(
                state = AuthUiState(
                    username = "john_doe",
                    email = "john@example.com",
                    password = "password123",
                    isLoading = true,
                    emailError = null,
                    passwordError = null
                ),
                onUsernameChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onSignUpClick = {},
                onGoogleSignInClick = {},
                onNavigateToLogin = {}
            )
        }
    }
}