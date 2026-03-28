package com.soroh.intermind.feature.training.impl.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ChoiceButton(
    answer: String,
    isAnswered: Boolean,
    selectedAnswer: String?,
    correctAnswer: String,
    onAnswerSelected: () -> Unit
) {
    val containerColor = getAnswerColor(
        isAnswered = isAnswered,
        buttonAnswer = answer,
        correctAnswer = correctAnswer,
        selectedAnswer = selectedAnswer
    )
    val borderColor = getBorderColor(
        isAnswered = isAnswered,
        buttonAnswer = answer,
        correctAnswer = correctAnswer,
        selectedAnswer = selectedAnswer
    )

    OutlinedButton(
        onClick = onAnswerSelected,
        enabled = !isAnswered,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(disabledContainerColor = containerColor)
    ) {
        Text(
            text = answer,
            modifier = Modifier.padding(8.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun getAnswerColor(
    isAnswered: Boolean,
    buttonAnswer: String,
    correctAnswer: String,
    selectedAnswer: String?,
): Color {
    return when {
        isAnswered && buttonAnswer == correctAnswer -> MaterialTheme.colorScheme.secondaryContainer
        isAnswered && buttonAnswer == selectedAnswer -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.background
    }
}

@Composable
private fun getBorderColor(
    isAnswered: Boolean,
    buttonAnswer: String,
    correctAnswer: String,
    selectedAnswer: String?,
): Color {
    return when {
        isAnswered && (buttonAnswer == correctAnswer || buttonAnswer == selectedAnswer) -> MaterialTheme.colorScheme.background
        else -> MaterialTheme.colorScheme.outline
    }
}