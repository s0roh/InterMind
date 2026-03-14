package com.soroh.intermind.feature.auth.impl.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soroh.intermind.core.designsystem.component.ThemePreviews
import com.soroh.intermind.core.designsystem.theme.InterMindTheme

@Composable
internal fun AuthFooter(
    text: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.padding(bottom = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )

        TextButton(
            onClick = onActionClick,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Text(
                text = actionText,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@ThemePreviews
@Composable
private fun AuthFooterPreviews() {
    InterMindTheme {
        Surface {
            AuthFooter(
                text = "Нет аккаунта",
                actionText = "Регистрация",
                onActionClick = {}
            )
        }
    }
}