package com.soroh.intermind.feature.trainingmodesettings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.repository.TrainingRepository
import com.soroh.intermind.core.domain.entity.TrainingModes
import com.soroh.intermind.feature.trainingmodesettings.api.navigation.TrainingModeSettingsNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TrainingModeSettingsViewModel.Factory::class)
class TrainingModeSettingsViewModel @AssistedInject constructor(
    private val trainingRepository: TrainingRepository,
    @Assisted private val key: TrainingModeSettingsNavKey,
) : ViewModel() {

    private val _state =
        MutableStateFlow<TrainingModeSettingsState>(TrainingModeSettingsState.Loading)
    val state: StateFlow<TrainingModeSettingsState> = _state.asStateFlow()

    init {
        loadModeSettings()
    }

    private fun loadModeSettings() {
        viewModelScope.launch {
            _state.value = TrainingModeSettingsState.Loading

            trainingRepository.getTrainingModes(key.deckId)
                .onSuccess { modes ->
                    _state.value = TrainingModeSettingsState.Success(modes)
                }.onFailure { error ->
                    _state.value = TrainingModeSettingsState.Error(
                        error.message ?: "Не удалось загрузить настройки"
                    )
                }
        }
    }

    fun updateModes(modes: TrainingModes) {
        if (_state.value is TrainingModeSettingsState.Success) {
            _state.value = TrainingModeSettingsState.Success(modes)
        }
    }

    fun saveModeSettings() {
        val currentState = _state.value
        if (currentState is TrainingModeSettingsState.Success) {
            viewModelScope.launch {
                trainingRepository.saveTrainingModes(currentState.modes)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(key: TrainingModeSettingsNavKey): TrainingModeSettingsViewModel
    }
}

sealed interface TrainingModeSettingsState {
    data object Loading : TrainingModeSettingsState
    data class Error(val message: String) : TrainingModeSettingsState
    data class Success(val modes: TrainingModes) : TrainingModeSettingsState
}