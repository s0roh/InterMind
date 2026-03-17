package com.soroh.intermind.fearure.addeditcard.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.Navigator
import com.soroh.intermind.fearure.addeditcard.api.navigation.AddEditCardNavKey
import com.soroh.intermind.fearure.addeditcard.impl.AddEditCardScreen
import com.soroh.intermind.fearure.addeditcard.impl.AddEditCardViewModel

fun EntryProviderScope<NavKey>.addEditCardEntry(navigator: Navigator) {
    entry<AddEditCardNavKey> { key ->
        val viewModel = hiltViewModel<AddEditCardViewModel, AddEditCardViewModel.Factory> {
            it.create(key)
        }
        AddEditCardScreen(
            viewModel = viewModel,
            onBackClick = navigator::goBack,
            onSaveClick = navigator::goBack
        )
    }
}
