package com.soroh.intermind.feature.deckdetails.impl.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.soroh.intermind.core.domain.entity.Deck

@Composable
fun DeckTitle(deck: Deck) {
    Text(
        text = deck.name,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onSurface,
        ),
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}