package com.soroh.intermind.feature.auth.impl

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.model.AuthResponse
import com.soroh.intermind.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val TAG = "AuthViewModelTag"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow<AuthState>(AuthState.Idle)
    val screenState: StateFlow<AuthState> = _screenState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun signUpWithEmail(name: String, email: String, password: String) {
        authRepository.signUpWithEmail(name, email, password)
            .onEach { response ->
                _screenState.value = AuthState.Loading
                when (response) {
                    is AuthResponse.Error -> {
                        Log.e(
                            TAG,
                            "Email Sign Up Error: ${response.message}"
                        )
                        _screenState.value = AuthState.Error(response.message)
                    }

                    AuthResponse.Success -> {
                        Log.d(TAG, "Email Sign Up Success")
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun signInWithEmail(email: String, password: String) {
        authRepository.signInWithEmail(email, password)
            .onEach { response ->
                _screenState.value = AuthState.Loading
                when (response) {
                    is AuthResponse.Error -> {
                        Log.e(
                            TAG,
                            "Email Sign In Error: ${response.message}"
                        )
                        _screenState.value = AuthState.Error(response.message)
                    }

                    AuthResponse.Success -> {
                        Log.d(TAG, "Email Sign In Success")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun loginWithGoogle(idToken: String) {
        authRepository.loginWithGoogleToken(idToken)
            .onEach { response ->
                when (response) {
                    is AuthResponse.Success -> Log.d(TAG, "Google Success")
                    is AuthResponse.Error -> Log.e(TAG, "Google Error: ${response.message}")
                }
            }
            .launchIn(viewModelScope)
    }

    fun resetPassword(email: String) {
        authRepository.resetPassword(email = email)
            .onEach { response ->
                when (response) {
                    is AuthResponse.Success -> {
                        Log.d(TAG, "Password reset email sent")
                    }

                    is AuthResponse.Error -> {
                        Log.e(TAG, "Reset password error: ${response.message}")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun signOut() {
        authRepository.signOut()
            .onEach {response ->
                when (response) {
                    is AuthResponse.Success -> {
                        _uiEvent.emit("Вы вышли из аккаунта")
                    }
                    is AuthResponse.Error -> {
                        _uiEvent.emit(response.message ?: "Ошибка при выходе")
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}