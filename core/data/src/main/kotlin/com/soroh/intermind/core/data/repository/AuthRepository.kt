package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.model.AuthResponse
import kotlinx.coroutines.flow.Flow

/**
 * Data layer interface for the auth feature.
 */
interface AuthRepository {

    fun signUpWithEmail(name: String, email: String, password: String): Flow<AuthResponse>

    fun signInWithEmail(email: String, password: String): Flow<AuthResponse>

    fun loginWithGoogleToken(googleIdToken: String): Flow<AuthResponse>

    fun resetPassword(email: String): Flow<AuthResponse>

    fun updatePassword(newPassword: String): Flow<AuthResponse>

     fun signOut(): Flow<AuthResponse>
}