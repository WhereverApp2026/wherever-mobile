package com.wherever.mobile.core.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wherever.mobile.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Recibe los push de FCM (specs/nv-46).
 * - Con la app cerrada o en segundo plano, Android pinta el push solo y al tocarlo abre la actividad
 *   launcher con `data` como extras.
 * - Con la app visible se muestra dentro de la app (`InAppPushes`, 88_PushBanner).
 */
@AndroidEntryPoint
class WhereverMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var registration: DeviceRegistration

    @Inject
    lateinit var inAppPushes: InAppPushes

    override fun onNewToken(token: String) {
        registration.registerInBackground(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = PushDestination.KEYS.associateWith { message.data[it] }
        val shownInApp = inAppPushes.offer(InAppPush(message.notification?.title, message.notification?.body, data))
        if (!shownInApp &&
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        ) {
            showSystemNotification(message)
        }
    }

    private fun showSystemNotification(message: RemoteMessage) {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val launch = packageManager.getLaunchIntentForPackage(packageName) ?: return
        launch.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        PushDestination.KEYS.forEach { key -> message.data[key]?.let { launch.putExtra(key, it) } }
        val requestCode = message.messageId?.hashCode() ?: System.currentTimeMillis().toInt()
        val pending = PendingIntent.getActivity(
            this,
            requestCode,
            launch,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, NotificationChannels.GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(message.notification?.title ?: getString(R.string.notifications_default_title))
            .setContentText(message.notification?.body ?: getString(R.string.notifications_default_body))
            .setAutoCancel(true)
            // 87_PushBloqueo: se ve en la pantalla de bloqueo sin el texto si el teléfono lo oculta.
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(
                NotificationCompat.Builder(this, NotificationChannels.GENERAL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.notifications_default_title))
                    .build()
            )
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setContentIntent(pending)
            .build()
        manager.notify(requestCode, notification)
    }
}
