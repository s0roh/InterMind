package com.soroh.intermind.core.data.util

import java.text.Normalizer

internal fun normalizeText(text: String): String {
    val normalized = Normalizer.normalize(text.lowercase().trim(), Normalizer.Form.NFD)
    return normalized.replace(Regex("[^\\p{L}\\d ]"), "")
}