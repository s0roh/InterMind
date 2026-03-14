package com.soroh.intermind.feature.auth.impl.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.soroh.intermind.feature.auth.impl.BuildConfig

internal object GoogleAuthUiClient {

    suspend fun signIn(context: Context): String? {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)

            val result = credentialManager.getCredential(context, request)

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                googleIdTokenCredential.idToken
            } else {
                Log.e("GoogleAuth", "Получен неизвестный тип кредов")
                null
            }
        } catch (_: GetCredentialCancellationException) {
            Log.d("GoogleAuth", "Вход отменен пользователем")
            null
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuth", "Ошибка CredentialManager: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Неизвестная ошибка: ${e.message}")
            null
        }
    }
}