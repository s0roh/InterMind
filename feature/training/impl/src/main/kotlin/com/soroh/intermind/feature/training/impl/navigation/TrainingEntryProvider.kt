package com.soroh.intermind.feature.training.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.Navigator
import com.soroh.intermind.feature.training.api.navigation.TrainingNavKey
import com.soroh.intermind.feature.training.impl.TrainingScreen
import com.soroh.intermind.feature.training.impl.TrainingViewModel

fun EntryProviderScope<NavKey>.trainingEntry(navigator: Navigator) {
    entry<TrainingNavKey> { key ->
        val viewModel = hiltViewModel<TrainingViewModel, TrainingViewModel.Factory> {
            it.create(key)
        }
        TrainingScreen(
            viewModel = viewModel,
            onBackClick = navigator::goBack,
        )
    }
}
