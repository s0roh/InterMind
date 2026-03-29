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
import kotlin.coroutines.cancellation.CancellationException

internal class ExplorePagingSource(
    private val supabase: SupabaseClient,
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
        val from = page * params.loadSize.toLong()
        val to = from + params.loadSize - 1

        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User must be logged in")

            val showOnlyFavourites = category == "favourite"

            val mainSort = when (sortBy) {
                "likes" -> "likes"
                "trainings" -> "trainings"
                else -> "created_at"
            }

            val decks: List<DeckDto>
            val favouriteIds: Set<String>

            if (showOnlyFavourites) {
                decks = decksTable.select(
                    columns = Columns.raw("*, user_favourites!inner(user_id)")
                ) {
                    filter { eq("user_favourites.user_id", userId) }
                    order(mainSort, Order.DESCENDING)
                    order("created_at", Order.DESCENDING)
                    order("id", Order.ASCENDING)   // стабильность
                    range(from = from, to = to)
                }.decodeList<DeckDto>()
                favouriteIds = decks.mapNotNull { it.id }.toSet()
            } else {
                decks = decksTable.select {
                    filter { eq("is_public", true) }
                    order(mainSort, Order.DESCENDING)
                    order("created_at", Order.DESCENDING)
                    order("id", Order.ASCENDING)
                    range(from = from, to = to)
                }.decodeList<DeckDto>()

                favouriteIds = if (decks.isNotEmpty()) {
                    val deckIds = decks.mapNotNull { it.id }
                    userFavouritesTable.select(columns = Columns.list("deck_id")) {
                        filter {
                            eq("user_id", userId)
                            isIn("deck_id", deckIds)
                        }
                    }.decodeList<FavouriteDto>().map { it.deckId }.toSet()
                } else emptySet()
            }

            val ids = decks.mapNotNull { it.id }
            require(ids.size == ids.toSet().size) { "Duplicate deck ids in page $page: $ids" }

            val uiModels = decks.map { deckDto ->
                deckDto.toDomain().toUiModel(isLiked = favouriteIds.contains(deckDto.id))
            }

            LoadResult.Page(
                data = uiModels,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (uiModels.size < params.loadSize) null else page + 1
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            LoadResult.Error(e)
        }
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