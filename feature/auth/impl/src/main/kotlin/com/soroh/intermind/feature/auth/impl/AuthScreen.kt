package com.soroh.intermind.feature.auth.impl

import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.soroh.intermind.feature.auth.impl.components.AuthTextField
import com.soroh.intermind.feature.auth.impl.components.GoogleSignInButton
import com.soroh.intermind.feature.auth.impl.components.Gradient
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TAG = "AuthScreen"

@Composable
fun AuthScreen() {
    var isRegistering by remember { mutableStateOf(true) }

    if (isRegistering) {
        RegisterScreen(
            onNavigateToLogin = { isRegistering = false }
        )
    } else {
        LoginScreen(
            onNavigateToRegister = { isRegistering = true }
        )
    }
}

@Composable
private fun RegisterScreen(
    onNavigateToLogin: () -> Unit
) {
    var usernameValue by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authManager = remember { AuthManager(context, supabase) }
    val coroutineScope = rememberCoroutineScope()

    AuthScreenContent(
        title = "Create Account",
        subtitle = "Sign up to get started",
        fields = listOf(
            AuthField.Username(
                value = usernameValue,
                onValueChange = { usernameValue = it }
            ),
            AuthField.Email(
                value = emailValue,
                onValueChange = { emailValue = it }
            ),
            AuthField.Password(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                isVisible = isPasswordVisible,
                onVisibilityChange = { isPasswordVisible = it }
            )
        ),
        buttonText = "Sign Up",
        onButtonClick = {
            isLoading = true
            authManager.signUpWithEmail(emailValue, passwordValue)
                .onEach { result ->
                    isLoading = false
                    when (result) {
                        is AuthResponse.Success -> Log.d(TAG, "Email Sign Up Success")
                        is AuthResponse.Error -> Log.e(
                            TAG,
                            "Email Sign Up Error: ${result.message}"
                        )
                    }
                }
                .launchIn(coroutineScope)
        },
        onGoogleClick = {
            authManager.loginGoogleUser()
                .onEach { result ->
                    when (result) {
                        is AuthResponse.Success -> Log.d(TAG, "Google Success")
                        is AuthResponse.Error -> Log.e(TAG, "Google Error: ${result.message}")
                    }
                }
                .launchIn(coroutineScope)
        },
        bottomText = "Already have an account? ",
        bottomTextAction = "Log in",
        onBottomTextClick = onNavigateToLogin,
        isLoading = isLoading
    )
}

@Composable
private fun LoginScreen(
    onNavigateToRegister: () -> Unit,
) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authManager = remember { AuthManager(context, supabase) }
    val coroutineScope = rememberCoroutineScope()

    AuthScreenContent(
        title = "Welcome Back",
        subtitle = "Sign in to continue",
        fields = listOf(
            AuthField.Email(
                value = emailValue,
                onValueChange = { emailValue = it }
            ),
            AuthField.Password(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                isVisible = isPasswordVisible,
                onVisibilityChange = { isPasswordVisible = it }
            )
        ),
        buttonText = "Log in",
        onButtonClick = {
            isLoading = true
            authManager.signInWithEmail(emailValue, passwordValue)
                .onEach { result ->
                    isLoading = false
                    when (result) {
                        is AuthResponse.Success -> Log.d(TAG, "Email Sign In Success")
                        is AuthResponse.Error -> Log.e(
                            TAG,
                            "Email Sign In Error: ${result.message}"
                        )
                    }
                }
                .launchIn(coroutineScope)
        },
        onGoogleClick = {
            authManager.loginGoogleUser()
                .onEach { result ->
                    when (result) {
                        is AuthResponse.Success -> Log.d(TAG, "Google Success")
                        is AuthResponse.Error -> Log.e(TAG, "Google Error: ${result.message}")
                    }
                }
                .launchIn(coroutineScope)
        },
        bottomText = "Don't have an account? ",
        bottomTextAction = "Sign up",
        onBottomTextClick = onNavigateToRegister,
        isLoading = isLoading
    )
}

// Модель для полей формы
private sealed class AuthField {
    abstract val value: String
    abstract val onValueChange: (String) -> Unit

    data class Username(
        override val value: String,
        override val onValueChange: (String) -> Unit
    ) : AuthField()

    data class Email(
        override val value: String,
        override val onValueChange: (String) -> Unit
    ) : AuthField()

    data class Password(
        override val value: String,
        override val onValueChange: (String) -> Unit,
        val isVisible: Boolean,
        val onVisibilityChange: (Boolean) -> Unit
    ) : AuthField()
}

@Composable
private fun AuthScreenContent(
    title: String,
    subtitle: String,
    fields: List<AuthField>,
    buttonText: String,
    onButtonClick: () -> Unit,
    onGoogleClick: () -> Unit,
    bottomText: String,
    bottomTextAction: String,
    onBottomTextClick: () -> Unit,
    isLoading: Boolean = false
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier.fillMaxSize()
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