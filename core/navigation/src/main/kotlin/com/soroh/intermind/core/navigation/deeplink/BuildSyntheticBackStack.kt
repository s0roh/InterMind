package com.soroh.intermind.core.navigation.deeplink

import androidx.navigation3.runtime.NavKey

fun buildSyntheticBackStack(
    deeplinkKey: DeepLinkKey,
): List<NavKey> {
    return buildList {
        var node: NavKey? = deeplinkKey
        while (node != null) {
            // ensure the parent is added to the start of the list
            add(0, node)
            val parent = if (node is DeepLinkKey) {
                node.parent
            } else null
            node = parent
        }
    }
}