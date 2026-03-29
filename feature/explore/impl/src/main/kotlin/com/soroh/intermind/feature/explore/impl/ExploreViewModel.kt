package com.soroh.intermind.feature.explore.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.soroh.intermind.core.data.repository.ExploreRepository
import com.soroh.intermind.core.ui.model.DeckUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository
) : ViewModel() {

    private val filtersFlow = MutableStateFlow(PublicDeckFilters(sortBy = "likes"))

    private val _pagingDataFlow = MutableStateFlow<PagingData<DeckUiModel>>(PagingData.empty())
    val decksFlow = _pagingDataFlow.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DeckUiModel>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    init {
        viewModelScope.launch {
            filtersFlow.flatMapLatest { filters ->
                exploreRepository.getPublicDecks(
                    sortBy = filters.sortBy,
                    category = filters.category
                )
            }.cachedIn(viewModelScope).collect {
                _pagingDataFlow.value = it
            }
        }
    }

    private val _state = MutableStateFlow(PublicDecksScreenState())
    val state = _state.asStateFlow()

    fun toggleLike(deck: DeckUiModel) {
        _pagingDataFlow.value = _pagingDataFlow.value.map { item ->
            if (item.id == deck.id) {
                item.copy(
                    isLiked = !deck.isLiked,
                    likes = if (deck.isLiked) deck.likes - 1 else deck.likes + 1
                )
            } else item
        }

        _searchResults.update { currentList ->
            currentList.map { item ->
                if (item.id == deck.id) {
                    item.copy(
                        isLiked = !deck.isLiked,
                        likes = if (deck.isLiked) deck.likes - 1 else deck.likes + 1
                    )
                } else item
            }
        }

        viewModelScope.launch {
            if (deck.isLiked) exploreRepository.unlikeDeck(deck.id)
            else exploreRepository.likeDeck(deck.id)

        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(query = query) }

        if (query.isBlank()) {
            _isSearching.value = false
            _searchResults.value = emptyList()
            return
        }

        _isSearching.value = true

        viewModelScope.launch {
            try {
                val results = exploreRepository.searchPublicDecks(query)
                _searchResults.value = results
            } catch (_: Exception) {
                _searchResults.value = emptyList()
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
    val sortBy: String? = null,
    val category: String? = null
)