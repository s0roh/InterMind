package com.soroh.intermind.feature.deckdetails.impl

import android.util.Log
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel(assistedFactory = DeckDetailsViewModel.Factory::class)
class DeckDetailsViewModel @AssistedInject constructor(
    private val decksRepository: DecksRepository,
    @Assisted val key: DeckDetailsNavKey,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeckDetailUiState>(DeckDetailUiState.Loading)
    val uiState: StateFlow<DeckDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DeckDetailsEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var loadJob: Job? = null

    init {
        loadDeck(key.deckId)
    }

    private fun loadDeck(deckId: String) {
        Log.d("LoadDeck", "loadDeck called for deckId: $deckId")
        //loadJob?.cancel()
       viewModelScope.launch {
            try {
                Log.d("LoadDeck", "Starting coroutine")
                _uiState.value = DeckDetailUiState.Loading

                Log.d("LoadDeck", "Getting deck...")
                val deck = decksRepository.getDeckById(deckId)
                Log.d("LoadDeck", "Deck received: ${deck?.id}")
                if (deck != null) {
                    Log.d("LoadDeck", "Checking isOwner...")
                    val isOwner = decksRepository.isDeckOwner(deckId)
                    Log.d("LoadDeck", "isOwner: $isOwner")

                    Log.d("LoadDeck", "Getting cards for deck...")
                    decksRepository.getCardsForDeck(deckId).collect { cardsList ->
                        Log.d("LoadDeck", "Cards collected: ${cardsList.size} cards")
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
                    Log.d("LoadDeck", "Finished collecting (should not reach here if flow infinite)")
                } else {
                    Log.e("LoadDeck", "Deck not found")
                    _uiState.value = DeckDetailUiState.Error("Колода не найдена")
                }
            } catch (e: CancellationException) {
                Log.d("LoadDeck", "Job cancelled")
                throw e
            } catch (e: Exception) {
                Log.e("LoadDeck", "Error loading deck: ${e.message}", e)
                _uiState.value = DeckDetailUiState.Error(e.localizedMessage ?: "Неизвестная ошибка")
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
        //loadDeck(key.deckId)
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            try {
                decksRepository.deleteCard(card)
                refreshCards()
            } catch (e: Exception) {
                _uiEvent.emit(DeckDetailsEvent.ShowError("Не удалось удалить карточку"))
            }
        }
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