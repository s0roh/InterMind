package com.soroh.intermind.core.domain.entity

data class Deck(
    val id: String,
    val name: String,
    val isPublic: Boolean,
    val authorId: String? = null,
    val cardsCount: Int,
    val likes: Int,
    val trainings: Int,
)