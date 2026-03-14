package com.soroh.intermind.core.data.model

sealed interface AuthResponse {
    data object Success : AuthResponse
    data class Error(val error: AuthError) : AuthResponse
}

sealed interface AuthError {

    val message: String

    data object InvalidCredentials : AuthError {
        override val message = "Неверный email или пароль"
    }

    data object EmailAlreadyExists : AuthError {
        override val message = "Пользователь с таким email уже существует"
    }

    data object SamePassword : AuthError {
        override val message = "Новый пароль должен отличаться от старого"
    }

    data class Unknown(
        val raw: String?
    ) : AuthError {
        override val message = raw ?: "Неизвестная ошибка"
    }
}