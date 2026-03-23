package com.soroh.intermind.feature.deckdetails.impl

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soroh.intermind.core.designsystem.component.AppAlertDialog
import com.soroh.intermind.core.designsystem.component.AppButton
import com.soroh.intermind.core.designsystem.component.AppElevatedButton
import com.soroh.intermind.core.designsystem.component.CenteredTopAppBar
import com.soroh.intermind.core.designsystem.component.LoadingState
import com.soroh.intermind.core.designsystem.component.NavigationIconType
import com.soroh.intermind.core.designsystem.icon.InterMindIcons
import com.soroh.intermind.core.domain.entity.Card
import com.soroh.intermind.feature.deckdetails.api.R
import com.soroh.intermind.feature.deckdetails.impl.component.CardListBottomSheet
import com.soroh.intermind.feature.deckdetails.impl.component.DeckInfoRow
import com.soroh.intermind.feature.deckdetails.impl.component.DeckTitle
import com.soroh.intermind.feature.deckdetails.impl.component.ErrorContent
import com.soroh.intermind.feature.deckdetails.impl.component.TrainingScheduledTime
import com.soroh.intermind.feature.deckdetails.impl.util.imageUriCacheSaver
import com.soroh.intermind.feature.deckdetails.impl.util.rememberSavableWithMap

@Composable
fun DeckDetailsScreen(
    viewModel: DeckDetailsViewModel,
    onBackClick: () -> Unit,
    onEditDeckClick: (deckId: String) -> Unit,
    onEditCardClick: (deckId: String, cardId: String?) -> Unit,
    onAddCardClick: (deckId: String) -> Unit,
    onDeleteDeck: () -> Unit,
    onStartTrainingClick: (deckId: String) -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DeckDetailsEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    when (val currentState = uiState.value) {
        is DeckDetailUiState.Error -> ErrorContent(onBackClick = onBackClick)
        DeckDetailUiState.Loading -> LoadingState()
        is DeckDetailUiState.Success -> {
            DeckDetailsContent(
                state = currentState,
                onBackClick = onBackClick,
                onDeleteDeck = {
                    viewModel.deleteDeck()
                    onDeleteDeck()
                },
                onEditDeckClick = onEditDeckClick,
                onDeleteCard = {},
                onAddCardClick = onAddCardClick,
                onEditCard = onEditCardClick,
                onStartTrainingClick = onStartTrainingClick,
                viewModel = viewModel
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckDetailsContent(
    state: DeckDetailUiState.Success,
    onBackClick: () -> Unit,
    onDeleteDeck: () -> Unit,
    onEditDeckClick: (String) -> Unit,
    onDeleteCard: (Card) -> Unit,
    onAddCardClick: (deckId: String) -> Unit,
    onEditCard: (deckId: String, cardId: String?) -> Unit,
    onStartTrainingClick: (deckId: String) -> Unit,
    viewModel: DeckDetailsViewModel,

) {
    var isBottomSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDeleteDialog) {
        AppAlertDialog(
            title = stringResource(R.string.feature_deckdetails_api_edelete_deck_title),
            message = stringResource(R.string.feature_deckdetails_api_delete_deck_message),
            confirmButtonText = stringResource(R.string.feature_deckdetails_api_reaffirm),
            onConfirm = {
                showDeleteDialog = false
                onDeleteDeck()
            },
            onCancel = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CenteredTopAppBar(
                navigationIconType = NavigationIconType.BACK,
                onNavigationClick = onBackClick,
                showActions = state.isOwner,
                onEditDeck = { onEditDeckClick(state.deck.id) },
                onOwner = {},
                onDeckStatistic = {},
                onTrainingSettings = { },
                onDeleteDeck = { showDeleteDialog = true },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            DeckTitle(deck = state.deck)

            Spacer(modifier = Modifier.height(35.dp))

            DeckInfoRow(deck = state.deck)

            Spacer(modifier = Modifier.height(66.dp))

            AppElevatedButton(
                title = if (state.nextTrainingTime == null) stringResource(R.string.feature_deckdetails_api_schedule_train)
                else stringResource(R.string.feature_deckdetails_api_open_training_plan),
                shouldShowIcon = true,
                iconResId = InterMindIcons.Calendar,
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(17.dp))

            AppElevatedButton(
                title = stringResource(R.string.feature_deckdetails_api_show_cards),
                shouldShowIcon = true,
                iconResId = InterMindIcons.Bookmark,
                onClick = { isBottomSheetOpen = true },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            state.nextTrainingTime?.let { nextTrainingTime ->
                TrainingScheduledTime(nextTrainingTime = nextTrainingTime)
            }

            if (state.deck.cardsCount > 0) {
                AppButton(
                    title = stringResource(R.string.feature_deckdetails_api_start_train),
                    shouldShowIcon = true,
                    iconResId = InterMindIcons.Play,
                    onClick = { onStartTrainingClick(state.deck.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                        .height(52.dp)
                )
            }

            val listState = rememberLazyListState()
            val expandedCardId = rememberSaveable { mutableStateOf<String?>(null) }
            val imageUriCache = rememberSavableWithMap(
                initialValue = { mutableStateMapOf<Int, Uri?>() },
                saver = imageUriCacheSaver
            )
            if (isBottomSheetOpen) {
                CardListBottomSheet(
                    state = state,
                    cards = state.cards,
                    expandedCardId = expandedCardId,
                    sheetState = sheetState,
                    coroutineScope = coroutineScope,
                    listState = listState,
                    onEditCard = onEditCard,
                    onDeleteCard = onDeleteCard,
                    onAddCardClick = onAddCardClick,
                    onDismiss = { isBottomSheetOpen = false },
                )
            }

            LaunchedEffect(isBottomSheetOpen) {
                if (isBottomSheetOpen) {
                    sheetState.show()
                }
            }
        }
    }
}