package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.mapper.AuthErrorMapper
import com.soroh.intermind.core.data.model.AuthResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

/**
 * Implements a [AuthRepository]
 */
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override fun signUpWithEmail(
        name: String,
        email: String,
        password: String
    ): Flow<AuthResponse> = authResponseFlow {
        supabaseClient.auth.signUpWith(Email) {
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
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        emit(AuthResponse.Success)
    }

    override fun loginWithGoogleToken(googleIdToken: String): Flow<AuthResponse> =
        authResponseFlow {
            supabaseClient.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }
            emit(AuthResponse.Success)
        }

    override fun resetPassword(email: String): Flow<AuthResponse> = authResponseFlow {
        supabaseClient.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = "app://intermind.com/reset-password"
        )
        emit(AuthResponse.Success)
    }

    override fun updatePassword(newPassword: String): Flow<AuthResponse> = authResponseFlow {
        supabaseClient.auth.updateUser {
            password = newPassword
        }
        emit(AuthResponse.Success)
    }

    override fun signOut(): Flow<AuthResponse> = authResponseFlow {
        supabaseClient.auth.signOut()
        emit(AuthResponse.Success)
    }

    private fun authResponseFlow(
        block: suspend FlowCollector<AuthResponse>.() -> Unit
    ): Flow<AuthResponse> =
        flow(block)
            .catch { throwable -> emit(AuthResponse.Error(AuthErrorMapper.map(throwable))) }
            .flowOn(Dispatchers.IO)
}