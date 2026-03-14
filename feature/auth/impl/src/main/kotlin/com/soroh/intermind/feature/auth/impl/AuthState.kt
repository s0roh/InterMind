package com.soroh.intermind.feature.auth.impl

sealed interface AuthEvent {
    data object PasswordResetSuccess : AuthEvent
}

data class AuthUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isEmailSent: Boolean = false,
    val isLoading: Boolean = false
)