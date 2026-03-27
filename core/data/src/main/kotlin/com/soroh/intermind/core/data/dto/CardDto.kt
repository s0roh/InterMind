package com.soroh.intermind.core.data.dto

import com.soroh.intermind.core.domain.entity.Card
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CardDto(
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