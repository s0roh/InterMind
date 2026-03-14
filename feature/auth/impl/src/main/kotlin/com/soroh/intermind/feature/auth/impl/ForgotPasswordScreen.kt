package com.soroh.intermind.feature.auth.impl

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.soroh.intermind.core.designsystem.component.ThemeDevicePreviews
import com.soroh.intermind.core.designsystem.theme.InterMindTheme
import com.soroh.intermind.feature.auth.api.R
import com.soroh.intermind.feature.auth.impl.components.AuthActionButton
import com.soroh.intermind.feature.auth.impl.components.AuthContent
import com.soroh.intermind.feature.auth.impl.components.AuthForm
import com.soroh.intermind.feature.auth.impl.components.AuthTextButton
import com.soroh.intermind.feature.auth.impl.model.AuthField

@Composable
internal fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    ForgotPasswordScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onSendClick = viewModel::forgotPassword,
        onResendClick = viewModel::resendResetEmail,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onResendClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    AuthContent(
        modifier = modifier,
        title = if (state.isEmailSent) {
            stringResource(R.string.feature_auth_api_forgot_password_title_sent)
        } else {
            stringResource(R.string.feature_auth_api_forgot_password_title)
        },
        subtitle = if (state.isEmailSent) {
            stringResource(R.string.feature_auth_api_forgot_password_subtitle_sent)
        } else {
            stringResource(R.string.feature_auth_api_forgot_password_subtitle)
        },
        onNavigateBack = onNavigateBack,
        content = {
            if (!state.isEmailSent) {
                AuthForm(
                    fields = listOf(
                        AuthField.Email(
                            value = state.email,
                            onValueChange = onEmailChange,
                            isError = state.emailError != null,
                            supportingText = state.emailError
                        )
                    )
                )

                Spacer(Modifier.height(32.dp))

                AuthActionButton(
                    text = stringResource(R.string.feature_auth_api_forgot_password_button),
                    isLoading = state.isLoading,
                    enabled = state.email.isNotBlank(),
                    onClick = onSendClick
                )
            } else {
                Spacer(modifier = Modifier.height(32.dp))

                AuthTextButton(
                    text = stringResource(R.string.feature_auth_api_forgot_password_resend),
                    onClick = onResendClick
                )
            }
        }
    ) { /* No Footer for this screen */ }
}

@ThemeDevicePreviews
@Composable
private fun ForgotPasswordScreenPreview() {
    InterMindTheme {
        Surface {
            ForgotPasswordScreen(
                state = AuthUiState(
                    email = "user@example.com",
                    isLoading = false,
                    emailError = null,
                    isEmailSent = false
                ),
                onEmailChange = {},
                onSendClick = {},
                onResendClick = {},
                onNavigateBack = {}
            )
        }
    }
}

@ThemeDevicePreviews
@Composable
private fun ForgotPasswordScreenErrorPreview() {
    InterMindTheme {
        Surface {
            ForgotPasswordScreen(
                state = AuthUiState(
                    email = "invalid-email",
                    isLoading = false,
                    emailError = "Пользователь с таким email не найден",
                    isEmailSent = false
                ),
                onEmailChange = {},
                onSendClick = {},
                onResendClick = {},
                onNavigateBack = {}
            )
        }
    }
}

@ThemeDevicePreviews
@Composable
private fun ForgotPasswordScreenEmailSentPreview() {
    InterMindTheme {
        Surface {
            ForgotPasswordScreen(
                state = AuthUiState(
                    email = "user@example.com",
                    isLoading = false,
                    emailError = null,
                    isEmailSent = true
                ),
                onEmailChange = {},
                onSendClick = {},
                onResendClick = {},
                onNavigateBack = {}
            )
        }
    }
}