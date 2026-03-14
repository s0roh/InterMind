package com.soroh.intermind.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.ui.graphics.vector.ImageVector
import com.soroh.intermind.feature.decks.api.navigation.DecksNavKey
import com.soroh.intermind.feature.explore.api.navigation.ExploreNavKey
import com.soroh.intermind.feature.history.api.navigation.HistoryNavKey
import com.soroh.intermind.feature.profile.api.navigation.ProfileNavKey

/**
 * Type for the top level navigation items in the application. Contains UI information about the
 * current route that is used in the top app bar and common navigation UI.
 *
 * @param selectedIcon The icon to be displayed in the navigation UI when this destination is
 * selected.
 * @param unselectedIcon The icon to be displayed in the navigation UI when this destination is
 * not selected.
 * @param iconTextId Text that to be displayed in the navigation UI.
 * @param titleTextId Text that is displayed on the top app bar.
 */
data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconText: String,
    val titleText: String,
//    @StringRes val iconTextId: Int,
//    @StringRes val titleTextId: Int,
)

val EXPLORE = TopLevelNavItem(
    selectedIcon = Icons.Filled.Upcoming,
    unselectedIcon = Icons.Outlined.Upcoming,
    iconText = "Explore",
    titleText = "InterMind",
)

val DECKS = TopLevelNavItem(
    selectedIcon = Icons.Filled.Bookmarks,
    unselectedIcon = Icons.Outlined.Bookmarks,
    iconText = "Decks",
    titleText = "Decks",
)

val HISTORY = TopLevelNavItem(
    selectedIcon = Icons.Filled.History,
    unselectedIcon = Icons.Outlined.History,
    iconText = "History",
    titleText = "History",
)

val PROFILE = TopLevelNavItem(
    selectedIcon = Icons.Filled.Person,
    unselectedIcon = Icons.Outlined.Person,
    iconText = "Profile",
    titleText = "Profile",
)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    ExploreNavKey to EXPLORE,
    DecksNavKey to DECKS,
    HistoryNavKey to HISTORY,
    ProfileNavKey to PROFILE,
)
