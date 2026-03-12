package com.soroh.intermind.feature.auth.impl

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.soroh.intermind.core.designsystem.component.ThemePreviews
import com.soroh.intermind.core.designsystem.theme.black
import com.soroh.intermind.core.designsystem.theme.darkGray
import com.soroh.intermind.core.designsystem.theme.darkPurple
import com.soroh.intermind.core.designsystem.theme.purple
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TAG = "AuthScreen"

sealed interface AuthScreenState {
    data object Register : AuthScreenState
    data object Login : AuthScreenState
    data object ForgotPassword : AuthScreenState
}

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<AuthScreenState>(AuthScreenState.Register) }

    when (currentScreen) {
        AuthScreenState.Register -> RegisterScreen(
            onNavigateToLogin = { currentScreen = AuthScreenState.Login }
        )
        AuthScreenState.Login -> LoginScreen(
            onNavigateToRegister = { currentScreen = AuthScreenState.Register },
            onNavigateToForgotPassword = { currentScreen = AuthScreenState.ForgotPassword }
        )
        AuthScreenState.ForgotPassword -> ForgotPasswordScreen(
            onNavigateBack = { currentScreen = AuthScreenState.Login }
        )
    }
}

val supabase = createSupabaseClient(
    supabaseUrl = "https://kmzvykougtykprzotyrr.supabase.co",
    supabaseKey = "sb_publishable_xHIxDmCM8N9kgAsnzC7JrQ_VtbhJ_LJ"
) {
    install(Auth)
}

@Composable
private fun RegisterScreen(
    onNavigateToLogin: () -> Unit
) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authManager = remember { AuthManager(context, supabase) }
    val coroutineScope = rememberCoroutineScope()

    AuthScreenContent(
        title = "Create An Account",
        subtitle = "Enter your personal data to create an account",
        emailValue = emailValue,
        onEmailChange = { emailValue = it },
        passwordValue = passwordValue,
        onPasswordChange = { passwordValue = it },
        buttonText = "Sign up",
        onButtonClick = {
            isLoading = true
            authManager.signUpWithEmail(emailValue, passwordValue)
                .onEach { result ->
                    isLoading = false
                    when (result) {
                        is AuthResponse.Success -> Log.d(TAG, "Email Sign Up Success")
                        is AuthResponse.Error -> Log.e(TAG, "Email Sign Up Error: ${result.message}")
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
        bottomText = buildAnnotatedString {
            append("Already have an account? ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                append("Log in")
            }
        },
        onBottomTextClick = onNavigateToLogin,
        isLoading = isLoading
    )
}

@Composable
private fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authManager = remember { AuthManager(context, supabase) }
    val coroutineScope = rememberCoroutineScope()

    AuthScreenContent(
        title = "Welcome Back",
        subtitle = "Enter your credentials to access your account",
        emailValue = emailValue,
        onEmailChange = { emailValue = it },
        passwordValue = passwordValue,
        onPasswordChange = { passwordValue = it },
        buttonText = "Log in",
        onButtonClick = {
            isLoading = true
            authManager.signInWithEmail(emailValue, passwordValue)
                .onEach { result ->
                    isLoading = false
                    when (result) {
                        is AuthResponse.Success -> Log.d(TAG, "Email Sign In Success")
                        is AuthResponse.Error -> Log.e(TAG, "Email Sign In Error: ${result.message}")
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
        bottomText = buildAnnotatedString {
            append("Don't have an account? ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                append("Sign up")
            }
        },
        onBottomTextClick = onNavigateToRegister,
        showForgotPassword = true,
        onForgotPasswordClick = onNavigateToForgotPassword,
        isLoading = isLoading
    )
}

@Composable
private fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit
) {
    var emailValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isEmailSent by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authManager = remember { AuthManager(context, supabase) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(black),
        contentAlignment = Alignment.TopCenter
    ) {
        Gradient()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 110.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEmailSent)
                    "Check your email for reset instructions"
                else
                    "Enter your email and we'll send you instructions to reset your password",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (!isEmailSent) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Email",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    TextField(
                        value = emailValue,
                        onValueChange = { emailValue = it },
                        placeholder = {
                            Text(
                                text = "your.email@example.com",
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = darkGray,
                            unfocusedContainerColor = darkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(35.dp))

                Button(
                    onClick = {
                        isLoading = true
                        authManager.resetPassword(emailValue)
                            .onEach { result ->
                                isLoading = false
                                when (result) {
                                    is AuthResponse.Success -> {
                                        isEmailSent = true
                                        Log.d(TAG, "Password reset email sent")
                                    }
                                    is AuthResponse.Error -> Log.e(TAG, "Reset Error: ${result.message}")
                                }
                            }
                            .launchIn(coroutineScope)
                    },
                    enabled = emailValue.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Send Reset Link",
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = black
                        )
                    }
                }
            } else {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Back to Login",
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = black
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            TextButton(onClick = onNavigateBack) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White.copy(alpha = 0.8f))) {
                            append("Remember your password? ")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                            append("Log in")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthScreenContent(
    title: String,
    subtitle: String,
    emailValue: String,
    onEmailChange: (String) -> Unit,
    passwordValue: String,
    onPasswordChange: (String) -> Unit,
    buttonText: String,
    onButtonClick: () -> Unit,
    onGoogleClick: () -> Unit,
    bottomText: AnnotatedString,
    onBottomTextClick: () -> Unit,
    showForgotPassword: Boolean = false,
    onForgotPasswordClick: (() -> Unit)? = null,
    isLoading: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(black),
        contentAlignment = Alignment.TopCenter
    ) {
        Gradient()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(40.dp))

            GoogleSignInButton(onClick = onGoogleClick)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )

                Text(
                    text = "Or",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
            }

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Email",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = emailValue,
                    onValueChange = onEmailChange,
                    placeholder = {
                        Text(
                            text = "your.email@example.com",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Password",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = passwordValue,
                    onValueChange = onPasswordChange,
                    placeholder = {
                        Text(
                            text = "Enter your password",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (showForgotPassword && onForgotPasswordClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onForgotPasswordClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Забыли пароль?",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onButtonClick,
                enabled = emailValue.isNotBlank() && passwordValue.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = buttonText,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = black
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            TextButton(onClick = onBottomTextClick) {
                Text(text = bottomText)
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(R.drawable.ic_google),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Sign In With Google",
            color = Color.White,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun Gradient() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.35f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(purple, darkPurple, black)
                )
            )
    )
}

sealed interface AuthResponse {
    data object Success : AuthResponse
    data class Error(val message: String?) : AuthResponse
}

class AuthManager(
    private val context: Context,
    private val supabase: SupabaseClient
) {

    fun signUpWithEmail(email: String, password: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            signInWithEmail(email, password).collect { emit(it) }
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun signInWithEmail(email: String, password: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun resetPassword(email: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.resetPasswordForEmail(email)
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun loginGoogleUser(): Flow<AuthResponse> = flow {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId("23416541006-p70terfeldmsdoemrdv3g9e8i7gvvj0r.apps.googleusercontent.com")
            .setAutoSelectEnabled(false)
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)

        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            supabase.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }
}

@ThemePreviews
@Composable
private fun RegisterPreview() {
    RegisterScreen(onNavigateToLogin = {})
}
