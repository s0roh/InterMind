package com.soroh.intermind.core.ui.util

import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
internal fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}