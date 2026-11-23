package com.wherever.mobile.feature.business.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class PaymentsStateDto(@SerialName("estado") val state: String? = null)

/** Perfil del negocio de `GET /negocios/mios` y de `PATCH /negocios/{id}`: solo lo que usa 58_NegPerfil. */
@Serializable
data class BusinessProfileDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("categoria") val category: String = "",
    @SerialName("descripcion") val description: String? = null,
    @SerialName("telefono") val phone: String? = null,
    @SerialName("direccion") val address: BizAddressDto? = null,
    @SerialName("estado") val state: String = "",
    /** Objeto `{desde, hasta, motivo}` o booleano según la versión de la API: solo importa si viene. */
    @SerialName("pausado") val paused: JsonElement? = null,
    @SerialName("horarios") val schedules: List<BizScheduleDto>? = null,
    @SerialName("fotos") val photos: List<BizPhotoDto>? = null,
    @SerialName("reservas_config") val reservationsConfig: JsonObject? = null,
    @SerialName("cobros") val payments: PaymentsStateDto? = null,
    @SerialName("version") val version: Long = 0
)

@Serializable
data class BusinessProfileListDto(@SerialName("items") val items: List<BusinessProfileDto> = emptyList())

@Serializable
data class DepositAccountInfoDto(
    @SerialName("clabe_ultimos4") val lastFour: String = "",
    @SerialName("banco") val bank: String? = null,
    @SerialName("titular") val holder: String? = null,
    @SerialName("estado") val state: String? = null
)

/** Lo que 58 y 57 leen del panel; nunca trae el expediente. */
@Serializable
data class PanelAccountsDto(
    @SerialName("datos_fiscales_completos") val taxDataComplete: Boolean? = null,
    @SerialName("cuenta_deposito") val depositAccount: DepositAccountInfoDto? = null
)

@Serializable
data class BusinessDataPatchDto(
    @SerialName("descripcion") val description: String,
    @SerialName("telefono") val phone: String?,
    @SerialName("horarios") val schedules: List<BizScheduleDto>
)

@Serializable
data class PauseDto(
    @SerialName("pausado") val paused: Boolean,
    @SerialName("hasta") val until: String? = null,
    @SerialName("motivo") val reason: String? = null
)

@Serializable
data class DeleteBusinessDto(@SerialName("confirmacion") val confirmation: String)

@Serializable
data class PhotoOrderDto(@SerialName("fotos_ids") val ids: List<String>)

@Serializable
data class BusinessPhotosDto(@SerialName("fotos") val photos: List<BizPhotoDto>? = null)

@Serializable
data class ConnectUrlDto(@SerialName("url") val url: String = "")

@Serializable
data class BalanceDto(
    @SerialName("disponible") val available: Long = 0,
    @SerialName("en_espera") val pending: Long = 0,
    @SerialName("proximo_deposito") val nextDeposit: String? = null
)

@Serializable
data class SettlementDto(
    @SerialName("id") val id: String,
    @SerialName("semana_inicio") val weekStart: String,
    @SerialName("neto") val net: Long = 0,
    @SerialName("estado") val state: String = ""
)

@Serializable
data class SettlementPageDto(
    @SerialName("items") val items: List<SettlementDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class TaxDataDto(
    @SerialName("rfc") val rfc: String,
    @SerialName("razon_social") val legalName: String,
    @SerialName("regimen_fiscal") val regime: String,
    @SerialName("uso_cfdi") val cfdiUse: String,
    @SerialName("cp_fiscal") val postalCode: String
)

/** Perfil, fotos y cobros del negocio (specs/nv-38). */
@Suppress("TooManyFunctions") // Un método por endpoint de la spec.
interface BusinessProfileApi {
    @GET("negocios/mios")
    suspend fun mine(): BusinessProfileListDto

    @GET("negocios/{id}/panel")
    suspend fun panelAccounts(@Path("id") id: String): PanelAccountsDto

    @PATCH("negocios/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Header("If-Match") ifMatch: String,
        @Body body: BusinessDataPatchDto
    ): BusinessProfileDto

    @PUT("negocios/{id}/reservas-config")
    suspend fun saveConfig(
        @Path("id") id: String,
        @Header("If-Match") ifMatch: String,
        @Body body: JsonObject
    ): BusinessProfileDto

    @POST("negocios/{id}/pausa")
    suspend fun pause(@Path("id") id: String, @Body body: PauseDto): BusinessProfileDto

    /** `DELETE` con cuerpo: Retrofit lo pide con `@HTTP`. */
    @HTTP(method = "DELETE", path = "negocios/{id}", hasBody = true)
    suspend fun delete(@Path("id") id: String, @Body body: DeleteBusinessDto)

    @POST("negocios/{id}/fotos")
    suspend fun addPhoto(@Path("id") id: String, @Body body: BizPhotoCreateDto): BusinessPhotosDto

    @PUT("negocios/{id}/fotos/orden")
    suspend fun reorderPhotos(@Path("id") id: String, @Body body: PhotoOrderDto): BusinessPhotosDto

    @DELETE("negocios/{id}/fotos/{photoId}")
    suspend fun deletePhoto(@Path("id") id: String, @Path("photoId") photoId: String)

    @POST("negocios/{id}/cobros/conectar")
    suspend fun connectPayments(@Path("id") id: String): ConnectUrlDto

    @GET("negocios/{id}/saldo")
    suspend fun balance(@Path("id") id: String): BalanceDto

    @GET("negocios/{id}/liquidaciones")
    suspend fun settlements(
        @Path("id") id: String,
        @Query("cursor") cursor: String?,
        @Query("limite") limit: Int
    ): SettlementPageDto

    @PUT("negocios/{id}/cuenta-deposito")
    suspend fun depositAccount(@Path("id") id: String, @Body body: DepositAccountDto): DepositAccountInfoDto

    @PUT("negocios/{id}/datos-fiscales")
    suspend fun taxData(@Path("id") id: String, @Body body: TaxDataDto)

    @POST("usuarios/yo/modo")
    suspend fun mode(@Body body: ModeDto)
}
