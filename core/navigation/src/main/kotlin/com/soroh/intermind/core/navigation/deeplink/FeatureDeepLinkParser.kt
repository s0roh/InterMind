package com.soroh.intermind.core.navigation.deeplink

import android.net.Uri

/**
 * Interface for parsing deep links into feature-specific navigation keys.
 */
interface FeatureDeepLinkParser {

    /**
     * Parses a URI and returns a corresponding [DeepLinkKey] if the feature
     * knows how to handle this URI.
     *
     * @param uri The deep link URI to parse
     * @return A [DeepLinkKey] if the URI is recognized and can be handled,
     *         or null if this URI doesn't belong to this feature
     */
    fun parse(uri: Uri): DeepLinkKey?
}