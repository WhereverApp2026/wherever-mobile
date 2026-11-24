package com.wherever.mobile.feature.groups.domain

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint

/** Punto de encuentro del grupo (backend nv-69). `at` es ISO 8601; `placeId` si es un negocio. */
data class MeetingPoint(val name: String, val lat: Double, val lng: Double, val at: String, val placeId: String?)

/** Lugar sugerido como punto medio: abierto a la hora del punto, cerca del promedio de las ubicaciones. */
data class MeetingPlace(
    val id: String,
    val name: String,
    val category: String,
    val rating: Double,
    val lat: Double,
    val lng: Double
)

/** `participants` = cuántos comparten su ubicación; la API nunca da el centro ni distancias por persona. */
data class MeetingSuggestion(val places: List<MeetingPlace>, val participants: Int)

/** Cuánto tarda cada quien al punto: exacto solo en la fila propia; rangos para los demás. */
data class MemberTrip(
    val userId: String,
    val name: String,
    val photoUrl: String?,
    val mine: Boolean,
    val minutes: Int?,
    val cost: Int?,
    val range: TripRange?,
    val noLocation: Boolean
)

enum class TripRange(val apiValue: String) {
    UP_TO_15("hasta_15"),
    FROM_15_TO_30("15_30"),
    FROM_30_TO_60("30_60"),
    OVER_60("mas_60");

    companion object {
        fun from(value: String?): TripRange? = entries.firstOrNull { it.apiValue == value }
    }
}

/** 47_PuntoMedio (backend nv-69). La ubicación compartida dura 6 h y nunca sale en ninguna respuesta. */
interface MeetingPointRepository {
    /** Comparte mi ubicación aproximada con el grupo, o deja de compartirla con `null`. */
    suspend fun shareLocation(groupId: String, point: GeoPoint?): Result<Group>

    /** Con menos de 2 ubicaciones la API responde 422 `faltan_ubicaciones`. */
    suspend fun suggest(groupId: String): Result<MeetingSuggestion>

    /** El último en fijarlo gana; `at` del día del plan y no en el pasado. */
    suspend fun setPoint(groupId: String, placeId: String, at: String): Result<Group>

    /** Sin punto la API responde 404 `sin_punto`. */
    suspend fun trips(groupId: String): Result<List<MemberTrip>>
}
