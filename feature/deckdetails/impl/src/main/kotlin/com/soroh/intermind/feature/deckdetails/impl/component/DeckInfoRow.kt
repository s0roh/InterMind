package com.soroh.intermind.feature.deckdetails.impl.component

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soroh.intermind.core.domain.entity.Deck
import com.soroh.intermind.core.ui.util.getCardWordForm
import com.soroh.intermind.feature.deckdetails.api.R

@Composable
fun DeckInfoRow(deck: Deck) {
    Row(
        modifier = Modifier.width(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (deck.isPublic) stringResource(R.string.feature_deckdetails_api_public_mark)
            else stringResource(R.string.feature_deckdetails_api_private_mark),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        VerticalDivider(
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .height(44.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${deck.cardsCount} ${getCardWordForm(deck.cardsCount)}",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}