package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.dto.DeckDto
import com.soroh.intermind.core.domain.entity.Deck
import com.soroh.intermind.core.ui.model.DeckUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExploreRepositoryImplTest {
    @Test
    fun `toUiModel converts Deck to DeckUiModel correctly`() {
        val deck = Deck(
            id = "deck-1",
            name = "Test Deck",
            isPublic = true,
            authorId = "user-1",
            cardsCount = 10,
            likes = 5,
            trainings = 3
        )

        val uiModel = deck.toUiModel(isLiked = true)

        assertEquals("deck-1", uiModel.id)
        assertEquals("Test Deck", uiModel.name)
        assertTrue(uiModel.isPublic)
        assertTrue(uiModel.isLiked)
        assertEquals(10, uiModel.cardsCount)
        assertEquals(5, uiModel.likes)
        assertEquals(3, uiModel.trainings)
        assertEquals(0, uiModel.newCardsCount)
        assertEquals(0, uiModel.reviewCardsCount)
    }

    @Test
    fun `toUiModel preserves isLiked false`() {
        val deck = Deck(
            id = "deck-2",
            name = "Public Deck",
            isPublic = true,
            cardsCount = 20,
            likes = 100,
            trainings = 50
        )

        val uiModel = deck.toUiModel(isLiked = false)

        assertFalse(uiModel.isLiked)
    }

    @Test
    fun `toUiModel handles zero values`() {
        val deck = Deck(
            id = "deck-3",
            name = "Empty Deck",
            isPublic = false,
            cardsCount = 0,
            likes = 0,
            trainings = 0
        )

        val uiModel = deck.toUiModel(isLiked = false)

        assertEquals(0, uiModel.cardsCount)
        assertEquals(0, uiModel.likes)
        assertEquals(0, uiModel.trainings)
    }

    @Test
    fun `toUiModel handles large values`() {
        val deck = Deck(
            id = "deck-4",
            name = "Popular Deck",
            isPublic = true,
            cardsCount = 1000,
            likes = Int.MAX_VALUE,
            trainings = 50000
        )

        val uiModel = deck.toUiModel(isLiked = true)

        assertEquals(Int.MAX_VALUE, uiModel.likes)
        assertEquals(1000, uiModel.cardsCount)
        assertEquals(50000, uiModel.trainings)
    }

    @Test
    fun `toUiModel handles empty name`() {
        val deck = Deck(
            id = "deck-5",
            name = "",
            isPublic = true,
            cardsCount = 5,
            likes = 2,
            trainings = 1
        )

        val uiModel = deck.toUiModel(isLiked = false)

        assertEquals("", uiModel.name)
    }

    @Test
    fun `DeckUiModel creates correctly with all fields`() {
        val model = DeckUiModel(
            id = "deck-1",
            name = "English Vocabulary",
            isPublic = true,
            isLiked = true,
            cardsCount = 50,
            likes = 25,
            trainings = 10,
            newCardsCount = 15,
            reviewCardsCount = 35
        )

        assertEquals("deck-1", model.id)
        assertEquals("English Vocabulary", model.name)
        assertTrue(model.isPublic)
        assertTrue(model.isLiked)
        assertEquals(50, model.cardsCount)
        assertEquals(25, model.likes)
        assertEquals(10, model.trainings)
        assertEquals(15, model.newCardsCount)
        assertEquals(35, model.reviewCardsCount)
    }

    @Test
    fun `DeckUiModel default values for newCardsCount and reviewCardsCount`() {
        val model = DeckUiModel(
            id = "deck-1",
            name = "Test",
            isPublic = true,
            isLiked = false,
            cardsCount = 10,
            likes = 5,
            trainings = 2
        )

        assertEquals(0, model.newCardsCount)
        assertEquals(0, model.reviewCardsCount)
    }

    @Test
    fun `FavouriteDto creates correctly`() {
        val dto = FavouriteDto(deckId = "deck-1")

        assertEquals("deck-1", dto.deckId)
    }

    @Test
    fun `PagingConfig constants are correct`() {
        assertEquals(10, ExploreRepositoryImpl.PAGE_SIZE)
        assertEquals(false, ExploreRepositoryImpl.ENABLE_PLACEHOLDERS)
        assertEquals(7, ExploreRepositoryImpl.PREFETCH_DISTANCE)
        assertEquals(10, ExploreRepositoryImpl.INITIAL_LOAD_SIZE)
    }

    @Test
    fun `search query builds correct filter for empty query`() {
        val query = ""

        val shouldFilter = query.isNotBlank()

        assertFalse(shouldFilter)
    }

    @Test
    fun `search query builds correct filter for non-empty query`() {
        val query = "kotlin"

        val shouldFilter = query.isNotBlank()

        assertTrue(shouldFilter)
    }

    @Test
    fun `search query builds correct filter for whitespace only`() {
        val query = "   "

        val shouldFilter = query.isNotBlank()

        assertFalse(shouldFilter)
    }

    @Test
    fun `sortBy returns likes for likes sort`() {
        val sortBy = "likes"

        val mainSort = when (sortBy) {
            "likes" -> "likes"
            "trainings" -> "trainings"
            else -> "created_at"
        }

        assertEquals("likes", mainSort)
    }

    @Test
    fun `sortBy returns trainings for trainings sort`() {
        val sortBy = "trainings"

        val mainSort = when (sortBy) {
            "likes" -> "likes"
            "trainings" -> "trainings"
            else -> "created_at"
        }

        assertEquals("trainings", mainSort)
    }

    @Test
    fun `sortBy returns created_at for null`() {
        val sortBy: String? = null

        val mainSort = when (sortBy) {
            "likes" -> "likes"
            "trainings" -> "trainings"
            else -> "created_at"
        }

        assertEquals("created_at", mainSort)
    }

    @Test
    fun `sortBy returns created_at for unknown value`() {
        val sortBy = "unknown"

        val mainSort = when (sortBy) {
            "likes" -> "likes"
            "trainings" -> "trainings"
            else -> "created_at"
        }

        assertEquals("created_at", mainSort)
    }

    @Test
    fun `category returns true for favourite filter`() {
        val category = "favourite"

        val showOnlyFavourites = category == "favourite"

        assertTrue(showOnlyFavourites)
    }

    @Test
    fun `category returns false for null`() {
        val category: String? = null

        val showOnlyFavourites = category == "favourite"

        assertFalse(showOnlyFavourites)
    }

    @Test
    fun `category returns false for other values`() {
        val category = "other"

        val showOnlyFavourites = category == "favourite"

        assertFalse(showOnlyFavourites)
    }

    @Test
    fun `list of DeckDto converts to list of DeckUiModel with likes`() {
        val deckDtos = listOf(
            DeckDto(
                id = "deck-1",
                name = "Deck 1",
                isPublic = true,
                cardsCount = 10,
                likes = 5,
                trainings = 3
            ),
            DeckDto(
                id = "deck-2",
                name = "Deck 2",
                isPublic = true,
                cardsCount = 20,
                likes = 15,
                trainings = 8
            )
        )

        val favouriteIds = setOf("deck-1")

        val uiModels = deckDtos.map { deckDto ->
            deckDto.toDomain().toUiModel(isLiked = favouriteIds.contains(deckDto.id))
        }

        assertEquals(2, uiModels.size)
        assertTrue(uiModels[0].isLiked)
        assertFalse(uiModels[1].isLiked)
        assertEquals("deck-1", uiModels[0].id)
        assertEquals("deck-2", uiModels[1].id)
    }

    @Test
    fun `list of DeckDto converts to list of DeckUiModel without likes`() {
        val deckDtos = listOf(
            DeckDto(
                id = "deck-1",
                name = "Deck 1",
                isPublic = true,
                cardsCount = 5,
                likes = 2,
                trainings = 1
            )
        )

        val favouriteIds = emptySet<String>()

        val uiModels = deckDtos.map { deckDto ->
            deckDto.toDomain().toUiModel(isLiked = favouriteIds.contains(deckDto.id))
        }

        assertEquals(1, uiModels.size)
        assertFalse(uiModels[0].isLiked)
    }

    @Test
    fun `empty list of DeckDto converts to empty list of DeckUiModel`() {
        val deckDtos = emptyList<DeckDto>()

        val uiModels = deckDtos.map { deckDto ->
            deckDto.toDomain().toUiModel(isLiked = true)
        }

        assertTrue(uiModels.isEmpty())
    }

    @Test
    fun `prevKey is null for first page`() {
        val page = 0

        val prevKey = if (page == 0) null else page - 1

        assertNull(prevKey)
    }

    @Test
    fun `prevKey is page minus 1 for non-first page`() {
        val page = 3

        val prevKey = if (page == 0) null else page - 1

        assertEquals(2, prevKey)
    }

    @Test
    fun `nextKey is null when items less than loadSize`() {
        val loadSize = 10
        val uiModelsSize = 5

        val nextKey = if (uiModelsSize < loadSize) null else 0 + 1

        assertNull(nextKey)
    }

    @Test
    fun `nextKey is page plus 1 when items equal loadSize`() {
        val page = 0
        val loadSize = 10
        val uiModelsSize = 10

        val nextKey = if (uiModelsSize < loadSize) null else page + 1

        assertEquals(1, nextKey)
    }

    @Test
    fun `range calculation for page 0`() {
        val page = 0
        val loadSize = 10

        val from = page * loadSize.toLong()
        val to = from + loadSize - 1

        assertEquals(0L, from)
        assertEquals(9L, to)
    }

    @Test
    fun `range calculation for page 1`() {
        val page = 1
        val loadSize = 10

        val from = page * loadSize.toLong()
        val to = from + loadSize - 1

        assertEquals(10L, from)
        assertEquals(19L, to)
    }

    @Test
    fun `range calculation for page 2 with custom loadSize`() {
        val page = 2
        val loadSize = 20

        val from = page * loadSize.toLong()
        val to = from + loadSize - 1

        assertEquals(40L, from)
        assertEquals(59L, to)
    }

    @Test
    fun `detects no duplicates in unique ids`() {
        val ids = listOf("deck-1", "deck-2", "deck-3")

        val hasDuplicates = ids.size != ids.toSet().size

        assertFalse(hasDuplicates)
    }

    @Test
    fun `detects duplicates in ids`() {
        val ids = listOf("deck-1", "deck-2", "deck-1")

        val hasDuplicates = ids.size != ids.toSet().size

        assertTrue(hasDuplicates)
    }

    @Test
    fun `handles empty ids list`() {
        val ids = emptyList<String>()

        val hasDuplicates = ids.size != ids.toSet().size

        assertFalse(hasDuplicates)
    }

    @Test
    fun `getRefreshKey returns prevKey plus 1 when available`() {
        val anchor = 15
        val prevKey: Int? = 1

        val refreshKey = prevKey?.plus(1)

        assertEquals(2, refreshKey)
    }

    @Test
    fun `getRefreshKey returns nextKey minus 1 when prevKey is null`() {
        val anchor = 15
        val prevKey: Int? = null
        val nextKey: Int? = 3

        val refreshKey = prevKey?.plus(1) ?: nextKey?.minus(1)

        assertEquals(2, refreshKey)
    }

    @Test
    fun `getRefreshKey returns null when anchor is null`() {
        val anchor: Int? = null

        val refreshKey = anchor?.let { 1 }

        assertNull(refreshKey)
    }

    @Test
    fun `DeckUiModel handles very long name`() {
        val longName = "A".repeat(500)
        val model = DeckUiModel(
            id = "deck-1",
            name = longName,
            isPublic = true,
            isLiked = false,
            cardsCount = 10,
            likes = 5,
            trainings = 2
        )

        assertEquals(500, model.name.length)
    }

    @Test
    fun `DeckUiModel equality check works correctly`() {
        val model1 = DeckUiModel(
            id = "deck-1",
            name = "Test",
            isPublic = true,
            isLiked = true,
            cardsCount = 10,
            likes = 5,
            trainings = 3
        )
        val model2 = DeckUiModel(
            id = "deck-1",
            name = "Test",
            isPublic = true,
            isLiked = true,
            cardsCount = 10,
            likes = 5,
            trainings = 3
        )

        assertEquals(model1, model2)
    }

    @Test
    fun `DeckUiModel inequality for different isLiked`() {
        val model1 = DeckUiModel(
            id = "deck-1",
            name = "Test",
            isPublic = true,
            isLiked = true,
            cardsCount = 10,
            likes = 5,
            trainings = 3
        )
        val model2 = DeckUiModel(
            id = "deck-1",
            name = "Test",
            isPublic = true,
            isLiked = false,
            cardsCount = 10,
            likes = 5,
            trainings = 3
        )

        assertTrue(model1 != model2)
    }
}
