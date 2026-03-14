package com.soroh.intermind.feature.auth.impl.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.soroh.intermind.core.designsystem.component.ThemePreviews
import com.soroh.intermind.core.designsystem.theme.InterMindTheme
import com.soroh.intermind.feature.auth.api.R
import com.soroh.intermind.feature.auth.impl.model.AuthField

@Composable
internal fun AuthForm(
    fields: List<AuthField>
) {
    val focusManager = LocalFocusManager.current

    fields.forEachIndexed { index, field ->
        when (field) {
            is AuthField.Username -> {
                AuthTextField(
                    value = field.value,
                    onValueChange = field.onValueChange,
                    label = stringResource(R.string.feature_auth_api_username),
                    imeAction = if (index < fields.lastIndex) ImeAction.Next else ImeAction.Done,
                    keyboardType = KeyboardType.Text,
                    onImeAction = {
                        if (index < fields.lastIndex) {
                            focusManager.moveFocus(FocusDirection.Down)
                        } else {
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is AuthField.Email -> {
                AuthTextField(
                    value = field.value,
                    onValueChange = field.onValueChange,
                    label = stringResource(R.string.feature_auth_api_email),
                    imeAction = if (index < fields.lastIndex) ImeAction.Next else ImeAction.Done,
                    keyboardType = KeyboardType.Email,
                    onImeAction = {
                        if (index < fields.lastIndex) {
                            focusManager.moveFocus(FocusDirection.Down)
                        } else {
                            focusManager.clearFocus()
                        }
                    },
                    isError = field.isError,
                    supportingText = field.supportingText,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is AuthField.Password -> {
                AuthTextField(
                    value = field.value,
                    onValueChange = field.onValueChange,
                    label = field.label,
                    visualTransformation = if (field.isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { field.onVisibilityChange(!field.isVisible) }) {
                            Icon(
                                imageVector = if (field.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (field.isVisible) {
                                    stringResource(R.string.feature_auth_api_hide_password)
                                } else {
                                    stringResource(R.string.feature_auth_api_show_password)
                                }
                            )
                        }
                    },
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() },
                    isError = field.isError,
                    supportingText = field.supportingText,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (index < fields.lastIndex) {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@ThemePreviews
@Composable
private fun AuthFormPreviews() {
    InterMindTheme {
        Surface {
            Column {
                AuthForm(
                    fields = listOf(
                        AuthField.Username(
                            value = "",
                            onValueChange = { }
                        ),
                        AuthField.Email(
                            value = "",
                            onValueChange = {},
                            isError = false,
                            supportingText = null
                        ),
                        AuthField.Password(
                            value = "",
                            onValueChange = {},
                            isVisible = false,
                            onVisibilityChange = {},
                            isError = false,
                            supportingText = null
                        )
                    )
                )
            }
        }
    }
}