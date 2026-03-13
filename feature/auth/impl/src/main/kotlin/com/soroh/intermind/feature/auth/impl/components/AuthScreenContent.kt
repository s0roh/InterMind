package com.soroh.intermind.feature.auth.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.soroh.intermind.feature.auth.impl.model.AuthField

@Composable
internal fun AuthScreenContent(
    title: String,
    subtitle: String,
    fields: List<AuthField>,
    buttonText: String,
    onButtonClick: () -> Unit,
    onGoogleClick: () -> Unit,
    bottomText: String,
    bottomTextAction: String,
    onBottomTextClick: () -> Unit,
    onForgotPasswordClick: (() -> Unit)? = null,
    isLoading: Boolean = false
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        contentAlignment = Alignment.TopCenter
    ) {
        Gradient()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Google кнопка
            GoogleSignInButton(onClick = onGoogleClick)

            // Разделитель
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
                Text(
                    text = "or",
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }

            // Поля формы
            fields.forEachIndexed { index, field ->
                when (field) {
                    is AuthField.Username -> {
                        AuthTextField(
                            value = field.value,
                            onValueChange = field.onValueChange,
                            label = "Username",
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
                            label = "Email",
                            imeAction = if (index < fields.lastIndex) ImeAction.Next else ImeAction.Done,
                            keyboardType = KeyboardType.Email,
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

                    is AuthField.Password -> {
                        AuthTextField(
                            value = field.value,
                            onValueChange = field.onValueChange,
                            label = "Password",
                            visualTransformation = if (field.isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { field.onVisibilityChange(!field.isVisible) }) {
                                    Icon(
                                        imageVector = if (field.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (field.isVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            imeAction = ImeAction.Done,
                            onImeAction = { focusManager.clearFocus() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (index < fields.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка действия
            Button(
                onClick = onButtonClick,
                enabled = fields.all { field ->
                    when (field) {
                        is AuthField.Username -> field.value.isNotBlank()
                        is AuthField.Email -> field.value.isNotBlank()
                        is AuthField.Password -> field.value.isNotBlank()
                    }
                } && !isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Ссылка "Forgot Password?" только на экране входа
            if (onForgotPasswordClick != null) {
                TextButton(
                    onClick = onForgotPasswordClick,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Нижний текст
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bottomText,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onBottomTextClick,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = bottomTextAction,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}