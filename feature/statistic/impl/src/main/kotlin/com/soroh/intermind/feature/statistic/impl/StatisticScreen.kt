package com.soroh.intermind.feature.statistic.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.soroh.intermind.core.designsystem.component.CenteredTopAppBar
import com.soroh.intermind.core.designsystem.component.ErrorState
import com.soroh.intermind.core.designsystem.component.LoadingState
import com.soroh.intermind.core.designsystem.component.NavigationIconType
import com.soroh.intermind.feature.statistic.api.R
import com.soroh.intermind.feature.statistic.impl.component.CardPhasesPieChart
import com.soroh.intermind.feature.statistic.impl.component.ChartWithDialog
import com.soroh.intermind.feature.statistic.impl.component.FutureDueLineChart
import com.soroh.intermind.feature.statistic.impl.component.TrainingModesColumnChart

@Composable
fun StatisticScreen(
    viewModel: StatisticViewModel,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenteredTopAppBar(
                title = "Статистика колоды",
                navigationIconType = NavigationIconType.BACK,
                onNavigationClick = onBackClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is StatisticUiState.Loading -> LoadingState()

                is StatisticUiState.Error -> ErrorState(message = stringResource(R.string.failed_to_load_data))

                is StatisticUiState.Success -> StatisticContent(state)
            }
        }
    }
}

@Composable
private fun StatisticContent(state: StatisticUiState.Success) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ChartWithDialog(
            title = stringResource(R.string.stats_forecast_title),
            description = stringResource(R.string.stats_forecast_description),
            modifier = Modifier.height(320.dp),
            chart = { FutureDueLineChart(state.forecastStats) }
        )

        ChartWithDialog(
            title = stringResource(R.string.stats_modes_title),
            description = stringResource(R.string.stats_modes_description),
            modifier = Modifier.height(300.dp),
            chart = { TrainingModesColumnChart(state.modesStats) }
        )

        ChartWithDialog(
            title = stringResource(R.string.stats_progress_title),
            description = stringResource(R.string.stats_progress_description),
            modifier = Modifier.height(320.dp),
            chart = { CardPhasesPieChart(state.phasesStats) }
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}





