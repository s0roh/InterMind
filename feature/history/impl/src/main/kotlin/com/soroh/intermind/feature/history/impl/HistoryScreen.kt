package com.soroh.intermind.feature.history.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.soroh.intermind.core.data.dto.history.TrainingHistoryItem
import com.soroh.intermind.core.designsystem.component.CenteredTopAppBar
import com.soroh.intermind.feature.history.api.R

@Composable
fun HistoryScreen(
    onHistoryClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = { CenteredTopAppBar(title = stringResource(R.string.feature_history_api_title)) },
        modifier = Modifier.padding()
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.onRefresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    if (!isRefreshing) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
                is HistoryUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ошибка: ${state.message}")
                        Button(onClick = { viewModel.loadHistory() }) {
                            Text("Повторить")
                        }
                    }
                }

                is HistoryUiState.Success -> {
                    if (state.groupedHistory.isEmpty()) {
                        EmptyHistoryMessage()
                    } else {
                        HistoryList(
                            groupedHistory = state.groupedHistory,
                            onHistoryClick = onHistoryClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryList(
    groupedHistory: Map<String, List<TrainingHistoryItem>>,
    onHistoryClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        groupedHistory.forEach { (header, items) ->
            item(key = header) {
                Text(
                    text = header,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            items(items, key = { it.id }) { session ->
                TrainingHistoryCard(
                    item = session,
                    onClick = { onHistoryClick(session.deckId) }
                )
            }
        }
    }
}

@Composable
private fun TrainingHistoryCard(
    item: TrainingHistoryItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.deckName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatDuration(item.durationSec),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SessionResultBar(
                correct = item.correctCount,
                incorrect = item.incorrectCount,
                skipped = item.skippedCount,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LegendItem(
                    color = Color(0xFF4CAF50),
                    count = item.correctCount,
                    label = stringResource(R.string.history_legend_correct)
                )
                if (item.incorrectCount > 0) {
                    LegendItem(
                        color = Color(0xFFF44336),
                        count = item.incorrectCount,
                        label = stringResource(R.string.history_legend_errors)
                    )
                }
                if (item.skippedCount > 0) {
                    LegendItem(
                        color = Color(0xFF9E9E9E),
                        count = item.skippedCount,
                        label = stringResource(R.string.history_legend_skipped)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionResultBar(
    correct: Int,
    incorrect: Int,
    skipped: Int,
    modifier: Modifier = Modifier
) {
    val total = correct + incorrect + skipped
    if (total == 0) return

    Row(
        modifier = modifier
            .height(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (correct > 0) {
            Box(
                Modifier
                    .weight(correct.toFloat())
                    .fillMaxHeight()
                    .background(Color(0xFF4CAF50))
            )
        }
        if (incorrect > 0) {
            Box(
                Modifier
                    .weight(incorrect.toFloat())
                    .fillMaxHeight()
                    .background(Color(0xFFF44336))
            )
        }
        if (skipped > 0) {
            Box(
                Modifier
                    .weight(skipped.toFloat())
                    .fillMaxHeight()
                    .background(Color(0xFF9E9E9E))
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, count: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = (seconds / 60).toString().padStart(2, '0')
    val secs = (seconds % 60).toString().padStart(2, '0')
    return "$minutes:$secs"
}

@Composable
private fun EmptyHistoryMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.you_dont_have_any_training_history_yet),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}