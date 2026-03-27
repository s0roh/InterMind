package com.soroh.intermind.feature.trainingmodesettings.impl.util

import com.soroh.intermind.core.domain.entity.TestType
import com.soroh.intermind.core.domain.entity.TrainingModes

internal fun updateMode(
    modes: TrainingModes,
    mode: TestType,
    isChecked: Boolean,
): TrainingModes {
    val currentModes = modes.modes.toMutableSet()

    if (isChecked) {
        currentModes.add(mode)
    } else if (currentModes.size > 1) {
        currentModes.remove(mode)
    }

    return modes.copy(modes = currentModes.toList())
}