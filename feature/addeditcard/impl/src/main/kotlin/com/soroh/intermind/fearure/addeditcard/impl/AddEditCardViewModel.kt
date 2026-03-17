package com.soroh.intermind.fearure.addeditcard.impl

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.repository.DecksRepository
import com.soroh.intermind.core.domain.entity.Card
import com.soroh.intermind.fearure.addeditcard.api.navigation.AddEditCardNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import java.util.UUID

@HiltViewModel(assistedFactory = AddEditCardViewModel.Factory::class)
class AddEditCardViewModel @AssistedInject constructor(
    private val decksRepository: DecksRepository,
    @Assisted val key: AddEditCardNavKey,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditCardUiState())
    val uiState: StateFlow<AddEditCardUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddEditCardEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var originalCardPictureUri: Uri? = null
    private var currentCard: Card? = null
    private var originalPicturePath: String? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            _uiEvent.emit(AddEditCardEvent.ShowError("Ошибка: ${throwable.message}"))
            _uiState.update { it.copy(isSaveButtonEnabled = true, isLoading = false) }
        }
    }

    init {
        key.cardId?.let { loadCard(it) }
    }

    private fun loadCard(cardId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val card = decksRepository.getCardById(cardId)
                if (card != null) {
                    currentCard = card

                    originalPicturePath = card.picturePath

                    val pictureUri = card.attachment?.toUri()
                        ?: if (!card.picturePath.isNullOrBlank()) {
                            decksRepository.getCardPicture(key.deckId, cardId)
                        } else null

                    originalCardPictureUri = pictureUri

                    _uiState.update {
                        it.copy(
                            question = card.question,
                            answer = card.answer,
                            wrongAnswerList = card.wrongAnswers,
                            attachment = card.attachment,
                            picturePath = card.picturePath,
                            cardPictureUri = pictureUri,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.emit(AddEditCardEvent.ShowError("Карточка не найдена"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.emit(AddEditCardEvent.ShowError(e.message ?: "Ошибка загрузки"))
            }
        }
    }

    fun onQuestionChanged(question: String) {
        _uiState.update {
            it.copy(
                question = question,
                questionError = null
            )
        }
        updateSaveButtonState()
    }

    fun onAnswerChanged(answer: String) {
        _uiState.update {
            it.copy(
                answer = answer,
                answerError = null
            )
        }
        updateSaveButtonState()
    }

    fun addWrongAnswerField() {
        _uiState.update { currentState ->
            if (currentState.wrongAnswerList.size < 3) {
                currentState.copy(
                    wrongAnswerList = currentState.wrongAnswerList + ""
                )
            } else {
                currentState
            }
        }
    }

    fun updateWrongAnswer(index: Int, value: String) {
        _uiState.update { currentState ->
            val updatedList = currentState.wrongAnswerList.toMutableList()
            if (index in updatedList.indices) {
                updatedList[index] = value
                currentState.copy(wrongAnswerList = updatedList)
            } else {
                currentState
            }
        }
    }

    fun removeWrongAnswer(index: Int) {
        _uiState.update { currentState ->
            val updatedList = currentState.wrongAnswerList.toMutableList()
            if (index in updatedList.indices) {
                updatedList.removeAt(index)
                currentState.copy(wrongAnswerList = updatedList)
            } else {
                currentState
            }
        }
    }

    fun setCardPicture(uri: String) {
        _uiState.update { it.copy(cardPictureUri = uri.toUri()) }
    }

    fun deleteCardPicture() {
        _uiState.update { it.copy(cardPictureUri = null) }
    }

    fun saveCard() {

        val currentState = _uiState.value

        if (currentState.question.isBlank() || currentState.answer.isBlank()) {
            _uiState.update {
                it.copy(
                    questionError = if (currentState.question.isBlank()) "Вопрос не может быть пустым" else null,
                    answerError = if (currentState.answer.isBlank()) "Ответ не может быть пустым" else null
                )
            }
            return
        }

        val trimmedAnswer = currentState.answer.trim()
        val trimmedWrongAnswers = currentState.wrongAnswerList.filter { it.isNotBlank() }.map { it.trim() }

        if (trimmedWrongAnswers.contains(trimmedAnswer)) {
            viewModelScope.launch { _uiEvent.emit(AddEditCardEvent.ShowError("Неправильный ответ не должен совпадать с правильным")) }
            return
        }

        if (trimmedWrongAnswers.size != trimmedWrongAnswers.toSet().size) {
            viewModelScope.launch { _uiEvent.emit(AddEditCardEvent.ShowError("Неправильные ответы не должны повторяться")) }
            return
        }

        _uiState.update { it.copy(isSaveButtonEnabled = false, isLoading = true) }

        viewModelScope.launch(exceptionHandler) {
            try {
                val cardId = key.cardId ?: UUID.randomUUID().toString()
                val deckId = key.deckId

                val picturePath = when {
                    key.cardId == null && currentState.cardPictureUri != null -> null
                    key.cardId != null -> originalPicturePath
                    else -> null
                }

                val card = Card(
                    id = cardId,
                    question = normalizeSpaces(currentState.question),
                    answer = normalizeSpaces(currentState.answer),
                    wrongAnswers = trimmedWrongAnswers,
                    attachment = currentState.attachment,
                    picturePath = picturePath
                )

                if (key.cardId == null) {
                    decksRepository.insertCard(card, deckId)
                } else {
                    decksRepository.updateCard(card)
                }

                handleCardPicture(cardId, deckId)

                _uiEvent.emit(AddEditCardEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvent.emit(AddEditCardEvent.ShowError(e.message ?: "Ошибка сохранения"))
                _uiState.update { it.copy(isSaveButtonEnabled = true, isLoading = false) }
            }
        }

    }

    private suspend fun handleCardPicture(cardId: String, deckId: String) {
        val currentState = _uiState.value

        when {
            currentState.cardPictureUri == null && originalCardPictureUri != null -> {
                decksRepository.deleteCardPicture(deckId, cardId)

                val cardWithoutPicture = decksRepository.getCardById(cardId)?.copy(
                    picturePath = null,
                    attachment = null
                )
                if (cardWithoutPicture != null) {
                    decksRepository.updateCard(cardWithoutPicture)
                }
            }
            currentState.cardPictureUri != null && currentState.cardPictureUri != originalCardPictureUri -> {
                decksRepository.updateCardPicture(
                    deckId = deckId,
                    cardId = cardId,
                    pictureUri = currentState.cardPictureUri
                )
            }
        }
    }

    private fun updateSaveButtonState() {
        val currentState = _uiState.value
        _uiState.update {
            it.copy(
                isSaveButtonEnabled = currentState.question.isNotBlank() &&
                        currentState.answer.isNotBlank()
            )
        }
    }

    private fun normalizeSpaces(text: String): String = text.replace(Regex("\\s+"), " ").trim()

    @AssistedFactory
    interface Factory {
        fun create(key: AddEditCardNavKey): AddEditCardViewModel
    }
}

data class AddEditCardUiState(
    val question: String = "",
    val answer: String = "",
    val questionError: String? = null,
    val answerError: String? = null,
    val cardPictureUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSaveButtonEnabled: Boolean = true,
    val wrongAnswerList: List<String> = emptyList(),
    val attachment: String? = null,
    val picturePath: String? = null,
)

sealed interface AddEditCardEvent {
    data class ShowError(val message: String) : AddEditCardEvent
    data object NavigateBack : AddEditCardEvent
}