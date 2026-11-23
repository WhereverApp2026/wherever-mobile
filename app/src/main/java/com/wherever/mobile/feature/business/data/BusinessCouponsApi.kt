package com.wherever.mobile.feature.business.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class CouponDto(
    @SerialName("id") val id: String,
    @SerialName("titulo") val title: String = "",
    @SerialName("tipo") val type: String = "",
    @SerialName("valor") val value: Long? = null,
    @SerialName("condiciones") val conditions: String? = null,
    @SerialName("vigencia_fin") val validUntil: String? = null,
    @SerialName("estado") val status: String = "",
    @SerialName("limite_usos") val usageLimit: Int = 0,
    @SerialName("usos_actuales") val uses: Int = 0,
    @SerialName("version") val version: Long = 0
)

@Serializable
data class CouponPageDto(@SerialName("items") val items: List<CouponDto> = emptyList())

@Serializable
data class CouponHoursDto(@SerialName("desde") val from: String, @SerialName("hasta") val to: String)

@Serializable
data class NewCouponDto(
    @SerialName("titulo") val title: String,
    @SerialName("tipo") val type: String,
    @SerialName("valor") val value: Long? = null,
    @SerialName("condiciones") val conditions: String? = null,
    @SerialName("vigencia_inicio") val start: String,
    @SerialName("vigencia_fin") val end: String,
    @SerialName("dias_validos") val days: List<Int>? = null,
    @SerialName("horas_validas") val hours: CouponHoursDto? = null,
    @SerialName("limite_usos") val usageLimit: Int
)

@Serializable
data class CouponStatusDto(@SerialName("estado") val status: String)

@Serializable
data class TitleRequestDto(
    @SerialName("tipo") val type: String,
    @SerialName("valor") val value: Long? = null,
    @SerialName("descripcion") val description: String? = null
)

@Serializable
data class TitleDto(@SerialName("titulo") val title: String = "")

/** Negocio de `GET /negocios/mios` con su configuración de reservas (se lee como objeto para no perder campos). */
@Serializable
data class BusinessConfigDto(
    @SerialName("id") val id: String,
    @SerialName("reservas_config") val reservationsConfig: JsonObject? = null,
    @SerialName("version") val version: Long = 0
)

@Serializable
data class BusinessConfigListDto(@SerialName("items") val items: List<BusinessConfigDto> = emptyList())

@Serializable
data class SavedConfigDto(
    @SerialName("reservas_config") val reservationsConfig: JsonObject? = null,
    @SerialName("version") val version: Long = 0
)

@Serializable
data class SlotDto(
    @SerialName("hora") val hour: String,
    @SerialName("disponible") val available: Boolean = false,
    @SerialName("lugares_libres") val free: Int = 0
)

@Serializable
data class AvailabilityDto(@SerialName("horarios") val slots: List<SlotDto> = emptyList())

/** wherever-backend `docs/endpoints.md`: cupones y cupo del negocio (specs/nv-37). */
interface BusinessCouponsApi {
    @GET("negocios/{id}/cupones")
    suspend fun coupons(@Path("id") id: String, @Query("limite") limit: Int): CouponPageDto

    @POST("negocios/{id}/cupones")
    suspend fun create(@Path("id") id: String, @Body body: NewCouponDto): CouponDto

    @PATCH("negocios/{id}/cupones/{cid}")
    suspend fun setStatus(
        @Path("id") id: String,
        @Path("cid") couponId: String,
        @Header("If-Match") ifMatch: String,
        @Body body: CouponStatusDto
    ): CouponDto

    @POST("negocios/{id}/cupones/sugerir-titulo")
    suspend fun suggestTitle(@Path("id") id: String, @Body body: TitleRequestDto): TitleDto

    @GET("negocios/mios")
    suspend fun mine(): BusinessConfigListDto

    @PUT("negocios/{id}/reservas-config")
    suspend fun saveConfig(
        @Path("id") id: String,
        @Header("If-Match") ifMatch: String,
        @Body body: JsonObject
    ): SavedConfigDto

    @GET("lugares/{id}/disponibilidad")
    suspend fun availability(
        @Path("id") id: String,
        @Query("fecha") date: String,
        @Query("personas") people: Int
    ): AvailabilityDto
}
