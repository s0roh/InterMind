package com.soroh.intermind.feature.auth.impl

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.model.AuthError
import com.soroh.intermind.core.data.model.AuthResponse
import com.soroh.intermind.core.data.repository.AuthRepository
import com.soroh.intermind.core.designsystem.util.SnackbarController
import com.soroh.intermind.core.designsystem.util.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthViewModelTag"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signUp() {
        val state = _uiState.value

        val emailError = validateEmail(state.email.trim())
        val passwordError = validatePassword(state.password.trim())

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        authRepository.signUpWithEmail(
            name = state.username.trim(),
            email = state.email.trim(),
            password = state.password.trim()
        ).collectAuth(
            onSuccess = {
                Log.d(TAG, "SignUp success")
            }
        )
    }

    fun signIn() {
        val state = _uiState.value

        val emailError = validateEmail(state.email.trim())
        val passwordError = validatePassword(state.password.trim())

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        authRepository.signInWithEmail(
            email = state.email.trim(),
            password = state.password.trim()
        ).collectAuth(
            onSuccess = {
                Log.d(TAG, "SignIn success")
            }
        )
    }

    fun loginWithGoogle(idToken: String) {
        authRepository.loginWithGoogleToken(idToken)
            .onEach { response ->
                when (response) {
                    AuthResponse.Success -> Log.d(TAG, "Google Success")
                    is AuthResponse.Error -> Log.e(TAG, "Google Error: ${response.error}")
                }
            }.launchIn(viewModelScope)
    }

    fun forgotPassword() {
        val state = _uiState.value

        val emailError = validateEmail(state.email.trim())

        if (emailError != null) {
            _uiState.update {
                it.copy(emailError = emailError)
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        authRepository.resetPassword(
            email = state.email.trim()
        ).collectAuth(
            onSuccess = {
                _uiState.update { it.copy(isEmailSent = true) }
                showSnackbar("Письмо для сброса пароля отправлено")
            }
        )
    }

    fun resetPassword() {
        val state = _uiState.value

        val (passwordError, confirmError) =
            validatePasswords(
                password = state.password.trim(),
                confirmPassword = state.confirmPassword.trim()
            )

        if (passwordError != null || confirmError != null) {
            _uiState.update {
                it.copy(
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        authRepository.updatePassword(
            newPassword = state.password.trim()
        ).collectAuth(
            onSuccess = {
                viewModelScope.launch {
                    _events.emit(AuthEvent.PasswordResetSuccess)
                }
            },
            onError = { error ->
                when (error) {
                    AuthError.SamePassword -> showSnackbar(error.message)
                    else -> showSnackbar(error.message)
                }
            }
        )
    }

    fun onUsernameChange(value: String) {
        _uiState.update {
            it.copy(username = value)
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                confirmPassword = value,
                confirmPasswordError = null
            )
        }
    }

    fun resendResetEmail() {
        _uiState.update {
            it.copy(isEmailSent = false)
        }
    }

    private fun validateEmail(email: String): String? {
        return if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            "Введите корректный email"
        } else null
    }

    private fun validatePassword(password: String): String? {
        return if (password.trim().length < 6) {
            "Пароль должен быть не менее 6 символов"
        } else null
    }

    private fun validatePasswords(
        password: String,
        confirmPassword: String
    ): Pair<String?, String?> {

        val passwordError = validatePassword(password.trim())

        val confirmError =
            if (password.trim() != confirmPassword.trim()) {
                "Пароли не совпадают"
            } else null

        return passwordError to confirmError
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            SnackbarController.sendEvent(
                event = SnackbarEvent(
                    message = message,
                )
            )
        }
    }

    private fun Flow<AuthResponse>.collectAuth(
        onSuccess: suspend () -> Unit,
        onError: suspend (AuthError) -> Unit = { error ->
            showSnackbar(error.message)
        }
    ) {
        onEach { response ->
            _uiState.update { it.copy(isLoading = false) }

            when (response) {
                AuthResponse.Success -> onSuccess()
                is AuthResponse.Error -> onError(response.error)
            }
        }.launchIn(viewModelScope)
    }
}