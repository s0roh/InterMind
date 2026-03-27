package com.soroh.intermind.feature.decks.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.repository.DecksRepository
import com.soroh.intermind.core.domain.entity.Deck
import com.soroh.intermind.core.ui.model.DeckUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DecksViewModel @Inject constructor(
    private val decksRepository: DecksRepository
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val decks: StateFlow<List<DeckUiModel>> = combine(
        decksRepository.getDecks(),
        decksRepository.getDecksTrainingStats(dailyLimit = 20)
    ) { domainDecks, statsMap ->
        domainDecks.map { deck ->
            val stats = statsMap[deck.id]
            deck.toUiModel(
                newCount = stats?.newCount ?: 0,
                reviewCount = stats?.reviewCount ?: 0
            )
        }
    }.flowOn(Dispatchers.Default)
        .onStart { _isRefreshing.value = true }
        .onEach { _isRefreshing.value = false }
        .catch { _ ->
            _isRefreshing.value = false
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

//    val decks: StateFlow<List<DeckUiModel>> = decksRepository.getDecks()
//        .map { domainList ->
//            domainList.map { it.toUiModel() }
//        }
//        .flowOn(Dispatchers.Default)
//        .onStart { _isRefreshing.value = true }
//        .onEach { _isRefreshing.value = false }
//        .catch { _ ->
//            _isRefreshing.value = false
//        }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = emptyList()
//        )

    fun refreshDecks() {
        viewModelScope.launch {
            decksRepository.refreshDecks()
        }
    }
}

fun Deck.toUiModel(newCount: Int, reviewCount: Int): DeckUiModel {
    return DeckUiModel(
        id = id,
        name = name,
        isPublic = isPublic,
        isLiked = false,
        cardsCount = cardsCount,
        likes = likes,
        trainings = trainings,
        newCardsCount = newCount,
        reviewCardsCount = reviewCount
    )
}