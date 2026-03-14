package com.soroh.intermind.feature.auth.impl.util

import android.net.Uri
import com.soroh.intermind.core.navigation.deeplink.DeepLinkKey
import com.soroh.intermind.core.navigation.deeplink.FeatureDeepLinkParser
import com.soroh.intermind.feature.auth.api.navigation.ResetPasswordNavKey
import jakarta.inject.Inject

internal class AuthDeepLinkParser @Inject constructor() : FeatureDeepLinkParser {

    override fun parse(uri: Uri): DeepLinkKey? {
        val host = uri.host
        val path = uri.path

        return when {
            host == "intermind.com" && path?.contains(
                "/reset-password",
                ignoreCase = true
            ) == true -> {
                ResetPasswordNavKey
            }

            else -> null
        }
    }
}