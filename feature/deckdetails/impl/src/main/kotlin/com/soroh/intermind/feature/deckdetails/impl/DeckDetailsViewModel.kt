package com.soroh.intermind.feature.deckdetails.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.repository.DecksRepository
import com.soroh.intermind.core.domain.entity.Card
import com.soroh.intermind.core.domain.entity.Deck
import com.soroh.intermind.feature.deckdetails.api.navigation.DeckDetailsNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DeckDetailsViewModel.Factory::class)
class DeckDetailsViewModel @AssistedInject constructor(
    private val decksRepository: DecksRepository,
    @Assisted val key: DeckDetailsNavKey,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeckDetailUiState>(DeckDetailUiState.Loading)
    val uiState: StateFlow<DeckDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DeckDetailsEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadDeck(key.deckId)
    }

    private fun loadDeck(deckId: String) {
        viewModelScope.launch {
            try {
                val deck = decksRepository.getDeckById(deckId)
                if (deck != null) {
                    val isOwner = decksRepository.isDeckOwner(deckId)

                    decksRepository.getCardsForDeck(deckId).collect { cardsList ->
                        val currentState = _uiState.value

                        if (currentState is DeckDetailUiState.Success) {
                            _uiState.value = currentState.copy(cards = cardsList)
                        } else {
                            _uiState.value = DeckDetailUiState.Success(
                                deck = deck,
                                cards = cardsList,
                                nextTrainingTime = null,
                                isOwner = isOwner
                            )
                        }
                    }
                } else {
                    _uiState.value = DeckDetailUiState.Error("Колода не найдена")
                }
            } catch (e: Exception) {
                _uiState.value = DeckDetailUiState.Error(e.localizedMessage ?: "Неизвестная ошибка")
            }
        }
    }

    fun changePrivacy() {
        val currentState = _uiState.value
        if (currentState !is DeckDetailUiState.Success) return
        viewModelScope.launch {
            try {
                val updatedDeck = currentState.deck.copy(isPublic = !currentState.deck.isPublic)
                decksRepository.updateDeck(updatedDeck)
                _uiState.value = currentState.copy(deck = updatedDeck)
            } catch (e: Exception) {
                _uiEvent.emit(DeckDetailsEvent.ShowError("Ошибка при смене приватности"))
            }
        }
    }

     fun deleteDeck() {
        viewModelScope.launch {
            try {
                decksRepository.deleteDeck(key.deckId)
            } catch (e: Exception) {
                _uiEvent.emit(DeckDetailsEvent.ShowError("Не удалось удалить колоду"))
            }
        }
    }

    fun refreshCards() {
        loadDeck(key.deckId)
    }

    @AssistedFactory
    interface Factory {
        fun create(key: DeckDetailsNavKey): DeckDetailsViewModel
    }
}

sealed interface DeckDetailUiState {

    data object Loading : DeckDetailUiState

    data class Error(val message: String? = null) : DeckDetailUiState

    data class Success(
        val deck: Deck,
        val cards: List<Card>,
        val nextTrainingTime: Long?,
        val isOwner: Boolean
    ) : DeckDetailUiState
}

sealed interface DeckDetailsEvent {
    data class ShowError(val message: String) : DeckDetailsEvent
}


