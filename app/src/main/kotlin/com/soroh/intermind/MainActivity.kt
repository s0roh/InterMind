package com.soroh.intermind

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.soroh.intermind.core.designsystem.theme.InterMindTheme
import com.soroh.intermind.feature.auth.impl.AuthScreen
import com.soroh.intermind.feature.auth.impl.supabase
import com.soroh.intermind.ui.InterMindApp
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            InterMindTheme {
                val isAuthenticated = rememberUserAuthentication()
                Log.d(TAG, "User authenticated: $isAuthenticated")

                if (isAuthenticated) {
                    InterMindApp()
                    // Показать главный экран
                    // MainScreen()
                } else {
                    AuthScreen()
                    // Показать экран авторизации
                    // AuthScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreenWithLogout() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                // Выход из аккаунта
                scope.launch {
                    try {
                        supabase.auth.signOut()
                        Toast.makeText(context, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при выходе: ${e.message}")
                        Toast.makeText(context, "Ошибка при выходе", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        ) {
            Text("Выйти из аккаунта")
        }
    }
}

@Composable
fun rememberUserAuthentication(): Boolean {
    var isAuthenticated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        supabase.auth.sessionStatus.collect { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    isAuthenticated = true
                    Log.d(TAG, "Received new authenticated session.")
                }

                SessionStatus.Initializing -> {
                    Log.d(TAG, "Session initializing...")
                    // Во время инициализации оставляем предыдущее состояние
                }

                is SessionStatus.NotAuthenticated -> {
                    isAuthenticated = false
                    if (status.isSignOut) {
                        Log.d(TAG, "User signed out")
                    } else {
                        Log.d(TAG, "User not signed in")
                    }
                }

                is SessionStatus.RefreshFailure -> {
                    isAuthenticated = false
                    Log.e(TAG, "Session expired and could not be refreshed")
                }
            }
        }
    }

    return isAuthenticated
}