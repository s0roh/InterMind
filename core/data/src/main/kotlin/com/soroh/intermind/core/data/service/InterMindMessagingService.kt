package com.soroh.intermind.core.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.soroh.intermind.core.data.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class InterMindMessagingService: FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Отправляем новый токен в Supabase
        serviceScope.launch {
            authRepository.saveFcmToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "InterMind"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Пора повторить карточки!"

        sendNotification(title, body, remoteMessage.data)
    }

    private fun sendNotification(title: String, body: String, data: Map<String, String>) {

        val channelId = "TrainingReminders"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // Замени на иконку своего приложения (желательно монохромную с прозрачным фоном)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Напоминания о тренировках", // То, что увидит пользователь в настройках Android
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(notificationIdCounter.getAndIncrement(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "InterMindMessaging"
        private val notificationIdCounter = AtomicInteger(0)
    }
}