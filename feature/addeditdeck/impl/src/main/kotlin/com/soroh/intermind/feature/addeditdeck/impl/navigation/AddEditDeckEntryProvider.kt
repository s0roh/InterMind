package com.soroh.intermind.feature.addeditdeck.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.Navigator
import com.soroh.intermind.feature.addeditdeck.api.navigation.AddEditDeckNavKey
import com.soroh.intermind.feature.addeditdeck.impl.AddEditDeckScreen
import com.soroh.intermind.feature.addeditdeck.impl.AddEditDeckViewModel

fun EntryProviderScope<NavKey>.addEditDeckEntry(navigator: Navigator) {
    entry<AddEditDeckNavKey> { key ->
        val viewModel = hiltViewModel<AddEditDeckViewModel, AddEditDeckViewModel.Factory> {
            it.create(key)
        }
        AddEditDeckScreen(
            viewModel = viewModel,
            isEditMode = key.deckId.isNullOrEmpty(),
            onBackClick = navigator::goBack,
            onSaveClick = navigator::goBack
        )
    }
}
