package com.soroh.intermind.feature.training.impl.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soroh.intermind.feature.training.api.R

@Composable
internal fun TrueFalseAnswerSection(displayedAnswer: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.feature_training_api_possible_answer),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayedAnswer ?: stringResource(R.string.feature_training_api_display_error),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp)
        )
    }
}