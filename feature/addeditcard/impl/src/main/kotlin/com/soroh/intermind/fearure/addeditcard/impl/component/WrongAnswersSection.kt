package com.soroh.intermind.fearure.addeditcard.impl.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.soroh.intermind.fearure.addeditcard.api.R

@Composable
internal fun WrongAnswersSection(
    wrongAnswers: List<String>,
    onAddWrongAnswer: () -> Unit,
    onWrongAnswerChanged: (Int, String) -> Unit,
    onRemoveWrongAnswer: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        wrongAnswers.forEachIndexed { index, answer ->
            WrongAnswerField(
                value = answer,
                onValueChange = { onWrongAnswerChanged(index, it) },
                onRemove = { onRemoveWrongAnswer(index) }
            )
        }

        if (wrongAnswers.size < 3) {
            TextButton(
                onClick = onAddWrongAnswer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.feature_addeditcard_api_add_wrong_answer))
            }
        }

        if (wrongAnswers.isEmpty()) {
            Text(
                text = stringResource(R.string.feature_addeditcard_api_wrong_answers_hint),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}