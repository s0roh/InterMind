package com.soroh.intermind.feature.training.impl.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.soroh.intermind.feature.training.api.R
import com.soroh.intermind.feature.training.impl.TrainingScreenState

@Composable
internal fun TFButton(
    text: String,
    isButtonTrue: Boolean,
    state: TrainingScreenState.InProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = getTFContainerColor(
        isAnswered = state.isAnswerRevealed,
        selectedAnswer = state.selectedAnswer,
        buttonIsTrue = isButtonTrue,
        correctAnswer = state.currentCard.answer,
        displayedAnswer = state.currentCard.displayedAnswer
    )

    val borderColor = getTFBorderColor(
        isAnswered = state.isAnswerRevealed,
        selectedAnswer = state.selectedAnswer,
        buttonIsTrue = isButtonTrue,
        correctAnswer = state.currentCard.answer,
        displayedAnswer = state.currentCard.displayedAnswer
    )

    Surface(
        onClick = onClick,
        enabled = !state.isAnswerRevealed,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        color = containerColor,
        modifier = modifier.height(100.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(
                    id = if (text == stringResource(R.string.feature_training_api_true_answer)) R.drawable.ic_true
                    else R.drawable.ic_false
                ),
                contentDescription = null,
                tint = if (text == stringResource(R.string.feature_training_api_true_answer)) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                modifier = if (text == stringResource(R.string.feature_training_api_true_answer)) Modifier.size(
                    100.dp
                )
                else Modifier.size((70.dp))
            )
        }
    }
}

@Composable
private fun getTFContainerColor(
    isAnswered: Boolean,
    selectedAnswer: String?,
    buttonIsTrue: Boolean,
    correctAnswer: String,
    displayedAnswer: String?
): Color {
    val isActuallyTrue = displayedAnswer == correctAnswer
    val userSelectedThisButton = isAnswered && (
            (buttonIsTrue && selectedAnswer != null) || (!buttonIsTrue && selectedAnswer == null)
            )

    return when {
        isAnswered && buttonIsTrue == isActuallyTrue -> MaterialTheme.colorScheme.secondaryContainer
        userSelectedThisButton -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.background
    }
}

@Composable
private fun getTFBorderColor(
    isAnswered: Boolean,
    selectedAnswer: String?,
    buttonIsTrue: Boolean,
    correctAnswer: String,
    displayedAnswer: String?
): Color {
    val isActuallyTrue = displayedAnswer == correctAnswer
    val userSelectedThisButton = isAnswered && (
            (buttonIsTrue && selectedAnswer != null) || (!buttonIsTrue && selectedAnswer == null)
            )

    return when {
        isAnswered && buttonIsTrue == isActuallyTrue -> MaterialTheme.colorScheme.background
        userSelectedThisButton -> MaterialTheme.colorScheme.background
        else -> MaterialTheme.colorScheme.outline
    }
}