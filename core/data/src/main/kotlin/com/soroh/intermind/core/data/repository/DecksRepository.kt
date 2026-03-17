package com.soroh.intermind.core.data.repository

import android.net.Uri
import com.soroh.intermind.core.domain.entity.Card
import com.soroh.intermind.core.domain.entity.Deck
import kotlinx.coroutines.flow.Flow

/**
 * Data layer interface for the decks feature.
 */
interface DecksRepository {

    fun getDecks(): Flow<List<Deck>>

    suspend fun refreshDecks()

    suspend fun getDeckById(deckId: String): Deck?

    suspend fun isDeckOwner(deckId: String): Boolean

    suspend fun insertDeck(deck: Deck)

    suspend fun updateDeck(deck: Deck)

    suspend fun deleteDeck(deckId: String)

    fun getCardsForDeck(deckId: String): Flow<List<Card>>

    suspend fun getCardById(cardId: String): Card?

    suspend fun getCardPicture(deckId: String, cardId: String): Uri?

    suspend fun insertCard(card: Card, deckId: String)

    suspend fun updateCard(card: Card)

    suspend fun updateCardPicture(deckId: String, cardId: String, pictureUri: Uri)

    suspend fun deleteCard(card: Card)

    suspend fun deleteCardPicture(deckId: String, cardId: String)
}