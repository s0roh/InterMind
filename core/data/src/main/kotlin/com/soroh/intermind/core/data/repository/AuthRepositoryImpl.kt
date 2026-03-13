package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.model.AuthResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    ): Flow<AuthResponse> = flow<AuthResponse> {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("first_name", name)
            }
        }
        signInWithEmail(email, password).collect { emit(it) }
    }.catch { message ->
        emit(AuthResponse.Error(message.localizedMessage))
    }.flowOn(Dispatchers.IO)

    override fun signInWithEmail(
        email: String,
        password: String
    ): Flow<AuthResponse> = flow<AuthResponse> {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        emit(AuthResponse.Success)
    }.catch { message ->
        emit(AuthResponse.Error(message.localizedMessage))
    }.flowOn(Dispatchers.IO)

    override fun loginWithGoogleToken(googleIdToken: String): Flow<AuthResponse> =
        flow<AuthResponse> {
            supabaseClient.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }
            emit(AuthResponse.Success)
        }.catch { message ->
            emit(AuthResponse.Error(message.localizedMessage))
        }.flowOn(Dispatchers.IO)

    override fun resetPassword(email: String): Flow<AuthResponse> = flow<AuthResponse> {
        supabaseClient.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = "app://intermind.com/reset-password"
        )
        emit(AuthResponse.Success)
    }.catch { message ->
        emit(AuthResponse.Error(message.localizedMessage))
    }.flowOn(Dispatchers.IO)

    override fun updatePassword(newPassword: String): Flow<AuthResponse> = flow<AuthResponse> {
        supabaseClient.auth.updateUser {
            password = newPassword
        }
        emit(AuthResponse.Success)
    }.catch { message ->
        emit(AuthResponse.Error(message.localizedMessage))
    }.flowOn(Dispatchers.IO)

    override fun signOut(): Flow<AuthResponse> = flow<AuthResponse> {
        supabaseClient.auth.signOut()
        emit(AuthResponse.Success)
    }.catch { message ->
        emit(AuthResponse.Error(message.localizedMessage))
    }.flowOn(Dispatchers.IO)
}