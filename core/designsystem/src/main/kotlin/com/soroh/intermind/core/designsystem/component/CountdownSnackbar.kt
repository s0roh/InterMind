package com.soroh.intermind.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soroh.intermind.core.designsystem.theme.InterMindTheme
import kotlinx.coroutines.delay

/**
 * Displays a snackbar with a countdown timer.
 *
 * @param snackbarData The data to be displayed in the snackbar, typically provided by [SnackbarHostState].
 * @param modifier The [Modifier] to be applied to the snackbar.
 * @param durationInSeconds The duration of the countdown timer in seconds.
 * @param actionOnNewLine Whether the action should be displayed on a separate line.
 * @param shape The [Shape] of the snackbar container.
 * @param containerColor The background color of the snackbar.
 * @param contentColor The preferred color for the content within the snackbar.
 * @param actionColor The color for the action button text.
 * @param actionContentColor The content color for the action button.
 * @param dismissActionContentColor The content color for the dismiss action icon.
 */
@Composable
fun CountdownSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    durationInSeconds: Int = 5,
    actionOnNewLine: Boolean = false,
    shape: Shape = SnackbarDefaults.shape,
    containerColor: Color = SnackbarDefaults.color,
    contentColor: Color = SnackbarDefaults.contentColor,
    actionColor: Color = SnackbarDefaults.actionColor,
    actionContentColor: Color = SnackbarDefaults.actionContentColor,
    dismissActionContentColor: Color = SnackbarDefaults.dismissActionContentColor,
) {
    val totalDuration = remember(durationInSeconds) { durationInSeconds * 1000 }
    var millisRemaining by remember { mutableIntStateOf(totalDuration) }

    LaunchedEffect(snackbarData) {
        while (millisRemaining > 0) {
            delay(40)
            millisRemaining -= 40
        }
        snackbarData.dismiss()
    }

    val actionLabel = snackbarData.visuals.actionLabel
    val actionComposable: (@Composable () -> Unit)? = if (actionLabel != null) {
        @Composable {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = actionColor),
                onClick = { snackbarData.performAction() },
                content = { Text(actionLabel) }
            )
        }
    } else {
        null
    }

    val dismissActionComposable: (@Composable () -> Unit)? =
        if (snackbarData.visuals.withDismissAction) {
            @Composable {
                IconButton(
                    onClick = { snackbarData.dismiss() },
                    content = {
                        Icon(Icons.Rounded.Close, null)
                    }
                )
            }
        } else {
            null
        }

    Snackbar(
        modifier = modifier.padding(12.dp),
        action = actionComposable,
        actionOnNewLine = actionOnNewLine,
        dismissAction = dismissActionComposable,
        dismissActionContentColor = dismissActionContentColor,
        actionContentColor = actionContentColor,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = shape,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SnackbarCountdown(
                timerProgress = millisRemaining.toFloat() / totalDuration.toFloat(),
                secondsRemaining = (millisRemaining + 999) / 1000,
                color = contentColor
            )
            Text(
                text = snackbarData.visuals.message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun SnackbarCountdown(
    timerProgress: Float,
    secondsRemaining: Int,
    color: Color,
) {
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val strokeStyle = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = color.copy(alpha = 0.12f),
                style = strokeStyle
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (-360f * timerProgress),
                useCenter = false,
                style = strokeStyle
            )
        }
        Text(
            text = secondsRemaining.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = color
            )
        )
    }
}

@ThemePreviews
@Composable
fun CountdownSnackbarPreview() {
    InterMindTheme {
        val previewSnackbarData = object : SnackbarData {
            override val visuals: SnackbarVisuals = object : SnackbarVisuals {
                override val message: String = "Колода удалена"
                override val actionLabel: String = "Отмена"
                override val withDismissAction: Boolean = true
                override val duration: SnackbarDuration = SnackbarDuration.Short
            }

            override fun performAction() {}
            override fun dismiss() {}
        }

        CountdownSnackbar(
            snackbarData = previewSnackbarData,
            durationInSeconds = 5,
            modifier = Modifier.padding(16.dp)
        )
    }
}