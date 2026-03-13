package com.soroh.intermind.feature.auth.impl.util

import android.net.Uri
import com.soroh.intermind.core.navigation.deeplink.DeepLinkKey
import com.soroh.intermind.feature.auth.api.navigation.ForgotPasswordNavKey

internal fun parseDeepLink(uri: Uri): DeepLinkKey? {
    val host = uri.host
    val path = uri.path

    return when {
        // app://intermind.com/forgot-password
        host == "intermind.com" && path?.contains(
            "/forgot-password",
            ignoreCase = true
        ) == true -> {
            ForgotPasswordNavKey
        }

        else -> null
    }
}