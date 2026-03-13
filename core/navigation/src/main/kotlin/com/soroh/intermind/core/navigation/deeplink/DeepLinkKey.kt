package com.soroh.intermind.core.navigation.deeplink

import androidx.navigation3.runtime.NavKey

interface DeepLinkKey: NavKey {
    val parent: NavKey
}