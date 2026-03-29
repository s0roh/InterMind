package com.soroh.intermind.feature.explore.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.soroh.intermind.core.ui.component.DeckDisplayMode
import com.soroh.intermind.core.ui.component.DeckItem
import com.soroh.intermind.core.ui.model.DeckUiModel
import com.soroh.intermind.feature.explore.api.R

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel(),
    onDeckClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    val query = rememberSaveable { mutableStateOf("") }
    val searchBarExpanded = remember { mutableStateOf(false) }
    val previousExpanded = remember { mutableStateOf(searchBarExpanded.value) }

    val listState = rememberLazyListState()
    val savedScrollPosition = rememberSaveable { mutableIntStateOf(0) }
    val savedScrollOffset = rememberSaveable { mutableIntStateOf(0) }

    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val pagingItems = viewModel.decksFlow.collectAsLazyPagingItems()

    val isScrollingDown = remember { derivedStateOf { listState.firstVisibleItemScrollOffset > 0 } }

    val animatedPadding by animateDpAsState(
        targetValue = if (searchBarExpanded.value) 0.dp else 24.dp,
        label = stringResource(R.string.feature_explore_api_searchbarpadding)
    )

    LaunchedEffect(searchBarExpanded.value) {
        val wasExpanded = previousExpanded.value
        val isNowCollapsed = !searchBarExpanded.value

        if (wasExpanded && isNowCollapsed && query.value.isBlank()) {
            listState.scrollToItem(savedScrollPosition.intValue, savedScrollOffset.intValue)
        }

        savedScrollPosition.intValue = listState.firstVisibleItemIndex
        savedScrollOffset.intValue = listState.firstVisibleItemScrollOffset

        previousExpanded.value = searchBarExpanded.value
    }

    PullToRefreshBox(
        isRefreshing = pagingItems.loadState.refresh is LoadState.Loading,
        onRefresh = { pagingItems.refresh() },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SearchBarComponent(
                screenState = state,
                searchBarExpanded = searchBarExpanded,
                searchResults = searchResults,
                isSearching = isSearching,
                onQueryChange = viewModel::updateSearchQuery,
                onLikeToggle = viewModel::toggleLike,
                onDeckClick = onDeckClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = animatedPadding)
            )

            AnimatedVisibility(visible = !isScrollingDown.value) {
                SortAndCategoryFilters(
                    state = state,
                    updateSortType = viewModel::updateSortType,
                    updateCategory = viewModel::updateCategory
                )
            }

            HandlePagingLoadState(
                loadState = pagingItems.loadState.refresh,
                itemCount = pagingItems.itemCount,
                isLikeCategorySelected = state.category == DeckCategory.LIKED,
                onRetry = { pagingItems.retry() }
            ) {
                LazyColumn(
                    modifier = Modifier,
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    items(
                        count = pagingItems.itemCount,
                        key = pagingItems.itemKey { it.id }
                    ) { index ->
                        pagingItems[index]?.let { deck ->
                            DeckItem(
                                deck = deck,
                                mode = DeckDisplayMode.SOCIAL,
                                onDeckClick = onDeckClick,
                                onLikeClick = { viewModel.toggleLike(deck) },
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .animateItem()
                            )

                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HandlePagingLoadState(
    loadState: LoadState,
    itemCount: Int,
    onRetry: () -> Unit,
    isLikeCategorySelected: Boolean,
    onContent: @Composable () -> Unit,
) {
    when (loadState) {
        is LoadState.Loading -> LoadingIndicator()
        is LoadState.Error -> {
            if (isLikeCategorySelected || itemCount == 0) {
                EmptyContent(modifier = Modifier.fillMaxSize())
            } else {
                ErrorContent(onRetry = onRetry, modifier = Modifier.fillMaxSize())
            }
        }

        is LoadState.NotLoading if itemCount == 0 -> EmptyContent(modifier = Modifier.fillMaxSize())
        else -> onContent()
    }
}

@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = stringResource(R.string.feature_explore_api_no_data),
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.feature_explore_api_no_results),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorContent(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.WifiOff,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.feature_explore_api_wifi_error),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.feature_explore_api_retry))
            }
            Spacer(modifier = Modifier.height(48.dp))

        }
    }
}

@Composable
private fun SortAndCategoryFilters(
    state: PublicDecksScreenState,
    updateSortType: (SortType) -> Unit,
    updateCategory: (DeckCategory) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val selectedSortType = state.sortType

            SortType.entries.forEach { sortType ->
                val backgroundColor by animateColorAsState(
                    targetValue = if (selectedSortType == sortType)
                        MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surface,
                    label = stringResource(R.string.feature_explore_api_sorttypebackground)
                )

                AssistChip(
                    onClick = { if (selectedSortType != sortType) updateSortType(sortType) },
                    label = {
                        Crossfade(targetState = sortType) { targetSortType ->
                            Text(
                                text = when (targetSortType) {
                                    SortType.LIKES -> stringResource(R.string.feature_explore_api_by_likes)
                                    SortType.TRAININGS -> stringResource(R.string.feature_explore_api_by_train)
                                }
                            )
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = backgroundColor),
                    border = if (selectedSortType == sortType) null else AssistChipDefaults.assistChipBorder(
                        true
                    ),
                    leadingIcon = {
                        if (selectedSortType == sortType) {
                            Crossfade(targetState = selectedSortType) { selected ->
                                if (selected == sortType) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = stringResource(R.string.feature_explore_api_selected_mark)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val selectedCategory = state.category
            AssistChip(
                onClick = {
                    if (selectedCategory == DeckCategory.LIKED) {
                        updateCategory(DeckCategory.ALL)
                    } else {
                        updateCategory(DeckCategory.LIKED)
                    }
                },
                label = { Text(stringResource(R.string.feature_explore_api_favorites)) },
                colors = AssistChipDefaults.assistChipColors(
                    if (selectedCategory == DeckCategory.LIKED)
                        MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                border = if (selectedCategory == DeckCategory.LIKED) null
                else AssistChipDefaults.assistChipBorder(true),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.feature_explore_api_favorite_icon),
                        tint = if (selectedCategory == DeckCategory.LIKED) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarComponent(
    screenState: PublicDecksScreenState,
    searchBarExpanded: MutableState<Boolean>,
    searchResults: List<DeckUiModel>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onDeckClick: (String) -> Unit,
    onLikeToggle: (DeckUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val inputField = @Composable {
        InputField(
            query = screenState.query,
            onQueryChange = onQueryChange,
            onSearch = { onQueryChange(it) },
            expanded = searchBarExpanded.value,
            onExpandedChange = { searchBarExpanded.value = it },
            placeholder = { Text(stringResource(R.string.feature_explore_api_search_decks)) },
            leadingIcon = {
                if (searchBarExpanded.value) {
                    IconButton(
                        onClick = {
                            searchBarExpanded.value = false
                            onQueryChange("")
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = screenState.query.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            }
        )
    }

    SearchBar(
        inputField = inputField,
        expanded = searchBarExpanded.value,
        onExpandedChange = { expanded ->
            searchBarExpanded.value = expanded
            if (!expanded) onQueryChange("")
        },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {

        if (isSearching) {
            if (searchResults.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.feature_explore_api_no_results))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    items(searchResults, key = { it.id }) { deck ->
                        DeckItem(
                            deck = deck,
                            mode = DeckDisplayMode.SOCIAL,
                            onDeckClick = onDeckClick,
                            onLikeClick = { onLikeToggle(deck) },
                            modifier = Modifier.animateItem()
                        )
                    }
                    item { Spacer(Modifier.imePadding()) }
                }
            }
        }
    }
}