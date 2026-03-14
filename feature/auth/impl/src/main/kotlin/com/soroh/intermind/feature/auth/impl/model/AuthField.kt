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
        override val onValueChange: (String) -> Unit,
        val isError: Boolean = false,
        val supportingText: String? = null
    ) : AuthField()

    data class Password(
        override val value: String,
        override val onValueChange: (String) -> Unit,
        val label: String = "Пароль",
        val isVisible: Boolean,
        val onVisibilityChange: (Boolean) -> Unit,
        val isError: Boolean = false,
        val supportingText: String? = null
    ) : AuthField()
}