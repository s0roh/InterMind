package com.soroh.intermind.feature.auth.impl.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.soroh.intermind.core.designsystem.component.ThemePreviews
import com.soroh.intermind.core.designsystem.theme.InterMindTheme

@Composable
internal fun AuthTextField(
    value: String,
    label: String,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    supportingText: String? = null,
    onImeAction: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Unspecified,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = keyboardType
        ),
        keyboardActions = KeyboardActions(onAny = { onImeAction() })
    )
}

@ThemePreviews
@Composable
private fun AuthTextFieldPreviews() {
    InterMindTheme {
        Surface {
            Column {
                AuthTextField(
                    value = "John Doe",
                    label = "Имя пользователя",
                    imeAction = ImeAction.Next,
                    onValueChange = {},
                    keyboardType = KeyboardType.Text
                )
                AuthTextField(
                    value = "invalid-email",
                    label = "Email",
                    imeAction = ImeAction.Next,
                    onValueChange = {},
                    isError = true,
                    supportingText = "Неверный формат email",
                    keyboardType = KeyboardType.Email
                )

                AuthTextField(
                    value = "password123",
                    label = "Пароль",
                    imeAction = ImeAction.Done,
                    onValueChange = {},
                    visualTransformation = PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Скрыть пароль"
                            )
                        }
                    },
                    keyboardType = KeyboardType.Password
                )
            }
        }
    }
}