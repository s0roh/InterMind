package com.soroh.intermind.feature.auth.impl.model

internal sealed class AuthField {
    abstract val value: String
    abstract val onValueChange: (String) -> Unit

    data class Username(
        override val value: String,
        override val onValueChange: (String) -> Unit
    ) : AuthField()

    data class Email(
        override val value: String,
        override val onValueChange: (String) -> Unit
    ) : AuthField()

    data class Password(
        override val value: String,
        override val onValueChange: (String) -> Unit,
        val isVisible: Boolean,
        val onVisibilityChange: (Boolean) -> Unit
    ) : AuthField()
}