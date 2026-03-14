package com.soroh.intermind.core.data.mapper

import com.soroh.intermind.core.data.model.AuthError

object AuthErrorMapper {

    fun map(throwable: Throwable): AuthError {

        val message = throwable.message
            ?: throwable.localizedMessage

        return when {
            message?.contains("invalid_credentials", true) == true ->
                AuthError.InvalidCredentials

            message?.contains("already registered", true) == true ->
                AuthError.EmailAlreadyExists

            message?.contains("same_password", true) == true ->
                AuthError.SamePassword

            else ->
                AuthError.Unknown(message)
        }
    }
}