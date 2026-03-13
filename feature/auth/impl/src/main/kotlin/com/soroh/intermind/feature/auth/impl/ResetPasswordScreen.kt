package com.soroh.intermind.feature.auth.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.soroh.intermind.feature.auth.impl.components.AuthTextField
import com.soroh.intermind.feature.auth.impl.components.Gradient

@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit = {},
    onPasswordResetSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var passwordValue by remember { mutableStateOf("") }
    var confirmPasswordValue by remember { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val passwordsMatch = passwordValue == confirmPasswordValue
    val isPasswordValid = passwordValue.length >= 6

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
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Кнопка назад
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Заголовок
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your new password",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Поле пароля
            AuthTextField(
                value = passwordValue,
                onValueChange = {
                    passwordValue = it
                    errorMessage = null
                },
                label = "New Password",
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Password,
                onImeAction = { },
                modifier = Modifier.fillMaxWidth(),
                isError = passwordValue.isNotEmpty() && !isPasswordValid
            )

            if (passwordValue.isNotEmpty() && !isPasswordValid) {
                Text(
                    text = "Password must be at least 6 characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Поле подтверждения пароля
            AuthTextField(
                value = confirmPasswordValue,
                onValueChange = {
                    confirmPasswordValue = it
                    errorMessage = null
                },
                label = "Confirm Password",
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password,
                onImeAction = { },
                modifier = Modifier.fillMaxWidth(),
                isError = confirmPasswordValue.isNotEmpty() && !passwordsMatch
            )

            if (confirmPasswordValue.isNotEmpty() && !passwordsMatch) {
                Text(
                    text = "Passwords do not match",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            // Общая ошибка
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка сброса
            Button(
                onClick = {
                    if (passwordValue == confirmPasswordValue && isPasswordValid) {
                        viewModel.resetPassword(newPassword = passwordValue)
                        onPasswordResetSuccess()
                    } else {
                        errorMessage = "Please ensure passwords match and are valid"
                    }
                },
                enabled = passwordValue.isNotBlank() && confirmPasswordValue.isNotBlank() && passwordsMatch && isPasswordValid && !isLoading,
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
                        text = "Reset Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
