package com.soroh.intermind.core.data.model

import com.soroh.intermind.core.domain.entity.TrainingCard

data class TrainingItem(
    val trainingCard: TrainingCard,
    val progress: UserCardProgress
)