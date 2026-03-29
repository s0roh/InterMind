package com.soroh.intermind.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.soroh.intermind.core.data.dto.DeckDto
import com.soroh.intermind.core.ui.model.DeckUiModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implements a [ExploreRepository]
 */
class ExploreRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ExploreRepository {

    private val decksTable
        get() = supabase.postgrest["decks"]
    private val userFavouritesTable
        get() = supabase.postgrest["user_favourites"]


    private fun getCurrentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    override fun getPublicDecks(
        sortBy: String?,
        category: String?
    ): Flow<PagingData<DeckUiModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = ENABLE_PLACEHOLDERS,
                prefetchDistance = PREFETCH_DISTANCE,
                initialLoadSize = INITIAL_LOAD_SIZE
            ),
            pagingSourceFactory = {
                ExplorePagingSource(
                    supabase = supabase,
                    sortBy = sortBy,
                    category = category
                )
            }
        ).flow
    }

    override suspend fun searchPublicDecks(query: String): List<DeckUiModel> {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User must be logged in to create a deck")

        val decks = decksTable.select {
            filter {
                eq("is_public", true)
                if (query.isNotBlank()) {
                    ilike("name", "%$query%")
                }
            }
            order("created_at", Order.DESCENDING)
        }.decodeList<DeckDto>()

        val favouriteIds = userFavouritesTable
            .select(columns = Columns.list("deck_id")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<FavouriteDto>()
            .map { it.deckId }
            .toSet()

        return decks.map { deckDto ->
            deckDto.toDomain().toUiModel(
                isLiked = favouriteIds.contains(deckDto.id)
            )
        }
    }

    override suspend fun likeDeck(deckId: String) {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User must be logged in to create a deck")

        userFavouritesTable.insert(
            mapOf(
                "user_id" to userId,
                "deck_id" to deckId
            )
        )
    }

    override suspend fun unlikeDeck(deckId: String) {
        val userId = getCurrentUserId() ?: throw IllegalStateException("User must be logged in to remove favourite")
        userFavouritesTable.delete {
            filter {
                eq("user_id", userId)
                eq("deck_id", deckId)
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 10
        const val ENABLE_PLACEHOLDERS = false
        const val PREFETCH_DISTANCE = 7
        const val INITIAL_LOAD_SIZE = 10
    }
}