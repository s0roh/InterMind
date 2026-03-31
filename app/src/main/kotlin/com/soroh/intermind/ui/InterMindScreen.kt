package com.soroh.intermind.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.soroh.intermind.core.designsystem.component.InterMindNavigationBar
import com.soroh.intermind.core.designsystem.component.InterMindNavigationBarItem
import com.soroh.intermind.core.navigation.NavigationState
import com.soroh.intermind.core.navigation.Navigator
import com.soroh.intermind.core.navigation.deeplink.DeepLinkKey
import com.soroh.intermind.core.navigation.rememberNavigationState
import com.soroh.intermind.core.navigation.toEntries
import com.soroh.intermind.fearure.addeditcard.impl.navigation.addEditCardEntry
import com.soroh.intermind.feature.addeditdeck.impl.navigation.addEditDeckEntry
import com.soroh.intermind.feature.deckdetails.impl.navigation.deckDetailsEntry
import com.soroh.intermind.feature.decks.impl.navigation.decksEntry
import com.soroh.intermind.feature.explore.api.navigation.ExploreNavKey
import com.soroh.intermind.feature.explore.impl.navigation.exploreEntry
import com.soroh.intermind.feature.history.impl.navigation.historyEntry
import com.soroh.intermind.feature.profile.impl.navigation.profileEntry
import com.soroh.intermind.feature.statistic.impl.navigation.statisticEntry
import com.soroh.intermind.feature.training.impl.navigation.trainingEntry
import com.soroh.intermind.feature.trainingmodesettings.impl.navigation.trainingModeSettingsEntry
import com.soroh.intermind.navigation.TOP_LEVEL_NAV_ITEMS

@Composable
fun InterMindScreen(
    modifier: Modifier = Modifier,
    deepLinkKey: DeepLinkKey?,
    viewModel: InterMindViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.syncFcmToken()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.syncFcmToken()
            }
        } else {
            viewModel.syncFcmToken()
        }
    }

    val navigationState = rememberNavigationState(ExploreNavKey, TOP_LEVEL_NAV_ITEMS.keys)
    val navigator = remember { Navigator(navigationState) }
    val shouldShowBottomBar = navigationState.currentKey in TOP_LEVEL_NAV_ITEMS.keys

    LaunchedEffect(deepLinkKey) {
        if (deepLinkKey is NavKey) {
            Log.d("!@#", "Force navigating to: $deepLinkKey")
            navigator.navigate(deepLinkKey)
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (shouldShowBottomBar) {
                MainBottomNavigation(
                    navigationState = navigationState,
                    onNavigate = { key -> navigator.navigate(key) }
                )
            }
        }
    ) { paddingValues ->

        val entryProvider = entryProvider<NavKey> {
            exploreEntry(navigator)
            decksEntry(navigator)
            historyEntry(navigator)
            profileEntry(navigator)
            addEditDeckEntry(navigator)
            addEditCardEntry(navigator)
            deckDetailsEntry(navigator)
            trainingEntry(navigator)
            trainingModeSettingsEntry(navigator)
            statisticEntry(navigator)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() }
            )
        }
    }
}

@Composable
private fun MainBottomNavigation(
    navigationState: NavigationState,
    onNavigate: (NavKey) -> Unit
) {
    val currentTopLevelKey = navigationState.currentTopLevelKey

    InterMindNavigationBar {
        TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
            val selected = navKey == currentTopLevelKey

            InterMindNavigationBarItem(
                selected = selected,
                onClick = { onNavigate(navKey) },
                icon = {
                    Icon(
                        imageVector = navItem.unselectedIcon,
                        contentDescription = stringResource(navItem.iconTextId)
                    )
                },
                selectedIcon = {
                    Icon(
                        imageVector = navItem.selectedIcon,
                        contentDescription = stringResource(navItem.iconTextId)
                    )
                },
                label = { Text(stringResource(navItem.iconTextId)) }
            )
        }
    }
}