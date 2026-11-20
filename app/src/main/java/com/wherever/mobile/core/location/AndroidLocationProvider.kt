package com.wherever.mobile.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

private const val FIX_TIMEOUT_MILLIS = 5_000L

/**
 * Solo `ACCESS_COARSE_LOCATION`: la app no necesita la ubicación exacta (US-004).
 * Usa `LocationManager` del sistema para no sumar Play Services al stack (constitución §3).
 */
class AndroidLocationProvider @Inject constructor(@ApplicationContext private val context: Context) :
    LocationProvider {
    private val manager = context.getSystemService(LocationManager::class.java)

    override fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // El permiso se comprueba en la primera línea; lint no lo sigue entre funciones.
    @SuppressLint("MissingPermission")
    override suspend fun current(): GeoPoint? {
        if (!hasPermission() || manager == null) return null
        val last = manager.getProviders(true)
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
        val location = last ?: freshLocation()
        return location?.let { GeoPoint(it.latitude, it.longitude) }
    }

    @SuppressLint("MissingPermission")
    private suspend fun freshLocation(): Location? {
        val lm = manager ?: return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val provider = LocationManager.NETWORK_PROVIDER.takeIf { lm.isProviderEnabled(it) } ?: return null
        val executor = Executor { it.run() }
        return withTimeoutOrNull(FIX_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                val signal = android.os.CancellationSignal()
                continuation.invokeOnCancellation { signal.cancel() }
                runCatching {
                    lm.getCurrentLocation(provider, signal, executor) { continuation.resume(it) }
                }.onFailure { continuation.resume(null) }
            }
        }
    }
}

class SharedLocationPermissionPreferences @Inject constructor(@ApplicationContext context: Context) :
    LocationPermissionPreferences {
    private val prefs = context.getSharedPreferences("permisos", Context.MODE_PRIVATE)

    override val deniedCount: Int get() = prefs.getInt(KEY_DENIED, 0)

    override val dismissed: Boolean get() = prefs.getBoolean(KEY_DISMISSED, false)

    override fun recordDenied() {
        prefs.edit { putInt(KEY_DENIED, deniedCount + 1) }
    }

    override fun dismiss() {
        prefs.edit { putBoolean(KEY_DISMISSED, true) }
    }

    override val notificationsDecided: Boolean get() = prefs.getBoolean(KEY_NOTIFICATIONS, false)

    override fun recordNotificationsDecided() {
        prefs.edit { putBoolean(KEY_NOTIFICATIONS, true) }
    }

    private companion object {
        const val KEY_DENIED = "ubicacion_negada"
        const val KEY_DISMISSED = "ubicacion_ahora_no"
        const val KEY_NOTIFICATIONS = "avisos_decididos"
    }
}
