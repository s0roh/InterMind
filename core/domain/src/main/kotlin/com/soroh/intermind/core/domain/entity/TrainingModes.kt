package com.soroh.intermind.core.domain.entity

data class TrainingModes(
    val deckId: String,
    val modes: List<TestType>
)