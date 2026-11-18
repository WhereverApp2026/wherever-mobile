package com.wherever.mobile.core.location

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
}

/** Después de 2 rechazos el sistema ya no muestra el diálogo: solo queda mandar a Ajustes (US-004). */
const val MAX_LOCATION_DENIALS = 2
