package com.soroh.intermind.feature.decks.impl.util

import android.net.Uri
import com.soroh.intermind.core.navigation.deeplink.DeepLinkKey
import com.soroh.intermind.core.navigation.deeplink.FeatureDeepLinkParser
import com.soroh.intermind.feature.decks.api.navigation.DecksNavKey
import javax.inject.Inject

internal class DecksDeepLinkParser @Inject constructor() : FeatureDeepLinkParser {
    override fun parse(uri: Uri): DeepLinkKey? {
        val host = uri.host
        val path = uri.path

        return when {
            host == "intermind.com" && path?.contains(
                "/decks",
                ignoreCase = true
            ) == true -> {
                DecksNavKey
            }

            else -> null
        }
    }
}