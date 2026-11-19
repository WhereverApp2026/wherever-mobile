package com.wherever.mobile.feature.reservations.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class TimeSlotDto(
    @SerialName("hora") val time: String,
    @SerialName("disponible") val available: Boolean = false,
    @SerialName("lugares_libres") val freeSeats: Int = 0
)

@Serializable
data class AvailabilityDto(
    @SerialName("fecha") val date: String = "",
    @SerialName("horarios") val slots: List<TimeSlotDto> = emptyList()
)

@Serializable
data class DepositDto(@SerialName("monto_centavos") val cents: Long = 0, @SerialName("tipo") val type: String = "")

@Serializable
data class PolicyDto(
    @SerialName("tipo") val type: String = "",
    @SerialName("horas_limite") val hoursLimit: Int = 0,
    @SerialName("porcentaje_antes") val percentBefore: Int = 0,
    @SerialName("porcentaje_despues") val percentAfter: Int = 0
)

@Serializable
data class ReservationMessageDto(
    @SerialName("id") val id: String,
    @SerialName("texto") val text: String,
    @SerialName("creado_en") val createdAt: String
)

@Serializable
data class ReservationDto(
    @SerialName("id") val id: String,
    @SerialName("codigo") val code: String = "",
    @SerialName("negocio_id") val placeId: String,
    @SerialName("fecha_hora") val dateTime: String,
    @SerialName("personas") val people: Int,
    @SerialName("nota") val note: String? = null,
    @SerialName("estado") val status: String,
    @SerialName("bloqueo_expira_en") val holdExpiresAt: String? = null,
    @SerialName("anticipo") val deposit: DepositDto = DepositDto(),
    @SerialName("politica_cancelacion") val policy: PolicyDto = PolicyDto(),
    @SerialName("reembolso_centavos") val refundCents: Long = 0,
    @SerialName("puede_cambiar") val canChange: Boolean = false,
    @SerialName("puede_cancelar") val canCancel: Boolean = false,
    @SerialName("version") val version: Int = 0,
    @SerialName("mensajes") val messages: List<ReservationMessageDto> = emptyList()
)

@Serializable
data class ReservationPageDto(
    @SerialName("items") val items: List<ReservationDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class CreateReservationDto(
    @SerialName("negocio_id") val placeId: String,
    @SerialName("fecha_hora") val dateTime: String,
    @SerialName("personas") val people: Int,
    @SerialName("nota") val note: String? = null
)

@Serializable
data class ChangeReservationDto(@SerialName("fecha_hora") val dateTime: String)

@Serializable
data class CancelReservationDto(@SerialName("motivo") val reason: String? = null)

@Serializable
data class RefundDto(
    @SerialName("reembolso_centavos") val cents: Long = 0,
    @SerialName("porcentaje") val percent: Int = 0
)

/** wherever-backend `docs/endpoints.md`, nv-59 (J5). */
interface ReservationsApi {
    @GET("lugares/{id}/disponibilidad")
    suspend fun availability(
        @Path("id") placeId: String,
        @Query("fecha") date: String,
        @Query("personas") people: Int
    ): AvailabilityDto

    @POST("reservas")
    suspend fun create(@Header("Idempotency-Key") key: String, @Body body: CreateReservationDto): ReservationDto

    @GET("reservas")
    suspend fun list(
        @Query("cuando") whenFilter: String,
        @Query("cursor") cursor: String?,
        @Query("limite") limit: Int
    ): ReservationPageDto

    @GET("reservas/{id}")
    suspend fun get(@Path("id") id: String): ReservationDto

    @GET("reservas/{id}/reembolso")
    suspend fun refund(@Path("id") id: String): RefundDto

    @POST("reservas/{id}/cancelar")
    suspend fun cancel(@Path("id") id: String, @Body body: CancelReservationDto): ReservationDto

    @PATCH("reservas/{id}")
    suspend fun change(
        @Path("id") id: String,
        @Header("If-Match") ifMatch: String,
        @Body body: ChangeReservationDto
    ): ReservationDto
}
