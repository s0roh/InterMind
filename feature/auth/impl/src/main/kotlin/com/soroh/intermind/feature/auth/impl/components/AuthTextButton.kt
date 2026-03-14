package com.soroh.intermind.feature.auth.impl.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soroh.intermind.core.designsystem.component.ThemePreviews
import com.soroh.intermind.core.designsystem.theme.InterMindTheme

@Composable
internal fun AuthTextButton(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@ThemePreviews
@Composable
private fun AuthActionButtonPreview() {
    InterMindTheme {
        Surface {
            Column {
                AuthActionButton(
                    text = "Регистрация",
                    isLoading = false,
                    enabled = true,
                    onClick = {}
                )

                AuthActionButton(
                    text = "Войти",
                    isLoading = false,
                    enabled = false,
                    onClick = {}
                )

                AuthActionButton(
                    text = "Отправить",
                    isLoading = true,
                    enabled = true,
                    onClick = {}
                )
            }
        }
    }
}