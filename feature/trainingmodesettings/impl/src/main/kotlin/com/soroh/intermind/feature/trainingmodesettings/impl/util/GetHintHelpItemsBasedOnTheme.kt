package com.soroh.intermind.feature.trainingmodesettings.impl.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.soroh.intermind.feature.trainingmodesettings.api.R


internal data class HintHelpItem(
    val imageResId: Int,
    val titleResId: Int,
    val descriptionResId: Int,
)

@Composable
internal fun getHintHelpItemsBasedOnTheme(): List<HintHelpItem> {
    return if (isSystemInDarkTheme()) {
        listOf(
            HintHelpItem(
                R.drawable.feature_trainingmodesettings_api_choice_help_dark,
                R.string.feature_trainingmodesettings_api_choice_help_title,
                R.string.feature_trainingmodesettings_api_choice_help_description
            ),
            HintHelpItem(
                R.drawable.feature_trainingmodesettings_api_true_false_help_dark,
                R.string.feature_trainingmodesettings_api_true_false_help_title,
                R.string.feature_trainingmodesettings_api_true_false_help_description
            ),
            HintHelpItem(
                R.drawable.feature_trainingmodesettings_api_input_help_dark,
                R.string.feature_trainingmodesettings_api_input_help_title,
                R.string.feature_trainingmodesettings_api_input_help_description
            )
        )
    } else {
        listOf(
            HintHelpItem(
                R.drawable.feature_trainingmodesettings_api_choice_help_light,
                R.string.feature_trainingmodesettings_api_choice_help_title,
                R.string.feature_trainingmodesettings_api_choice_help_description
            ),
            HintHelpItem(
                R.drawable.feature_trainingmodesettings_api_true_false_help_light,
                R.string.feature_trainingmodesettings_api_true_false_help_title,
                R.string.feature_trainingmodesettings_api_true_false_help_description
            ),
            HintHelpItem(
                R.drawable.feature_trainingmodesettings_api_input_help_light,
                R.string.feature_trainingmodesettings_api_input_help_title,
                R.string.feature_trainingmodesettings_api_input_help_description
            )
        )
    }
}