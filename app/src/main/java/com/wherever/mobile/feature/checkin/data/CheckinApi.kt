package com.wherever.mobile.feature.checkin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class CheckinRequestDto(
    @SerialName("negocio_id") val placeId: String,
    @SerialName("metodo") val method: String,
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lng") val lng: Double? = null,
    @SerialName("codigo") val code: String? = null,
    @SerialName("plan_id") val planId: String? = null
)

@Serializable
data class CheckinDto(
    @SerialName("id") val id: String,
    @SerialName("negocio_id") val placeId: String = "",
    @SerialName("valido") val valid: Boolean = false,
    @SerialName("resena_id") val reviewId: String? = null,
    @SerialName("creado_en") val createdAt: String = ""
)

/** wherever-backend `docs/endpoints.md`: check-in en el lugar (specs/nv-30). */
interface CheckinApi {
    @POST("checkins")
    suspend fun checkIn(@Body body: CheckinRequestDto): CheckinDto
}
