package com.soroh.intermind.feature.statistic.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.Navigator
import com.soroh.intermind.feature.statistic.api.navigation.StatisticNavKey
import com.soroh.intermind.feature.statistic.impl.StatisticScreen
import com.soroh.intermind.feature.statistic.impl.StatisticViewModel

fun EntryProviderScope<NavKey>.statisticEntry(navigator: Navigator) {
    entry<StatisticNavKey> { key ->
        val viewModel = hiltViewModel<StatisticViewModel, StatisticViewModel.Factory> {
            it.create(key)
        }
        StatisticScreen(
            viewModel = viewModel,
            onBackClick = navigator::goBack,
        )
    }
}
