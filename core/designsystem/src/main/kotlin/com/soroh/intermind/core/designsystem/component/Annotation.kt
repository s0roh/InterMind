package com.soroh.intermind.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multipreview annotation that represents light and dark themes. Add this annotation to a
 * composable to render the both themes.
 */
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark theme")
annotation class ThemePreviews

/**
 * Multipreview annotation that represents light and dark themes and device sizes. Add this annotation to a composable
 * to render various devices.
 */
@Preview(
    name = "Light theme",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Preview(
    name = "Dark theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    device = "spec:width=412dp,height=915dp,dpi=420", showSystemUi = false
)
annotation class ThemeDevicePreviews