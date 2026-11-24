package com.wherever.mobile.feature.groups.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

@Serializable
data class LatLngDto(@SerialName("lat") val lat: Double, @SerialName("lng") val lng: Double)

/** `PATCH /grupos/{id}/integrantes/yo` solo para la ubicación: sin `estado`. */
@Serializable
data class ShareLocationDto(
    @SerialName("ubicacion") val location: LatLngDto? = null,
    @SerialName("compartir_ubicacion") val share: Boolean
)

@Serializable
data class SetMeetingPointDto(@SerialName("negocio_id") val placeId: String, @SerialName("hora") val at: String)

@Serializable
data class MeetingRatingDto(@SerialName("promedio") val average: Double = 0.0)

@Serializable
data class MeetingPlaceDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("categoria") val category: String = "",
    @SerialName("calificacion") val rating: MeetingRatingDto? = null,
    @SerialName("ubicacion") val location: LatLngDto? = null
)

@Serializable
data class SuggestDto(
    @SerialName("lugares") val places: List<MeetingPlaceDto> = emptyList(),
    @SerialName("participantes") val participants: Int = 0
)

@Serializable
data class TripDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("foto_url") val photoUrl: String? = null,
    @SerialName("propio") val mine: Boolean = false,
    @SerialName("minutos") val minutes: Int? = null,
    @SerialName("costo_estimado") val cost: Int? = null,
    @SerialName("rango") val range: String? = null,
    @SerialName("sin_ubicacion") val noLocation: Boolean = false
)

@Serializable
data class TripsDto(@SerialName("traslados") val items: List<TripDto> = emptyList())

/** wherever-backend nv-69: punto de encuentro del grupo (47_PuntoMedio). */
interface MeetingPointApi {
    @PATCH("grupos/{id}/integrantes/yo")
    suspend fun shareLocation(@Path("id") id: String, @Body body: ShareLocationDto): GroupDto

    @GET("grupos/{id}/punto-encuentro/sugerido")
    suspend fun suggestMeeting(@Path("id") id: String): SuggestDto

    @PUT("grupos/{id}/punto-encuentro")
    suspend fun setMeetingPoint(@Path("id") id: String, @Body body: SetMeetingPointDto): GroupDto

    @GET("grupos/{id}/punto-encuentro/traslados")
    suspend fun trips(@Path("id") id: String): TripsDto
}
