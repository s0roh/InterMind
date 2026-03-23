package com.soroh.intermind.feature.deckdetails.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.Navigator
import com.soroh.intermind.fearure.addeditcard.api.navigation.AddEditCardNavKey
import com.soroh.intermind.feature.addeditdeck.api.navigation.AddEditDeckNavKey
import com.soroh.intermind.feature.deckdetails.api.navigation.DeckDetailsNavKey
import com.soroh.intermind.feature.deckdetails.impl.DeckDetailsScreen
import com.soroh.intermind.feature.deckdetails.impl.DeckDetailsViewModel

fun EntryProviderScope<NavKey>.deckDetailsEntry(navigator: Navigator) {
    entry<DeckDetailsNavKey> { key ->
        val viewModel = hiltViewModel<DeckDetailsViewModel, DeckDetailsViewModel.Factory> {
            it.create(key)
        }
        DeckDetailsScreen(
            viewModel = viewModel,
            onBackClick = navigator::goBack,
            onEditDeckClick = { deckId -> navigator.navigate(AddEditDeckNavKey(deckId)) },
            onDeleteDeck = navigator::goBack,
            onEditCardClick = { deckId, cardId ->
                navigator.navigate(AddEditCardNavKey(cardId = cardId, deckId = deckId))
            },
            onAddCardClick = {deckId ->
                navigator.navigate(AddEditCardNavKey(deckId = deckId))
            },
            onStartTrainingClick = {}
        )
    }
}
