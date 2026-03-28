package com.soroh.intermind.feature.trainingmodesettings.impl.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun HintCard(
    imageResId: Int,
    titleResId: Int,
    descriptionResId: Int,
) {
    Column {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = stringResource(id = titleResId),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 35.dp)
        )
        Text(
            text = stringResource(id = titleResId),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.W600,
                fontSize = 18.sp
            ),
            modifier = Modifier.padding(horizontal = 28.dp)
        )
        Spacer(modifier = Modifier.height(11.dp))
        Text(
            text = stringResource(id = descriptionResId),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp
            ),
            modifier = Modifier.padding(horizontal = 28.dp)
        )
    }
}