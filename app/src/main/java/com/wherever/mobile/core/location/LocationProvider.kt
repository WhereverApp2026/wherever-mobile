package com.wherever.mobile.core.location

import javax.inject.Qualifier

/** Coordenadas sin dependencias de Android, para que `domain` las use. */
data class GeoPoint(val lat: Double, val lng: Double)

/** Ubicación aproximada del teléfono (US-004). Sin permiso o sin señal devuelve `null`. */
interface LocationProvider {
    fun hasPermission(): Boolean

    suspend fun current(): GeoPoint?
}

/** Cuántas veces se negó el permiso y si la persona eligió "Ahora no" (US-004, US-009). Sin datos personales. */
interface LocationPermissionPreferences {
    val deniedCount: Int
    val dismissed: Boolean

    fun recordDenied()

    fun dismiss()

    /** Ya se decidió sobre los avisos en `08_Permisos` (Ahora no o rechazo): Inicio no vuelve a pedirlos. */
    val notificationsDecided: Boolean

    fun recordNotificationsDecided()
}

/** Después de 2 rechazos el sistema ya no muestra el diálogo: solo queda mandar a Ajustes (US-004). */
const val MAX_LOCATION_DENIALS = 2

/** Punto que usa Inicio cuando no hay ubicación: la API exige `lat` y `lng` (specs/nv-47). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FallbackPoint
