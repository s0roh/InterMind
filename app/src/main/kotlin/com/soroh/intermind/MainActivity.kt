package com.soroh.intermind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.soroh.intermind.core.designsystem.theme.InterMindTheme
import com.soroh.intermind.feature.auth.impl.AuthScreen
import com.soroh.intermind.ui.InterMindApp
import com.soroh.intermind.util.DeepLinkHandler
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabase: SupabaseClient

    @Inject
    lateinit var deepLinkHandler: DeepLinkHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialDeepLinkKey = intent?.data?.let { deepLinkHandler.handleDeepLink(it) }

        setContent {
            InterMindTheme {
                val isAuthenticated = rememberUserAuthentication(supabase)

                var currentDeepLinkKey by remember { mutableStateOf(initialDeepLinkKey) }

                Crossfade(targetState = isAuthenticated) { authenticated ->
                    if (authenticated) {
                        InterMindApp()
                    } else {
                        AuthScreen(currentDeepLinkKey)
                    }
                }
            }
        }
    }
}

@Composable
fun rememberUserAuthentication(supabase: SupabaseClient): Boolean {
    val sessionStatus by supabase.auth.sessionStatus.collectAsState()
    return sessionStatus is SessionStatus.Authenticated
}