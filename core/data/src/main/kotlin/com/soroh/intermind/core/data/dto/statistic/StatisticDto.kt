package com.soroh.intermind.core.data.dto.statistic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrainingSessionDto(
    @SerialName("id") val id: String,
    @SerialName("deck_id") val deckId: String,
    @SerialName("modes_stat") val modesStat: Map<String, ModeStatDto> = emptyMap()
)

@Serializable
data class ModeStatDto(
    @SerialName("total") val total: Int = 0,
    @SerialName("correct") val correct: Int = 0
) {
    val incorrect: Int get() = total - correct
}

data class AggregatedModeStatistic(
    val modeName: String,
    val totalCards: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int
)

data class CardPhaseStat(
    val phase: CardPhase,
    val count: Int
)

enum class CardPhase {
    NEW,
    LEARNING,
    GRADUATED
}

data class ForecastStat(
    val date: String,
    val count: Int
)

@Serializable
data class CardWithProgressDto(
    val id: String,
    @SerialName("user_card_progress") val progress: List<ProgressPhaseDto>? = null
)

@Serializable
data class ProgressPhaseDto(
    val phase: Int
)

@Serializable
data class CardDueDateDto(
    @SerialName("user_card_progress") val progress: List<DueDateDto>? = null
)

@Serializable
data class DueDateDto(
    @SerialName("due_date") val dueDate: String
)