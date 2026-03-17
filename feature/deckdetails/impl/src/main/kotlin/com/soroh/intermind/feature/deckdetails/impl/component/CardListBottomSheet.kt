package com.soroh.intermind.feature.deckdetails.impl.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.soroh.intermind.core.designsystem.component.AppButton
import com.soroh.intermind.core.domain.entity.Card
import com.soroh.intermind.feature.deckdetails.api.R
import com.soroh.intermind.feature.deckdetails.impl.DeckDetailUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListBottomSheet(
    state: DeckDetailUiState.Success,
    cards: List<Card>,
    expandedCardId: MutableState<String?>,
    sheetState: SheetState,
    coroutineScope: CoroutineScope,
    listState: LazyListState,
    onEditCard: (deckId: String, cardId: String) -> Unit,
    onDeleteCard: (Card) -> Unit,
    onAddCardClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.deck.cardsCount == 0) {
                NoCardsPlaceholder(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (state.isOwner) 60.dp else 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.feature_deckdetails_api_tap_the_card_to_see_the_answer),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(bottom = 7.dp)
                        )
                    }

                    items(cards, key = { it.id }) { card ->
                        val isMenuExpanded = remember { mutableStateOf(false) }

                        ExpandableCardItem(
                            card = card,
                            deckId = state.deck.id,
                            isOwner = state.isOwner,
                            expandedCardId = expandedCardId,
                            isMenuExpanded = isMenuExpanded,
                            sheetState = sheetState,
                            coroutineScope = coroutineScope,
                            onEditCard = onEditCard,
                            onDeleteCard = onDeleteCard,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            if (state.isOwner) {
                AppButton(
                    title = stringResource(R.string.feature_deckdetails_api_add_card),
                    onClick = {
                        coroutineScope.launch {
                            sheetState.hide()
                            onAddCardClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}