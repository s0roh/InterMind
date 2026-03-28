package com.soroh.intermind.core.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.soroh.intermind.core.data.dto.DeckDto
import com.soroh.intermind.core.domain.entity.Deck
import com.soroh.intermind.core.ui.model.DeckUiModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class ExplorePagingSource(
    private val supabase: SupabaseClient,
    private val query: String? = null,
    private val sortBy: String? = null,
    private val category: String? = null,
) : PagingSource<Int, DeckUiModel>() {

    private val decksTable
        get() = supabase.postgrest["decks"]
    private val userFavouritesTable
        get() = supabase.postgrest["user_favourites"]

    override fun getRefreshKey(state: PagingState<Int, DeckUiModel>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DeckUiModel> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        val from = page * pageSize.toLong()
        val to = from + pageSize - 1

        return try {
            val userId = getCurrentUserId()
                ?: throw IllegalStateException("User must be logged in to create a deck")

            val showOnlyFavourites = category == "favourite"

            val favouriteIds = userFavouritesTable
                .select(columns = Columns.list("deck_id")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<FavouriteDto>()
                .map { it.deckId }
                .toSet()

            val decks = if (showOnlyFavourites) {
                userFavouritesTable
                    .select(columns = Columns.list("deck_id, decks!inner(*)")) {
                        filter {
                            eq("user_id", userId)
                            if (!query.isNullOrBlank()) {
                                ilike("decks.name", "%$query%")
                            }
                        }
                        val sortColumn = when (sortBy) {
                            "likes" -> "decks.likes"
                            "trainings" -> "decks.trainings"
                            else -> "decks.created_at"
                        }
                        order(sortColumn, Order.DESCENDING)
                        range(from, to)
                    }
                    .decodeList<FavouriteWithDeckDto>()
                    .map { it.deck }
            } else {
                decksTable.select {
                    filter {
                        eq("is_public", true)
                        if (!query.isNullOrBlank()) {
                            ilike("name", "%$query%")
                        }
                    }
                    val sortColumn = when (sortBy) {
                        "likes" -> "likes"
                        "trainings" -> "trainings"
                        else -> "created_at"
                    }
                    order(sortColumn, Order.DESCENDING)
                    range(from, to)
                }.decodeList<DeckDto>()
            }

            val uiModels = decks.map { deckDto ->
                deckDto.toDomain().toUiModel(
                    isLiked = favouriteIds.contains(deckDto.id)
                )
            }

            LoadResult.Page(
                data = uiModels,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (uiModels.size < pageSize) null else page + 1
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getCurrentUserId(): String? {
        return supabase.auth.sessionManager.loadSession()?.user?.id
    }
}

fun Deck.toUiModel(
    isLiked: Boolean,
): DeckUiModel {
    return DeckUiModel(
        id = id,
        name = name,
        isPublic = isPublic,
        isLiked = isLiked,
        cardsCount = cardsCount,
        likes = likes,
        trainings = trainings
    )
}

@Serializable
internal data class FavouriteDto(
    @SerialName("deck_id") val deckId: String
)

@Serializable
internal data class FavouriteWithDeckDto(
    @SerialName("decks") val deck: DeckDto
)