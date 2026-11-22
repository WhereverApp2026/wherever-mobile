package com.wherever.mobile.feature.business.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

@Serializable
data class ModeDto(@SerialName("modo") val mode: String)

@Serializable
data class NextReservationDto(@SerialName("hora") val time: String = "", @SerialName("personas") val people: Int = 0)

@Serializable
data class SealCriterionDto(
    @SerialName("nombre") val name: String = "",
    @SerialName("actual") val current: Double? = null,
    @SerialName("meta") val goal: Double = 0.0,
    @SerialName("cumple") val met: Boolean = false
)

@Serializable
data class SealProgressDto(
    @SerialName("elegible") val eligible: Boolean = false,
    @SerialName("criterios") val criteria: List<SealCriterionDto> = emptyList()
)

/** `GET /negocios/{id}/panel`: solo los campos que muestra 48_NegPanel. Nunca trae el expediente. */
@Serializable
data class PanelDto(
    @SerialName("negocio_id") val businessId: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("estado") val state: String = "",
    @SerialName("reservas_hoy") val reservationsToday: Int? = null,
    @SerialName("proxima_reserva") val nextReservation: NextReservationDto? = null,
    @SerialName("cupones_canjeados_hoy") val couponsRedeemedToday: Int? = null,
    @SerialName("cupones_activos") val activeCoupons: Int? = null,
    @SerialName("llegadas_hoy") val arrivalsToday: Int? = null,
    @SerialName("calificacion_semana") val weekRating: Double? = null,
    @SerialName("cambio_vs_semana_anterior") val ratingChange: Double? = null,
    @SerialName("resenas_por_responder") val reviewsToAnswer: Int? = null,
    @SerialName("sello_excelencia") val seal: Boolean = false,
    @SerialName("avance_sello") val sealProgress: SealProgressDto? = null
)

@Serializable
data class TipDto(@SerialName("texto") val text: String = "", @SerialName("accion") val action: String = "ninguna")

@Serializable
data class StatsPointDto(
    @SerialName("inicio") val start: String,
    @SerialName("vistas_ficha") val views: Int = 0,
    @SerialName("reservas") val reservations: Int = 0,
    @SerialName("llegadas") val arrivals: Int = 0,
    @SerialName("cupones_canjeados") val coupons: Int = 0,
    @SerialName("favoritos_nuevos") val favorites: Int = 0
)

@Serializable
data class StatsTotalsDto(
    @SerialName("vistas_ficha") val views: Int = 0,
    @SerialName("reservas") val reservations: Int = 0,
    @SerialName("llegadas") val arrivals: Int = 0,
    @SerialName("cupones_canjeados") val coupons: Int = 0,
    @SerialName("favoritos_nuevos") val favorites: Int = 0
)

@Serializable
data class StatsChangeDto(@SerialName("reservas") val reservations: Double? = null)

@Serializable
data class StatsOriginDto(
    @SerialName("busqueda") val search: Int = 0,
    @SerialName("mapa") val map: Int = 0,
    @SerialName("inicio") val home: Int = 0,
    @SerialName("plan_ia") val aiPlan: Int = 0,
    @SerialName("cupon") val coupon: Int = 0
)

@Serializable
data class StatsAdvancedDto(@SerialName("origen") val origin: StatsOriginDto? = null)

@Serializable
data class StatsDto(
    @SerialName("desde") val from: String,
    @SerialName("hasta") val to: String,
    @SerialName("serie") val series: List<StatsPointDto> = emptyList(),
    @SerialName("totales") val totals: StatsTotalsDto = StatsTotalsDto(),
    @SerialName("cambio") val change: StatsChangeDto = StatsChangeDto(),
    @SerialName("frase") val phrase: String = "",
    @SerialName("avanzadas") val advanced: StatsAdvancedDto? = null,
    @SerialName("premium_requerido") val premiumRequired: Boolean = false
)

@Serializable
data class ReservationDto(
    @SerialName("id") val id: String,
    @SerialName("codigo") val code: String = "",
    @SerialName("fecha_hora") val dateTime: String,
    @SerialName("cliente") val client: String = "",
    @SerialName("personas") val people: Int = 0,
    @SerialName("nota") val note: String? = null,
    @SerialName("anticipo") val deposit: Long = 0,
    @SerialName("estado") val status: String = "",
    @SerialName("version") val version: Long = 0
)

@Serializable
data class ReservationDayDto(
    @SerialName("fecha") val date: String,
    @SerialName("items") val items: List<ReservationDto> = emptyList()
)

@Serializable
data class ReservationStatusDto(@SerialName("estado") val status: String)

@Serializable
data class ContactDto(@SerialName("texto") val text: String)

@Serializable
data class CheckInDto(@SerialName("codigo") val code: String)

@Serializable
data class ChargeDto(
    @SerialName("anticipo") val deposit: Long = 0,
    @SerialName("reembolsos") val refunds: Long = 0,
    @SerialName("comision") val commission: Long = 0,
    @SerialName("neto") val net: Long = 0,
    @SerialName("estado") val status: String = ""
)

@Serializable
data class BlockRequestDto(
    @SerialName("desde") val from: String,
    @SerialName("hasta") val to: String,
    @SerialName("motivo") val reason: String? = null
)

@Serializable
data class BlockDto(
    @SerialName("id") val id: String,
    @SerialName("desde") val from: String,
    @SerialName("hasta") val to: String,
    @SerialName("motivo") val reason: String? = null
)

/** wherever-backend `docs/endpoints.md`: panel, estadísticas, reservas y check-in del negocio (specs/nv-35). */
interface BusinessHomeApi {
    @POST("usuarios/yo/modo")
    suspend fun mode(@Body body: ModeDto)

    @GET("negocios/{id}/panel")
    suspend fun panel(@Path("id") id: String): PanelDto

    @GET("negocios/{id}/consejo")
    suspend fun tip(@Path("id") id: String): TipDto

    @GET("negocios/{id}/estadisticas")
    suspend fun stats(
        @Path("id") id: String,
        @Query("desde") from: String,
        @Query("hasta") to: String,
        @Query("agrupar") groupBy: String
    ): StatsDto

    @Streaming
    @GET("negocios/{id}/reportes/mensual")
    suspend fun monthlyReport(@Path("id") id: String, @Query("mes") month: String): ResponseBody

    @GET("negocios/{id}/reservas")
    suspend fun reservations(
        @Path("id") id: String,
        @Query("fecha") date: String?,
        @Query("estado") status: String?,
        @Query("q") query: String?
    ): ReservationDayDto

    @PATCH("negocios/{id}/reservas/{rid}")
    suspend fun updateReservation(
        @Path("id") id: String,
        @Path("rid") reservationId: String,
        @Body body: ReservationStatusDto
    ): ReservationDto

    @POST("negocios/{id}/reservas/{rid}/mensajes")
    suspend fun contact(@Path("id") id: String, @Path("rid") reservationId: String, @Body body: ContactDto)

    @POST("negocios/{id}/reservas/escanear")
    suspend fun checkIn(@Path("id") id: String, @Body body: CheckInDto): ReservationDto

    @GET("negocios/{id}/reservas/{rid}/cobro")
    suspend fun charge(@Path("id") id: String, @Path("rid") reservationId: String): ChargeDto

    @POST("negocios/{id}/bloqueos")
    suspend fun block(@Path("id") id: String, @Body body: BlockRequestDto): BlockDto

    @DELETE("negocios/{id}/bloqueos/{bid}")
    suspend fun unblock(@Path("id") id: String, @Path("bid") blockId: String)
}
