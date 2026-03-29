package com.soroh.intermind.core.data.repository

import androidx.paging.PagingData
import com.soroh.intermind.core.ui.model.DeckUiModel
import kotlinx.coroutines.flow.Flow

/**
 * Data layer interface for the explore feature.
 */
interface ExploreRepository {

    fun getPublicDecks(
        sortBy: String? = null,
        category: String? = null,
    ): Flow<PagingData<DeckUiModel>>

    suspend fun searchPublicDecks(query: String): List<DeckUiModel>

    suspend fun likeDeck(deckId: String)

    suspend fun unlikeDeck(deckId: String)
}