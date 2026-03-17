package com.soroh.intermind.feature.addeditdeck.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.repository.DecksRepository
import com.soroh.intermind.core.domain.entity.Deck
import com.soroh.intermind.feature.addeditdeck.api.AddEditDeckNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel(assistedFactory = AddEditDeckViewModel.Factory::class)
class AddEditDeckViewModel @AssistedInject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val decksRepository: DecksRepository,
    @Assisted val key: AddEditDeckNavKey,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditDeckUiState())
    val uiState: StateFlow<AddEditDeckUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddEditDeckEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        key.deckId?.let { loadDeck(it) }
    }

    private fun loadDeck(deckId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val deck = decksRepository.getDeckById(deckId)
                if (deck != null) {
                    _uiState.update {
                        it.copy(
                            name = deck.name,
                            isPublic = deck.isPublic,
                            originalDeck = deck,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.emit(AddEditDeckEvent.ShowError("Колода не найдена"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.emit(AddEditDeckEvent.ShowError(e.message ?: "Ошибка загрузки"))
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onPublicChanged(isPublic: Boolean) {
        _uiState.update { it.copy(isPublic = isPublic) }
    }

    fun saveDeck() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Название не может быть пустым") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaveButtonEnabled = false, isLoading = true) }
            try {
                if (key.deckId == null) {
                    val newDeck = Deck(
                        id = UUID.randomUUID().toString(),
                        name = currentState.name.trim(),
                        isPublic = currentState.isPublic,
                        // Устанавливаем дефолтные значения для новой колоды
                        cardsCount = 0,
                        likes = 0,
                        trainings = 0
                    )
                    decksRepository.insertDeck(newDeck)
                } else {
                    val original = currentState.originalDeck ?: return@launch
                    val updatedDeck = original.copy(
                        name = currentState.name.trim(),
                        isPublic = currentState.isPublic
                    )
                    decksRepository.updateDeck(updatedDeck)
                }

                // Успешно сохранили - отправляем ивент для навигации
                _uiEvent.emit(AddEditDeckEvent.DeckSaved)

            } catch (e: Exception) {
                _uiState.update { it.copy(isSaveButtonEnabled = true, isLoading = false) }
                _uiEvent.emit(AddEditDeckEvent.ShowError(e.message ?: "Ошибка при сохранении"))
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(key: AddEditDeckNavKey): AddEditDeckViewModel
    }
}

data class AddEditDeckUiState(
    val name: String = "",
    val isPublic: Boolean = false,
    val nameError: String? = null,
    val isSaveButtonEnabled: Boolean = true,
    val isLoading: Boolean = false,

    val originalDeck: Deck? = null
)

sealed interface AddEditDeckEvent {
    data object DeckSaved : AddEditDeckEvent
    data class ShowError(val message: String) : AddEditDeckEvent
}