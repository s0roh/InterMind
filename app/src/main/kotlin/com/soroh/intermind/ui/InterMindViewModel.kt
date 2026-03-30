package com.soroh.intermind.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.soroh.intermind.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class InterMindViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun syncFcmToken() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                authRepository.saveFcmToken(token)
            } catch (e: Exception) {
                Log.e("FCM", "Failed to get token", e)
            }
        }
    }
}