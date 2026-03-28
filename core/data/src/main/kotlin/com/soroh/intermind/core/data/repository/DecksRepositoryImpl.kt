package com.soroh.intermind.core.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.soroh.intermind.core.data.dto.CardDto
import com.soroh.intermind.core.data.dto.DeckDto
import com.soroh.intermind.core.data.dto.DeckTrainingStatsDto
import com.soroh.intermind.core.data.model.DeckTrainingStats
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Implements a [DecksRepository]
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DecksRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val supabase: SupabaseClient
) : DecksRepository {

    private val decksTable
        get() = supabase.postgrest["decks"]
    private val cardsTable
        get() = supabase.postgrest["cards"]

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)

    override fun getDecks(): Flow<List<Deck>> =
        refreshTrigger.onStart { emit(Unit) }
            .flatMapLatest { fetchDecks() }

    override fun getDecksTrainingStats(dailyLimit: Int): Flow<Map<String, DeckTrainingStats>> =
        refreshTrigger.onStart { emit(Unit) }
            .flatMapLatest {
                flow {
                    val userId = getCurrentUserId() ?: return@flow emit(emptyMap())
                    val response = supabase.postgrest.rpc(
                        function = "get_decks_training_stats",
                        parameters = buildJsonObject {
                            put("p_user_id", userId)
                            put("p_new_limit", dailyLimit)
                        }
                    ).decodeList<DeckTrainingStatsDto>()

                    val statsMap = response.associate {
                        it.deckId to DeckTrainingStats(
                            it.newCount,
                            it.reviewCount
                        )
                    }
                    emit(statsMap)
                }
            }.flowOn(Dispatchers.IO)

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
        return try {
            val card = getCardById(cardId)
            val path = card?.picturePath

            if (path.isNullOrBlank()) {
                return null
            }

            val publicUrl = supabase.storage["cards_pics"].publicUrl(path)
            publicUrl.toUri()
        } catch (_: Exception) {
            null
        }
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
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("Пользователь не авторизован (userId is null)")

        val bytes = try {
            context.contentResolver.openInputStream(pictureUri)?.use { it.readBytes() }
        } catch (e: Exception) {
            throw IllegalArgumentException("Не удалось прочитать изображение: ${e.message}")
        } ?: throw IllegalArgumentException("Пустой InputStream для URI")

        val fileName = "$userId/$deckId/${cardId}_${System.currentTimeMillis()}.jpg"

        val bucket = supabase.storage["cards_pics"]

        try {
            bucket.upload(path = fileName, data = bytes) { upsert = true }

            cardsTable.update({ set("picture_path", fileName) }) {
                filter { eq("id", cardId) }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deleteCard(card: Card) {
        cardsTable.delete { filter { eq("id", card.id) } }
    }

    override suspend fun deleteCardPicture(deckId: String, cardId: String) {
        val card = getCardById(cardId)
        val path = card?.picturePath

        if (path == null) {
            Log.w(TAG, "deleteCardPicture: picture_path уже равен null в БД. Удалять из Storage нечего.")
        } else {
            Log.d(TAG, "deleteCardPicture: Удаляю файл из Storage по пути: $path")
            try {
                supabase.storage["cards_pics"].delete(path)
                Log.d(TAG, "deleteCardPicture: Файл успешно удален из Storage")
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage)
            }
        }

        Log.d(TAG, "deleteCardPicture: Обнуляю picture_path в БД...")
        cardsTable.update({ set<String>("picture_path", null) }) {
            filter { eq("id", cardId) }
        }
        Log.d(TAG, "deleteCardPicture: Успешно")
    }

    private suspend fun getCurrentUserId(): String? {
        return supabase.auth.sessionManager.loadSession()?.user?.id
    }

    companion object {
        private const val TAG = "DecksRepository"
    }
}