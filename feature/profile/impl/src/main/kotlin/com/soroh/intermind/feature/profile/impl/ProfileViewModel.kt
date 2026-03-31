package com.soroh.intermind.feature.profile.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.dto.profile.UserProfile
import com.soroh.intermind.core.data.dto.profile.UserSettingsDto
import com.soroh.intermind.core.data.model.AuthResponse
import com.soroh.intermind.core.data.repository.AuthRepository
import com.soroh.intermind.core.data.repository.ProfileRepository
import com.soroh.intermind.core.designsystem.util.SnackbarController
import com.soroh.intermind.core.designsystem.util.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val profile = profileRepository.getUserProfile()
                if (profile != null) {
                    _uiState.value = ProfileUiState.Success(profile)
                } else {
                    _uiState.value = ProfileUiState.Error("User not found")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updatePushEnabled(enabled: Boolean) {
        updateSettings { it.copy(pushEnabled = enabled) }
    }

    fun updateThreshold(threshold: Int) {
        updateSettings { it.copy(notificationThreshold = threshold) }
    }

    private fun updateSettings(transform: (UserSettingsDto) -> UserSettingsDto) {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            val newSettings = transform(currentState.profile.settings)

            // Оптимистичное обновление UI
            _uiState.value = ProfileUiState.Success(
                currentState.profile.copy(settings = newSettings)
            )

            viewModelScope.launch {
                try {
                    profileRepository.updateSettings(newSettings)
                } catch (_: Exception) {
                    loadProfile()
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
            .onEach { response ->
                when (response) {
                    is AuthResponse.Success -> {
                        showSnackbar("Вы вышли из аккаунта")
                    }

                    is AuthResponse.Error -> {
                        showSnackbar("Ошибка при выходе")
                    }
                }
            }.launchIn(viewModelScope)
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
}

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data class Success(val profile: UserProfile) : ProfileUiState
}
