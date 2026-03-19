package com.soroh.intermind.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.soroh.intermind.R
import com.soroh.intermind.core.designsystem.icon.InterMindIcons
import com.soroh.intermind.feature.decks.api.navigation.DecksNavKey
import com.soroh.intermind.feature.explore.api.navigation.ExploreNavKey
import com.soroh.intermind.feature.history.api.navigation.HistoryNavKey
import com.soroh.intermind.feature.profile.api.navigation.ProfileNavKey
import com.soroh.intermind.feature.decks.api.R as decksR
import com.soroh.intermind.feature.explore.api.R as exploreR
import com.soroh.intermind.feature.history.api.R as historyR
import com.soroh.intermind.feature.profile.api.R as profileR

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
    @param:StringRes val iconTextId: Int,
    @param:StringRes val titleTextId: Int,
)

val EXPLORE = TopLevelNavItem(
    selectedIcon = InterMindIcons.FilledPublic,
    unselectedIcon = InterMindIcons.OutlinedPublic,
    iconTextId = exploreR.string.feature_explore_api_title,
    titleTextId = R.string.app_name,
)

val DECKS = TopLevelNavItem(
    selectedIcon = InterMindIcons.FilledLocal,
    unselectedIcon = InterMindIcons.OutlinedLocal,
    iconTextId = decksR.string.feature_decks_api_title,
    titleTextId = decksR.string.feature_decks_api_title,
)

val HISTORY = TopLevelNavItem(
    selectedIcon = InterMindIcons.FilledHistory,
    unselectedIcon = InterMindIcons.OutlinedHistory,
    iconTextId = historyR.string.feature_history_api_title,
    titleTextId = historyR.string.feature_history_api_title,
)

val PROFILE = TopLevelNavItem(
    selectedIcon = InterMindIcons.FilledPerson,
    unselectedIcon = InterMindIcons.OutlinedPerson,
    iconTextId = profileR.string.feature_profile_api_title,
    titleTextId = profileR.string.feature_profile_api_title,
)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    ExploreNavKey to EXPLORE,
    DecksNavKey to DECKS,
    HistoryNavKey to HISTORY,
    ProfileNavKey to PROFILE,
)
