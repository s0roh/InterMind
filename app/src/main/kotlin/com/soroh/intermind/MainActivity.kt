package com.soroh.intermind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.soroh.intermind.core.designsystem.component.CountdownSnackbar
import com.soroh.intermind.core.designsystem.theme.InterMindTheme
import com.soroh.intermind.core.designsystem.util.SnackbarController
import com.soroh.intermind.feature.auth.api.navigation.ResetPasswordNavKey
import com.soroh.intermind.feature.auth.impl.AuthScreen
import com.soroh.intermind.ui.InterMindScreen
import com.soroh.intermind.util.DeepLinkHandler
import com.soroh.intermind.util.ObserveAsEvents
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch
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
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                ObserveAsEvents(
                    flow = SnackbarController.events,
                    snackbarHostState
                ) { event ->
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()

                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.action?.name,
                            duration = SnackbarDuration.Indefinite
                        )

                        when (result) {
                            SnackbarResult.ActionPerformed -> event.action?.action?.invoke()
                            SnackbarResult.Dismissed -> event.action?.dismiss?.invoke()
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                initialValue = SwipeToDismissBoxValue.Settled,
                                positionalThreshold = { totalDistance -> totalDistance * 0.3f }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {},
                                content = {
                                    CountdownSnackbar(data)
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    val sessionStatus by supabase.auth.sessionStatus.collectAsState()
                    val isAuthenticated = sessionStatus is SessionStatus.Authenticated

                    var currentDeepLinkKey by remember { mutableStateOf(initialDeepLinkKey) }
                    val isResettingPassword = currentDeepLinkKey is ResetPasswordNavKey

                    Crossfade(
                        targetState = isAuthenticated,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = paddingValues.calculateBottomPadding()),
                        label = "RootNavigation"
                    ) { authenticated ->
                        if (authenticated && !isResettingPassword) {
                            InterMindScreen()
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
}