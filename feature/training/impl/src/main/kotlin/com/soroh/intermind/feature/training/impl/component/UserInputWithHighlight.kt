package com.soroh.intermind.feature.training.impl.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.soroh.intermind.feature.training.api.R

@Composable
internal fun UserInputWithHighlight(
    userInput: String,
    missingWords: List<String>,
    isCorrect: Boolean,
) {
    Column {
        Text(
            text = stringResource(R.string.feature_training_api_your_answer),
            style = MaterialTheme.typography.labelMedium,
            color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        val annotatedUserInput = buildAnnotatedString {
            val userInputWords = userInput.split("\\s+".toRegex()).filter { it.isNotBlank() }

            if (userInput.isBlank()) {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                    append(stringResource(R.string.feature_training_api_you_dont_answered))
                }
            } else {
                userInputWords.forEachIndexed { index, word ->
                    val expectedWord = missingWords.getOrNull(index)

                    val isWordCorrect = word.equals(expectedWord, ignoreCase = true)
                    val color = if (isWordCorrect) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }

                    withStyle(style = SpanStyle(color = color)) {
                        append(word)
                    }
                    if (index < userInputWords.size - 1) append(" ")
                }
            }
        }

        Text(
            text = annotatedUserInput,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}