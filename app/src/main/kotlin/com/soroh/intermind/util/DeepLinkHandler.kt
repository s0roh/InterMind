package com.soroh.intermind.util

import android.net.Uri
import com.soroh.intermind.core.navigation.deeplink.DeepLinkKey
import com.soroh.intermind.core.navigation.deeplink.FeatureDeepLinkParser
import javax.inject.Inject

class DeepLinkHandler @Inject constructor(
    private val deepLinkParsers: Set<@JvmSuppressWildcards FeatureDeepLinkParser>
) {
    fun handleDeepLink(uri: Uri): DeepLinkKey? {
        return deepLinkParsers.firstNotNullOfOrNull { parser ->
            parser.parse(uri)
        }
    }
}