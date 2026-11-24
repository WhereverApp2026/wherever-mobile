package com.wherever.mobile.feature.business.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

@Serializable
data class BizOptionDto(@SerialName("id") val id: String, @SerialName("nombre") val name: String = "")

@Serializable
data class BizCatalogDto(
    @SerialName("categorias") val categories: List<BizOptionDto> = emptyList(),
    @SerialName("servicios") val services: List<BizOptionDto> = emptyList()
)

@Serializable
data class BizAddressDto(
    @SerialName("calle") val street: String = "",
    @SerialName("numero") val number: String = "",
    @SerialName("colonia") val neighborhood: String = "",
    @SerialName("municipio") val city: String = "",
    @SerialName("estado") val state: String = "",
    @SerialName("cp") val zip: String = ""
)

/** GeoJSON. `type` sin valor por omisión: `ApiJson` no manda los campos que quedan en su valor por omisión. */
@Serializable
data class BizPointDto(@SerialName("type") val type: String, @SerialName("coordinates") val coordinates: List<Double>)

@Serializable
data class BizScheduleDto(
    @SerialName("dia") val day: Int,
    @SerialName("abre") val opens: String = "",
    @SerialName("cierra") val closes: String = "",
    @SerialName("cerrado") val closed: Boolean = false
)

@Serializable
data class BizPhotoDto(@SerialName("id") val id: String = "", @SerialName("url") val url: String = "")

@Serializable
data class BizDocumentsDto(
    @SerialName("ine") val ine: Boolean = false,
    @SerialName("comprobante") val proof: Boolean = false
)

/** Negocio del alta. La API agrega campos con el tiempo; solo se leen los que usa la app. */
@Serializable
data class BusinessDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("categoria") val category: String = "",
    @SerialName("descripcion") val description: String? = null,
    @SerialName("estado") val state: String = "",
    @SerialName("paso_alta") val step: Int = 1,
    @SerialName("version") val version: Long = 0,
    @SerialName("fotos") val photos: List<BizPhotoDto>? = null,
    @SerialName("direccion") val address: BizAddressDto? = null,
    @SerialName("horarios") val schedules: List<BizScheduleDto>? = null,
    @SerialName("nivel_precio") val priceLevel: Int? = null,
    @SerialName("precio_promedio") val averagePrice: Int? = null,
    @SerialName("servicios") val services: List<String>? = null,
    @SerialName("documentos") val documents: BizDocumentsDto? = null,
    @SerialName("requiere_refresh") val requiresRefresh: Boolean = false
)

@Serializable
data class BusinessListDto(@SerialName("items") val items: List<BusinessDto> = emptyList())

@Serializable
data class BusinessCreateDto(
    @SerialName("nombre") val name: String,
    @SerialName("categoria") val category: String,
    @SerialName("descripcion") val description: String
)

@Serializable
data class LocationStepDto(
    @SerialName("direccion") val address: BizAddressDto,
    @SerialName("ubicacion") val point: BizPointDto
)

@Serializable
data class ScheduleStepDto(@SerialName("horarios") val schedules: List<BizScheduleDto>)

@Serializable
data class OperationStepDto(
    @SerialName("nivel_precio") val priceLevel: Int,
    @SerialName("precio_promedio") val averagePrice: Int,
    @SerialName("servicios") val services: List<String>
)

@Serializable
data class BizPhotoCreateDto(@SerialName("imagen_id") val imageId: String, @SerialName("tipo") val type: String)

/** Versiones vigentes de `GET /configuracion`: el consentimiento debe citarlas. */
@Serializable
data class LegalVersionsDto(
    @SerialName("version_terminos") val terms: String = "",
    @SerialName("version_aviso_privacidad") val privacyNotice: String = ""
)

/** `POST /usuarios/yo/consentimientos` con `datos_negocio` (nv-19): la API lo exige antes de documentos y CLABE. */
@Serializable
data class BusinessConsentDto(
    @SerialName("version_terminos") val terms: String,
    @SerialName("version_aviso_privacidad") val privacyNotice: String,
    @SerialName("datos_negocio") val businessData: Boolean = true
)

@Serializable
data class DepositAccountDto(@SerialName("clabe") val clabe: String, @SerialName("titular") val holder: String)

@Serializable
data class DescriptionWordsDto(@SerialName("palabras") val words: List<String>)

@Serializable
data class DescriptionDto(@SerialName("descripcion") val description: String = "")

/** wherever-backend `docs/endpoints.md`: alta del negocio (specs/nv-34). */
interface BusinessOnboardingApi {
    @GET("categorias")
    suspend fun catalog(): BizCatalogDto

    @GET("negocios/mios")
    suspend fun mine(): BusinessListDto

    @POST("negocios")
    suspend fun create(@Body body: BusinessCreateDto): BusinessDto

    /** Los pasos sin cuerpo (4 y 6) mandan `{}`. */
    @PATCH("negocios/{id}/alta/{paso}")
    suspend fun step(
        @Path("id") id: String,
        @Path("paso") step: Int,
        @Header("If-Match") ifMatch: String,
        @Body body: JsonObject
    ): BusinessDto

    @POST("negocios/{id}/fotos")
    suspend fun addPhoto(@Path("id") id: String, @Body body: BizPhotoCreateDto): BusinessDto

    @Multipart
    @POST("negocios/{id}/documentos")
    suspend fun uploadDocument(@Path("id") id: String, @Part("tipo") type: RequestBody, @Part file: MultipartBody.Part)

    @PUT("negocios/{id}/cuenta-deposito")
    suspend fun depositAccount(@Path("id") id: String, @Body body: DepositAccountDto)

    @GET("configuracion")
    suspend fun legalVersions(): LegalVersionsDto

    @POST("usuarios/yo/consentimientos")
    suspend fun consent(@Body body: BusinessConsentDto)

    @POST("negocios/{id}/enviar-revision")
    suspend fun submit(@Path("id") id: String): BusinessDto

    @POST("negocios/{id}/ia/descripcion")
    suspend fun describe(@Path("id") id: String, @Body body: DescriptionWordsDto): DescriptionDto
}
