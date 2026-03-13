package com.soroh.intermind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.soroh.intermind.core.designsystem.theme.InterMindTheme
import com.soroh.intermind.feature.auth.api.navigation.ResetPasswordNavKey
import com.soroh.intermind.feature.auth.impl.AuthScreen
import com.soroh.intermind.ui.InterMindApp
import com.soroh.intermind.util.DeepLinkHandler
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
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
        supabase.handleDeeplinks(intent)
        enableEdgeToEdge()

        val initialDeepLinkKey = intent?.data?.let { deepLinkHandler.handleDeepLink(it) }

        setContent {
            InterMindTheme {
                val sessionStatus by supabase.auth.sessionStatus.collectAsState()
                val isAuthenticated = sessionStatus is SessionStatus.Authenticated

                var currentDeepLinkKey by remember { mutableStateOf(initialDeepLinkKey) }

                val isResettingPassword = currentDeepLinkKey is ResetPasswordNavKey

                Crossfade(targetState = isAuthenticated) { authenticated ->
                    if (authenticated && !isResettingPassword) {
                        InterMindApp()
                    } else {
                        AuthScreen(
                            deepLinkKey = currentDeepLinkKey,
                            onPasswordResetSuccess = { currentDeepLinkKey = null }
                        )
                    }
                }
            }
        }
    }
}