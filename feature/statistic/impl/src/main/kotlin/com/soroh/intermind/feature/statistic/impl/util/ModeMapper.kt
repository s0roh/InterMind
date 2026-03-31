package com.soroh.intermind.feature.statistic.impl.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.soroh.intermind.feature.statistic.api.R

@Composable
fun String.toModeLabel(): String {
    val resId = when (this.uppercase()) {
        "CHOICE" -> R.string.mode_choice
        "INPUT" -> R.string.mode_input
        "TRUE_FALSE" -> R.string.mode_true_false
        else -> R.string.mode_unknown
    }
    return stringResource(id = resId)
}