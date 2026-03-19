package com.soroh.intermind.core.ui.model

data class DeckUiModel(
    val id: String,
    val name: String,
    val isPublic: Boolean,
    val isLiked: Boolean,
    val cardsCount: Int,
    val likes: Int,
    val trainings: Int,
)
