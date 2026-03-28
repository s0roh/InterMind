package com.soroh.intermind.feature.explore.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.soroh.intermind.core.data.repository.ExploreRepository
import com.soroh.intermind.core.ui.model.DeckUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.copy

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository
) : ViewModel() {

    private val filtersFlow = MutableStateFlow(PublicDeckFilters())
    private var searchJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val decksFlow: Flow<PagingData<DeckUiModel>> = filtersFlow
        .flatMapLatest { filters ->
            exploreRepository.getPublicDecks(
                query = filters.query,
                sortBy = filters.sortBy,
                category = filters.category
            )
        }.cachedIn(viewModelScope)

    private val _state = MutableStateFlow(PublicDecksScreenState())
    val state = _state.asStateFlow()

    fun toggleLike(deck: DeckUiModel) {
        viewModelScope.launch {
            if (deck.isLiked) {
                exploreRepository.unlikeDeck(deck.id)
            } else {
                exploreRepository.likeDeck(deck.id)
            }

            // Вариант 1: Просто обновить список целиком (самый надежный для Paging)
            // Это заставит PagingSource заново загрузить данные с актуальными лайками
            filtersFlow.value = filtersFlow.value.copy()
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(query = query) }

        if (query.isBlank()) {
            //searchJob?.cancel()
            filtersFlow.update { it.copy(query = null) }
        } else {
            //searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(500)
                filtersFlow.update { it.copy(query = query) }
            }
        }
    }

    fun updateSortType(sortType: SortType) {
        _state.update { it.copy(sortType = sortType) }
        filtersFlow.update { it.copy(sortBy = sortType.value) }
    }

    fun updateCategory(category: DeckCategory) {
        _state.update { it.copy(category = category) }
        filtersFlow.update { it.copy(category = category.value) }
    }

}

data class PublicDecksScreenState(
    val decks: Flow<PagingData<DeckUiModel>>? = null,
    val sortType: SortType = SortType.LIKES,
    val category: DeckCategory = DeckCategory.ALL,
    val query: String = ""
)

enum class SortType(val value: String) {
    LIKES("likes"),
    TRAININGS("trainings")
}

enum class DeckCategory(val value: String?) {
    ALL(null),
    LIKED("favourite")
}

data class PublicDeckFilters(
    val query: String? = null,
    val sortBy: String? = null,
    val category: String? = null
)