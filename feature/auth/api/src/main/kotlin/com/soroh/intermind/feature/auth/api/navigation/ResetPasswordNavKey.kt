package com.soroh.intermind.feature.auth.api.navigation

import androidx.navigation3.runtime.NavKey
import com.soroh.intermind.core.navigation.deeplink.DeepLinkKey
import kotlinx.serialization.Serializable

@Serializable
object ResetPasswordNavKey : DeepLinkKey {
    override val parent: NavKey = ForgotPasswordNavKey
}
