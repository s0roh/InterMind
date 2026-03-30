package com.soroh.intermind.core.data.repository

import android.os.Build
import android.util.Log
import com.soroh.intermind.core.data.mapper.AuthErrorMapper
import com.soroh.intermind.core.data.model.AuthResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

/**
 * Implements a [AuthRepository]
 */
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    private val userDevicesTable
        get() = supabase.postgrest["user_devices"]

    override fun signUpWithEmail(
        name: String,
        email: String,
        password: String
    ): Flow<AuthResponse> = authResponseFlow {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("first_name", name)
            }
        }
        signInWithEmail(email, password).collect { emit(it) }
    }

    override fun signInWithEmail(
        email: String,
        password: String
    ): Flow<AuthResponse> = authResponseFlow {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        emit(AuthResponse.Success)
    }

    override fun loginWithGoogleToken(googleIdToken: String): Flow<AuthResponse> =
        authResponseFlow {
            supabase.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }
            emit(AuthResponse.Success)
        }

    override fun resetPassword(email: String): Flow<AuthResponse> = authResponseFlow {
        supabase.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = "app://intermind.com/reset-password"
        )
        emit(AuthResponse.Success)
    }

    override fun updatePassword(newPassword: String): Flow<AuthResponse> = authResponseFlow {
        supabase.auth.updateUser {
            password = newPassword
        }
        emit(AuthResponse.Success)
    }

    override fun signOut(): Flow<AuthResponse> = authResponseFlow {
        supabase.auth.signOut()
        emit(AuthResponse.Success)
    }

    override suspend fun saveFcmToken(token: String) = withContext(Dispatchers.IO) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext

        try {
            userDevicesTable.upsert(
                buildJsonObject {
                    put("user_id", userId)
                    put("fcm_token", token)
                    put("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
                },
            ) {
                onConflict = "fcm_token"
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Failed to save token", e)
        }
    }

    private fun authResponseFlow(
        block: suspend FlowCollector<AuthResponse>.() -> Unit
    ): Flow<AuthResponse> =
        flow(block)
            .catch { throwable -> emit(AuthResponse.Error(AuthErrorMapper.map(throwable))) }
            .flowOn(Dispatchers.IO)
}