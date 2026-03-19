package com.soroh.intermind.feature.deckdetails.impl.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.soroh.intermind.core.designsystem.component.CenteredTopAppBar
import com.soroh.intermind.core.designsystem.component.ErrorState
import com.soroh.intermind.core.designsystem.component.NavigationIconType
import com.soroh.intermind.feature.deckdetails.api.R

@Composable
internal fun ErrorContent(
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenteredTopAppBar(
                navigationIconType = NavigationIconType.BACK,
                onNavigationClick = onBackClick
            )
        }
    ) { paddingValues ->
        ErrorState(
            modifier = Modifier.padding(paddingValues),
            iconResId = R.drawable.feature_deckdetails_api_ic_error,
            message = stringResource(R.string.feature_deckdetails_api_error_loading_deck)
        )
    }
}