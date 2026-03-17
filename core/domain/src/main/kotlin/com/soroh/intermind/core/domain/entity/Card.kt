package com.soroh.intermind.core.domain.entity

data class Card(
    val id: String,
    val question: String,
    val answer: String,
    val wrongAnswers: List<String> = emptyList(),
    val attachment: String? = null,
    val picturePath: String? = null,
)