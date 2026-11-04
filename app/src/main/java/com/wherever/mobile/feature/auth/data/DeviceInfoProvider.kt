package com.wherever.mobile.feature.auth.data

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import com.wherever.mobile.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface DeviceInfoProvider {
    val device: DeviceDto
}

@Singleton
class AndroidDeviceInfoProvider @Inject constructor(@ApplicationContext context: Context) : DeviceInfoProvider {

    private val deviceId: String = context
        .getSharedPreferences(DEVICE_PREFERENCES, Context.MODE_PRIVATE)
        .let { preferences ->
            preferences.getString(DEVICE_ID_KEY, null)
                ?: UUID.randomUUID().toString().also { generated ->
                    preferences.edit { putString(DEVICE_ID_KEY, generated) }
                }
        }

    override val device: DeviceDto
        get() = DeviceDto(
            id = deviceId,
            name = listOf(Build.MANUFACTURER, Build.MODEL)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { "Android" },
            platform = "android",
            appVersion = BuildConfig.VERSION_NAME
        )

    private companion object {
        const val DEVICE_PREFERENCES = "device_info"
        const val DEVICE_ID_KEY = "device_id"
    }
}
