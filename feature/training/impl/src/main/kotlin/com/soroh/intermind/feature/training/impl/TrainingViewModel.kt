package com.soroh.intermind.feature.training.impl

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.model.ModeStat
import com.soroh.intermind.core.data.model.ObjectiveResult
import com.soroh.intermind.core.data.model.SessionStatistics
import com.soroh.intermind.core.data.model.UserCardProgress
import com.soroh.intermind.core.data.repository.TrainingItem
import com.soroh.intermind.core.data.repository.TrainingRepository
import com.soroh.intermind.core.domain.entity.TestType
import com.soroh.intermind.core.domain.entity.TrainingCard
import com.soroh.intermind.feature.training.api.navigation.TrainingNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@HiltViewModel(assistedFactory = TrainingViewModel.Factory::class)
class TrainingViewModel @AssistedInject constructor(
    private val trainingRepository: TrainingRepository,
    @Assisted private val key: TrainingNavKey,
) : ViewModel() {

    private val _state = MutableStateFlow<TrainingScreenState>(TrainingScreenState.Initial)
    val state: StateFlow<TrainingScreenState> = _state.asStateFlow()

    private var cards: List<TrainingItem> = emptyList()
    private var currentIndex = 0
    private var startTime = 0L
    private var cardStartTime = 0L
    private var correctCount = 0

    // Статистика по режимам для сохранения сессии
    private val modeStats = mutableMapOf<TestType, ModeStat>()

    // Предзагрузка картинок
    private val preloadedPictures = mutableMapOf<String, Uri?>()

    // Лимит новых карточек (можно получить из настроек пользователя)
    private val dailyNewCardsLimit = 20

    // Режимы тестирования (пока все)
    private val enabledModes = setOf(TestType.CHOICE, TestType.INPUT, TestType.TRUE_FALSE)

    init {
        loadCards()
    }

    fun loadCards() {
        viewModelScope.launch {
            _state.value = TrainingScreenState.Loading

            trainingRepository.prepareTrainingCards(
                deckId = key.deckId,
                dailyLimit = dailyNewCardsLimit,
                modes = enabledModes
            ).onSuccess { trainingItems ->
                if (trainingItems.isEmpty()) {
                    _state.value = TrainingScreenState.Empty(key.deckId)
                    return@launch
                }

                cards = trainingItems
                currentIndex = 0
                correctCount = 0
                startTime = System.currentTimeMillis()
                modeStats.clear()

                // Предзагружаем картинки для всех карточек
                preloadPictures()

                showCurrentCard()
            }.onFailure { error ->
                _state.value = TrainingScreenState.Error(
                    error.message ?: "Failed to load training cards"
                )
            }
        }
    }

    private fun preloadPictures() {
        cards.forEach { item ->
            val attachment = item.trainingCard.attachment
            if (attachment != null) {
                preloadedPictures[item.trainingCard.id] = attachment.toUri()
            }
        }
    }

    private fun showCurrentCard() {
        if (currentIndex >= cards.size) {
            finishTraining()
            return
        }

        val item = cards[currentIndex]
        cardStartTime = System.currentTimeMillis()

        _state.value = TrainingScreenState.InProgress(
            currentCard = item.trainingCard,
            currentProgress = item.progress,
            cardNumber = currentIndex + 1,
            totalCards = cards.size,
            isAnswerRevealed = false,
            selectedAnswer = null,
            isCorrect = null,
            preloadedPicture = preloadedPictures[item.trainingCard.id]
        )
    }

    fun selectAnswer(answer: String) {
        val currentState = _state.value as? TrainingScreenState.InProgress ?: return
        if (currentState.isAnswerRevealed) return

        val correct = answer == currentState.currentCard.answer
        processAnswer(correct, answer)
    }

    fun submitTextAnswer(text: String) {
        val currentState = _state.value as? TrainingScreenState.InProgress ?: return
        if (currentState.isAnswerRevealed) return

        val currentCard = currentState.currentCard

        if (currentCard.testType == TestType.INPUT) {
            viewModelScope.launch {
                val similarity = trainingRepository.checkFillInTheBlankAnswer(
                    userInput = text,
                    correctWords = currentCard.missingWords
                )

                val isCorrect = similarity >= 0.85
                processAnswer(isCorrect, text, accuracy = similarity)
            }
        }
    }

    fun revealAnswer() {
        val currentState = _state.value as? TrainingScreenState.InProgress ?: return
        if (currentState.isAnswerRevealed) return

        // "Не знаю" - неверный ответ
        processAnswer(isCorrect = false, userAnswer = null, revealed = true)
    }

    private fun processAnswer(
        isCorrect: Boolean,
        userAnswer: String?,
        accuracy: Double = if (isCorrect) 1.0 else 0.0,
        revealed: Boolean = false
    ) {
        val currentState = _state.value as? TrainingScreenState.InProgress ?: return
        val item = cards[currentIndex]
        val responseTime = System.currentTimeMillis() - cardStartTime

        // Обновляем счётчик правильных ответов
        if (isCorrect) {
            correctCount++
        }

        // Обновляем статистику по режимам
        val testType = currentState.currentCard.testType
        val currentModeStat = modeStats.getOrDefault(testType, ModeStat(0, 0))
        modeStats[testType] = ModeStat(
            correct = currentModeStat.correct + if (isCorrect) 1 else 0,
            total = currentModeStat.total + 1
        )

        // Сохраняем прогресс в FSRS
        viewModelScope.launch {
            val result = ObjectiveResult(
                accuracy = accuracy,
                responseTimeMs = responseTime,
                testType = testType
            )

            trainingRepository.processCardAnswer(
                currentProgress = item.progress,
                result = result
            ).onFailure {
                Log.e("!@#", it.toString())
            }
        }

        // Обновляем UI
        _state.update { state ->
            (state as TrainingScreenState.InProgress).copy(
                isAnswerRevealed = true,
                selectedAnswer = userAnswer,
                isCorrect = isCorrect
            )
        }
    }

    fun nextCard() {
        currentIndex++
        showCurrentCard()
    }

    fun finishTraining() {
        val durationSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()

        viewModelScope.launch {
            val stats = SessionStatistics(
                deckId = key.deckId,
                durationSec = durationSec,
                totalCards = cards.size,
                correctCount = correctCount,
                modesStat = modeStats.mapKeys { it.key.name }
            )

            trainingRepository.saveSessionResult(stats).onSuccess {
                Log.d("!@#", it.toString())
            }.onFailure {
                Log.e("!@#", it.toString())

            }
        }

        _state.value = TrainingScreenState.Finished(
            totalCards = cards.size,
            correctCount = correctCount,
            durationSec = durationSec,
            deckId = key.deckId
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(key: TrainingNavKey): TrainingViewModel
    }
}

sealed interface TrainingScreenState {

    data object Initial : TrainingScreenState

    data object Loading : TrainingScreenState

    data class Empty(val deckId: String) : TrainingScreenState

    data class InProgress(
        val currentCard: TrainingCard,
        val currentProgress: UserCardProgress,
        val cardNumber: Int,
        val totalCards: Int,
        val isAnswerRevealed: Boolean = false,
        val selectedAnswer: String? = null,
        val isCorrect: Boolean? = null,
        val preloadedPicture: Uri? = null,
    ) : TrainingScreenState

    data class Finished(
        val totalCards: Int,
        val correctCount: Int,
        val durationSec: Int,
        val deckId: String,
    ) : TrainingScreenState

    data class Error(val message: String) : TrainingScreenState
}
