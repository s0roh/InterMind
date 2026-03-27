package com.soroh.intermind.core.data.dto

import com.soroh.intermind.core.data.model.UserCardProgress
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
internal data class UserCardProgressDto(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("card_id") val cardId: String,
    @SerialName("stability") val stability: Double = 2.5,
    @SerialName("difficulty") val difficulty: Double = 2.5,
    @SerialName("interval") val interval: Int = 0,
    @SerialName("due_date") val dueDate: String,
    @SerialName("review_count") val reviewCount: Int = 0,
    @SerialName("last_review") val lastReview: String,
    @SerialName("phase") val phase: Int = 0
) {
    fun toDomain() = UserCardProgress(
        id = id,
        userId = userId,
        cardId = cardId,
        stability = stability,
        difficulty = difficulty,
        interval = interval,
        dueDate = Instant.parse(dueDate),
        reviewCount = reviewCount,
        lastReview = Instant.parse(lastReview),
        phase = phase
    )

    companion object {
        fun fromDomain(progress: UserCardProgress) = UserCardProgressDto(
            id = progress.id,
            userId = progress.userId,
            cardId = progress.cardId,
            stability = progress.stability,
            difficulty = progress.difficulty,
            interval = progress.interval,
            dueDate = progress.dueDate.toString(),
            reviewCount = progress.reviewCount,
            lastReview = progress.lastReview.toString(),
            phase = progress.phase
        )
    }
}