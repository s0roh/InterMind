package com.soroh.intermind.feature.auth.impl.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun <T : NavKey> NavBackStack<NavKey>.navigateSingleTop(key: T) {
    val existingIndex = indexOfFirst { it::class == key::class }

    if (existingIndex != -1) {
        while (lastIndex > existingIndex) {
            removeAt(lastIndex)
        }
    } else {
        add(key)
    }
}
