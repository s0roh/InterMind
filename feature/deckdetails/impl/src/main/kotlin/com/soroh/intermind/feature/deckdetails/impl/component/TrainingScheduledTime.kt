package com.soroh.intermind.feature.deckdetails.impl.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.soroh.intermind.core.ui.util.getFormattedTime
import com.soroh.intermind.feature.deckdetails.api.R

@Composable
fun TrainingScheduledTime(nextTrainingTime: Long) {
    Text(
        text = stringResource(R.string.feature_deckdetails_api_training_scheduled),
        style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
    Text(
        text = getFormattedTime(nextTrainingTime),
        style = MaterialTheme.typography.titleSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
    Spacer(modifier = Modifier.height(17.dp))
}