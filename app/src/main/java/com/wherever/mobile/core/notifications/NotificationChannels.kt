package com.wherever.mobile.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.wherever.mobile.R

/** Un solo canal por ahora; el id coincide con `default_notification_channel_id` del manifiesto. */
object NotificationChannels {
    const val GENERAL = "general"

    fun create(context: Context) {
        val channel = NotificationChannel(
            GENERAL,
            context.getString(R.string.notifications_channel_general),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.notifications_channel_general_description) }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
