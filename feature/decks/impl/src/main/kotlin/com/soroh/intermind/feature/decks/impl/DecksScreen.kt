package com.soroh.intermind.feature.decks.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.soroh.intermind.core.designsystem.component.CenteredTopAppBar
import com.soroh.intermind.core.ui.component.DeckDisplayMode
import com.soroh.intermind.core.ui.component.DeckItem
import com.soroh.intermind.feature.decks.api.R

@Composable
fun DecksScreen(
    onDeckClick: (String) -> Unit,
    onAddClick: () -> Unit,
    viewModel: DecksViewModel = hiltViewModel()
) {
    val decks by viewModel.decks.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val refreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()
    val expandedFab by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        topBar = {
            CenteredTopAppBar(title = stringResource(R.string.feature_decks_api_my_decks))
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                expanded = expandedFab,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(text = stringResource(R.string.feature_decks_api_create_deck)) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            state = refreshState,
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refreshDecks
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(items = decks, key = { it.id }) { deck ->
                    DeckItem(
                        modifier = Modifier.animateItem(),
                        deck = deck,
                        mode = DeckDisplayMode.PERSONAL,
                        onDeckClick = { onDeckClick(deck.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(180.dp))
                }
            }
        }
    }
}