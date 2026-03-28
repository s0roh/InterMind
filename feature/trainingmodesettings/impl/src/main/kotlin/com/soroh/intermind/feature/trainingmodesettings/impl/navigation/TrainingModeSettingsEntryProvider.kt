package com.soroh.intermind.feature.trainingmodesettings.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.Navigator
import com.soroh.intermind.feature.trainingmodesettings.api.navigation.TrainingModeSettingsNavKey
import com.soroh.intermind.feature.trainingmodesettings.impl.TrainingModeSettingsScreen
import com.soroh.intermind.feature.trainingmodesettings.impl.TrainingModeSettingsViewModel

fun EntryProviderScope<NavKey>.trainingModeSettingsEntry(navigator: Navigator) {
    entry<TrainingModeSettingsNavKey> { key ->
        val viewModel = hiltViewModel<TrainingModeSettingsViewModel, TrainingModeSettingsViewModel.Factory> {
            it.create(key)
        }
        TrainingModeSettingsScreen(
            viewModel = viewModel,
            onBackClick = navigator::goBack,
        )
    }
}
