package com.soroh.intermind.feature.training.impl.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.soroh.intermind.feature.training.api.R

@Composable
internal fun AnswerWithHighlight(
    answer: String,
    missingWords: List<String>,
    startIndex: Int
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = stringResource(R.string.feature_training_api_answer),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        val words = answer.split("\\s+".toRegex()).filter { it.isNotBlank() }

        val annotatedAnswer = buildAnnotatedString {
            words.forEachIndexed { index, word ->
                if (index > 0) append(" ")

                if (index >= startIndex && index < startIndex + missingWords.size) {
                    withStyle(style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )) {
                        append(word)
                    }
                } else {
                    append(word)
                }
            }
        }

        Text(
            text = annotatedAnswer,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}