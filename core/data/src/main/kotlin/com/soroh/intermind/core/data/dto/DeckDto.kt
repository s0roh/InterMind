package com.soroh.intermind.core.data.dto

import com.soroh.intermind.core.domain.entity.Deck
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DeckDto(
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