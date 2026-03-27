package com.soroh.intermind.feature.trainingmodesettings.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.soroh.intermind.core.designsystem.component.CenteredTopAppBar
import com.soroh.intermind.core.designsystem.component.ErrorState
import com.soroh.intermind.core.designsystem.component.LoadingState
import com.soroh.intermind.core.designsystem.component.NavigationIconType
import com.soroh.intermind.core.domain.entity.TestType
import com.soroh.intermind.core.domain.entity.TrainingModes
import com.soroh.intermind.feature.trainingmodesettings.api.R
import com.soroh.intermind.feature.trainingmodesettings.impl.component.HintsCarousel
import com.soroh.intermind.feature.trainingmodesettings.impl.component.TrainingModeSwitch
import com.soroh.intermind.feature.trainingmodesettings.impl.util.getHintHelpItemsBasedOnTheme
import com.soroh.intermind.feature.trainingmodesettings.impl.util.updateMode

@Composable
fun TrainingModeSettingsScreen(
    viewModel: TrainingModeSettingsViewModel,
    onBackClick: () -> Unit = {},
) {
    val state = viewModel.state.collectAsState()

    when (val currentState = state.value) {
        is TrainingModeSettingsState.Error -> ErrorState(message = currentState.message)
        TrainingModeSettingsState.Loading -> LoadingState()
        is TrainingModeSettingsState.Success -> {
            TrainingModeSettingsContent(
                state = currentState,
                onBackClick = onBackClick,
                onSaveClick = viewModel::saveModeSettings,
                onUpdateClick = viewModel::updateModes
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainingModeSettingsContent(
    state: TrainingModeSettingsState.Success,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onUpdateClick: (TrainingModes) -> Unit
) {
    var isBottomSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler {
        onSaveClick()
        onBackClick()
    }

    Scaffold(
        topBar = {
            CenteredTopAppBar(
                title = stringResource(R.string.feature_trainingmodesettings_api_settings),
                navigationIconType = NavigationIconType.BACK,
                onNavigationClick = {
                    onSaveClick()
                    onBackClick()
                }
            )
        }
    ) { paddingValues ->
        state.modes.let { modes ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                TrainingModeSwitch(
                    label = stringResource(R.string.feature_trainingmodesettings_api_answer_selection_mode),
                    checked = modes.modes.contains(TestType.CHOICE),
                    onCheckedChange = { isChecked ->
                        val updatedModes = updateMode(modes, TestType.CHOICE, isChecked)
                        onUpdateClick(updatedModes)
                    }
                )

                TrainingModeSwitch(
                    label = stringResource(R.string.feature_trainingmodesettings_api_true_false_mode),
                    checked = modes.modes.contains(TestType.TRUE_FALSE),
                    onCheckedChange = { isChecked ->
                        val updatedModes = updateMode(modes, TestType.TRUE_FALSE, isChecked)
                        onUpdateClick(updatedModes)
                    }
                )

                TrainingModeSwitch(
                    label = stringResource(R.string.feature_trainingmodesettings_api_part_of_answer_mode),
                    checked = modes.modes.contains(TestType.INPUT),
                    onCheckedChange = { isChecked ->
                        val updatedModes = updateMode(modes, TestType.INPUT, isChecked)
                        onUpdateClick(updatedModes)
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .wrapContentWidth()
                        .clip(MaterialTheme.shapes.large)
                        .clickable { isBottomSheetOpen = true }
                        .padding(vertical = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = stringResource(R.string.feature_trainingmodesettings_api_about_train_modes),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.feature_trainingmodesettings_api_about_train_modes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }
    }

    if (isBottomSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isBottomSheetOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 8.dp,
            dragHandle = {}
        ) {
            HintsCarousel(hints = getHintHelpItemsBasedOnTheme())
        }
    }
}