package com.soroh.intermind.feature.auth.impl

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.soroh.intermind.feature.auth.impl.model.AuthField

@Composable
internal fun ResetPasswordScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    ResetPasswordScreen(
        state = state,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onResetPasswordClick = viewModel::resetPassword,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun ResetPasswordScreen(
    modifier: Modifier = Modifier,
    state: AuthUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onResetPasswordClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPPasswordVisible by rememberSaveable { mutableStateOf(false) }

    AuthContent(
        modifier = modifier,
        title = stringResource(R.string.feature_auth_api_reset_password_title),
        subtitle = stringResource(R.string.feature_auth_api_reset_password_subtitle),
        onNavigateBack = onNavigateBack,
        content = {
            AuthForm(
                fields = listOf(
                    AuthField.Password(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = stringResource(R.string.feature_auth_api_reset_password_new),
                        isVisible = isPasswordVisible,
                        onVisibilityChange = {
                            isPasswordVisible = !isPasswordVisible
                        },
                        isError = state.passwordError != null,
                        supportingText = state.passwordError
                    ),
                    AuthField.Password(
                        value = state.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = stringResource(R.string.feature_auth_api_reset_password_confirm),
                        isVisible = isConfirmPPasswordVisible,
                        onVisibilityChange = {
                            isConfirmPPasswordVisible = !isConfirmPPasswordVisible
                        },
                        isError = state.confirmPasswordError != null,
                        supportingText = state.confirmPasswordError
                    )
                )
            )

            Spacer(Modifier.height(32.dp))

            AuthActionButton(
                text = stringResource(R.string.feature_auth_api_reset_password_button),
                isLoading = state.isLoading,
                enabled = state.password.isNotBlank() && state.confirmPassword.isNotBlank(),
                onClick = onResetPasswordClick
            )
        }
    ) { /* No Footer for this screen */ }
}

@ThemeDevicePreviews
@Composable
private fun ResetPasswordScreenPreview() {
    InterMindTheme {
        Surface {
            ResetPasswordScreen(
                state = AuthUiState(
                    password = "newPassword123",
                    confirmPassword = "newPassword123",
                    isLoading = false,
                    passwordError = null,
                    confirmPasswordError = null
                ),
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onResetPasswordClick = {},
                onNavigateBack = {}
            )
        }
    }
}

@ThemeDevicePreviews
@Composable
private fun ResetPasswordScreenPasswordErrorPreview() {
    InterMindTheme {
        Surface {
            ResetPasswordScreen(
                state = AuthUiState(
                    password = "123",
                    confirmPassword = "123",
                    isLoading = false,
                    passwordError = "Пароль должен содержать минимум 6 символов",
                    confirmPasswordError = null
                ),
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onResetPasswordClick = {},
                onNavigateBack = {}
            )
        }
    }
}

@ThemeDevicePreviews
@Composable
private fun ResetPasswordScreenConfirmErrorPreview() {
    InterMindTheme {
        Surface {
            ResetPasswordScreen(
                state = AuthUiState(
                    password = "newPassword123",
                    confirmPassword = "differentPassword",
                    isLoading = false,
                    passwordError = null,
                    confirmPasswordError = "Пароли не совпадают"
                ),
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onResetPasswordClick = {},
                onNavigateBack = {}
            )
        }
    }
}