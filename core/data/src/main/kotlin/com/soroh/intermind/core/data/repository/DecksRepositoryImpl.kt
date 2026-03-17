package com.soroh.intermind.core.data.repository

import android.content.Context
import android.net.Uri
import com.soroh.intermind.core.domain.entity.Card
import com.soroh.intermind.core.domain.entity.Deck
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Implements a [DecksRepository]
 */
class DecksRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabase: SupabaseClient
) : DecksRepository {

    private val decksTable
        get() = supabase.postgrest["decks"]
    private val cardsTable
        get() = supabase.postgrest["cards"]

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDecks(): Flow<List<Deck>> =
        refreshTrigger.onStart { emit(Unit) }
            .flatMapLatest { fetchDecks() }

    private fun fetchDecks(): Flow<List<Deck>> = flow {
        val userId = getCurrentUserId() ?: run {
            emit(emptyList())
            return@flow
        }

        val response = decksTable.select {
            filter { eq("user_id", userId) }
        }.decodeList<DeckDto>()

        val domainDecks = response.map { it.toDomain() }
        emit(domainDecks)
    }.flowOn(Dispatchers.IO)

    override suspend fun refreshDecks() {
        refreshTrigger.emit(Unit)
    }

    override suspend fun getDeckById(deckId: String): Deck? {
        val response = decksTable.select {
            filter { eq("id", deckId) }
        }
        return response.decodeSingleOrNull<DeckDto>()?.toDomain()
    }

    override suspend fun isDeckOwner(deckId: String): Boolean {
        val userId = getCurrentUserId() ?: return false

        val response = decksTable.select(columns = Columns.raw("user_id")) {
            filter {
                eq("id", deckId)
                eq("user_id", userId)
            }
        }

        return response.data != "[]" && response.decodeList<Map<String, String>>().isNotEmpty()
    }

    override suspend fun insertDeck(deck: Deck) {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User must be logged in to create a deck")
        val dto = DeckDto.fromDomain(deck, userId)
        decksTable.insert(dto)
    }

    override suspend fun updateDeck(deck: Deck) {
        decksTable.update(
            {
                set("name", deck.name)
                set("is_public", deck.isPublic)
                set("cards_count", deck.cardsCount)
                set("likes", deck.likes)
                set("trainings", deck.trainings)
            }
        ) {
            filter { eq("id", deck.id) }
        }
    }

    override suspend fun deleteDeck(deckId: String) {
        decksTable.delete {
            filter {
                eq("id", deckId)
            }
        }
    }

    override fun getCardsForDeck(deckId: String): Flow<List<Card>> = flow {
        val response = cardsTable
            .select {
                filter {
                    eq("deck_id", deckId)
                }
            }.decodeList<CardDto>()

        val domainCards = response.map { dto ->
            val publicUrl = dto.picturePath?.let { path ->
                supabase.storage["cards_pics"].publicUrl(path)
            }

            dto.toDomain(attachment = publicUrl)
        }
        emit(domainCards)
    }.flowOn(Dispatchers.IO)

    override suspend fun getCardById(cardId: String): Card? {
        val response = cardsTable.select {
            filter {
                eq("id", cardId)
            }
        }.decodeSingleOrNull<CardDto>()

        return response?.let { dto ->
            val publicUrl = dto.picturePath?.let { path ->
                supabase.storage["cards_pics"].publicUrl(path)
            }
            dto.toDomain(attachment = publicUrl)
        }
    }

    override suspend fun getCardPicture(
        deckId: String,
        cardId: String
    ): Uri? {
        TODO("Not yet implemented")
    }

    override suspend fun insertCard(
        card: Card,
        deckId: String
    ) {
        val dto = CardDto.fromDomain(card, deckId)
        cardsTable.insert(dto)
    }

    override suspend fun updateCard(card: Card) {
        cardsTable.update(
            {
                set("question", card.question)
                set("answer", card.answer)
                set("wrong_answers", card.wrongAnswers)
                set("picture_path", card.picturePath)
            }
        ) {
            filter { eq("id", card.id) }
        }
    }

    override suspend fun updateCardPicture(
        deckId: String,
        cardId: String,
        pictureUri: Uri
    ) {
        val bytes = context.contentResolver.openInputStream(pictureUri)?.use {
            it.readBytes()
        } ?: throw IllegalArgumentException("Cannot read URI")

        val userId = getCurrentUserId() ?: return
        val fileName = "$userId/$deckId/${cardId}_${System.currentTimeMillis()}.jpg"

        val bucket = supabase.storage["cards_pics"]

        bucket.upload(path = fileName, data = bytes) {
            upsert = true
        }

        cardsTable.update(
            {
                set("picture_path", fileName)
            }
        ) {
            filter { eq("id", cardId) }
        }
    }

    override suspend fun deleteCard(card: Card) {
        cardsTable.delete {
            filter { eq("id", card.id) }
        }
    }

    override suspend fun deleteCardPicture(deckId: String, cardId: String) {
        val card = getCardById(cardId)
        val path = card?.picturePath ?: return

        supabase.storage["cards_pics"].delete(path)

        cardsTable.update({ set<String>("picture_path", null) }) {
            filter { eq("id", cardId) }
        }
    }

    private suspend fun getCurrentUserId(): String? {
        return supabase.auth.sessionManager.loadSession()?.user?.id
    }
}

@Serializable
private data class DeckDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("cards_count") val cardsCount: Int = 0,
    @SerialName("likes") val likes: Int = 0,
    @SerialName("trainings") val trainings: Int = 0
) {
    fun toDomain() = Deck(
        id = id ?: "",
        name = name,
        isPublic = isPublic,
        authorId = userId,
        cardsCount = cardsCount,
        likes = likes,
        trainings = trainings
    )

    companion object {
        fun fromDomain(deck: Deck, userId: String?) = DeckDto(
            id = deck.id,
            name = deck.name,
            isPublic = deck.isPublic,
            userId = userId,
            cardsCount = deck.cardsCount,
            likes = deck.likes,
            trainings = deck.trainings
        )
    }
}

@Serializable
private data class CardDto(
    @SerialName("id") val id: String? = null,
    @SerialName("deck_id") val deckId: String = "",
    @SerialName("question") val question: String,
    @SerialName("answer") val answer: String,
    @SerialName("wrong_answers") val wrongAnswers: List<String> = emptyList(),
    @SerialName("picture_path") val picturePath: String? = null
) {
    fun toDomain(attachment: String?) = Card(
        id = id ?: "",
        question = question,
        answer = answer,
        wrongAnswers = wrongAnswers,
        attachment = attachment,
        picturePath = picturePath
    )

    companion object {
        fun fromDomain(card: Card, deckId: String) = CardDto(
            id = card.id,
            deckId = deckId,
            question = card.question,
            answer = card.answer,
            wrongAnswers = card.wrongAnswers,
            picturePath = card.picturePath
        )
    }
}