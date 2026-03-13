package com.soroh.intermind.feature.auth.impl

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Error(val message: String?) : AuthState
    data object PasswordResetSuccess : AuthState
}