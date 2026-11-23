package com.wherever.mobile.feature.plans.data

import com.wherever.mobile.feature.places.data.PlaceCardDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Contratos de wherever-backend docs/endpoints.md (nv-30 a nv-33, nv-38, nv-68).

@Serializable
data class StopDto(
    @SerialName("id") val id: String = "",
    @SerialName("negocio_id") val placeId: String,
    @SerialName("llega") val arrives: String,
    @SerialName("minutos") val minutes: Int = 0,
    @SerialName("precio") val price: Int = 0,
    @SerialName("nombre") val name: String? = null,
    @SerialName("categoria") val category: String? = null,
    @SerialName("por_que") val why: String? = null
)

@Serializable
data class TransferDto(
    @SerialName("desde") val from: String = "",
    @SerialName("hasta") val to: String = "",
    @SerialName("minutos") val minutes: Int = 0,
    @SerialName("km") val km: Double = 0.0
)

@Serializable
data class PlanDto(
    @SerialName("id") val id: String = "",
    @SerialName("nombre") val name: String = "",
    @SerialName("fecha") val date: String = "",
    @SerialName("personas") val people: Int = 1,
    @SerialName("presupuesto") val budget: Int = 0,
    @SerialName("movilidad") val mobility: String? = null,
    @SerialName("paradas") val stops: List<StopDto> = emptyList(),
    @SerialName("traslados") val transfers: List<TransferDto> = emptyList(),
    @SerialName("costo_total") val totalCost: Int = 0,
    @SerialName("costo_por_persona") val costPerPerson: Int = 0,
    @SerialName("origen") val origin: String? = null,
    @SerialName("visibilidad") val visibility: String? = null,
    @SerialName("version") val version: Int = 0
)

@Serializable
data class ParametersDto(
    @SerialName("compania") val company: String? = null,
    @SerialName("con_ninos") val withKids: Boolean = false,
    @SerialName("con_mascota") val withPet: Boolean = false,
    @SerialName("presupuesto_max") val maxBudget: Int = 0,
    @SerialName("hora") val time: String? = null,
    @SerialName("palabras_clave") val keywords: List<String> = emptyList()
)

@Serializable
data class GenerateResponseDto(
    @SerialName("fuente") val source: String = "",
    @SerialName("aviso") val notice: String? = null,
    @SerialName("parametros") val parameters: ParametersDto? = null,
    @SerialName("plan") val plan: PlanDto
)

@Serializable
data class GenerateRequestDto(
    @SerialName("frase") val phrase: String,
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double,
    @SerialName("fecha") val dateTime: String? = null,
    @SerialName("personas") val people: Int? = null,
    @SerialName("movilidad") val mobility: String? = null,
    @SerialName("excluir") val exclude: List<String>? = null
)

@Serializable
data class CreateStopDto(
    @SerialName("negocio_id") val placeId: String,
    @SerialName("llega") val arrives: String,
    @SerialName("minutos") val minutes: Int,
    @SerialName("precio") val price: Int
)

@Serializable
data class CreatePlanDto(
    @SerialName("nombre") val name: String,
    @SerialName("fecha") val date: String,
    @SerialName("personas") val people: Int,
    @SerialName("presupuesto") val budget: Int,
    @SerialName("movilidad") val mobility: String,
    @SerialName("origen") val origin: String? = null,
    @SerialName("paradas") val stops: List<CreateStopDto>
)

@Serializable
data class PlanPatchDto(
    @SerialName("nombre") val name: String? = null,
    @SerialName("visibilidad") val visibility: String? = null
)

@Serializable
data class StopPatchDto(@SerialName("llega") val arrives: String)

@Serializable
data class OrderDto(@SerialName("orden") val order: List<String>)

@Serializable
data class CloneDto(@SerialName("fecha") val date: String)

@Serializable
data class ReplaceStopDto(@SerialName("negocio_id") val placeId: String)

@Serializable
data class PlanListDto(@SerialName("items") val items: List<PlanDto> = emptyList())

@Serializable
data class PopularStopDto(@SerialName("lugar") val place: PlaceCardDto? = null)

@Serializable
data class PopularPlanDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("personas") val people: Int = 1,
    @SerialName("costo_por_persona") val costPerPerson: Int = 0,
    @SerialName("veces_clonado") val copies: Int = 0,
    @SerialName("paradas") val stops: List<PopularStopDto> = emptyList()
)

@Serializable
data class PopularPlansDto(@SerialName("items") val items: List<PopularPlanDto> = emptyList())

@Serializable
data class AlternativeDto(
    @SerialName("lugar") val place: PlaceCardDto,
    @SerialName("distancia_m") val distanceMeters: Double = 0.0,
    @SerialName("minutos_extra") val extraMinutes: Int = 0
)

@Serializable
data class AlternativesDto(@SerialName("items") val items: List<AlternativeDto> = emptyList())

@Serializable
data class ShareDto(@SerialName("enlace") val link: String = "", @SerialName("vence_en") val expiresAt: String = "")

@Suppress("TooManyFunctions")
interface PlansApi {
    @POST("planes/generar")
    suspend fun generate(@Body body: GenerateRequestDto): GenerateResponseDto

    @POST("planes/generar/otra-idea")
    suspend fun otherIdea(@Body body: GenerateRequestDto): GenerateResponseDto

    @GET("planes/populares")
    suspend fun popular(@Query("lat") lat: Double, @Query("lng") lng: Double): PopularPlansDto

    @POST("planes")
    suspend fun create(@Body body: CreatePlanDto): PlanDto

    @GET("planes")
    suspend fun list(@Query("pestana") tab: String, @Query("limite") limit: Int): PlanListDto

    @GET("planes/{id}")
    suspend fun get(@Path("id") id: String): PlanDto

    @PATCH("planes/{id}")
    suspend fun patch(@Path("id") id: String, @Header("If-Match") ifMatch: String, @Body body: PlanPatchDto): PlanDto

    @DELETE("planes/{id}")
    suspend fun delete(@Path("id") id: String, @Header("If-Match") ifMatch: String)

    @POST("planes/{id}/clonar")
    suspend fun clone(@Path("id") id: String, @Body body: CloneDto): PlanDto

    @POST("planes/{id}/paradas")
    suspend fun addStop(@Path("id") id: String, @Header("If-Match") ifMatch: String, @Body body: CreateStopDto): PlanDto

    @PUT("planes/{id}/paradas/orden")
    suspend fun reorder(@Path("id") id: String, @Header("If-Match") ifMatch: String, @Body body: OrderDto): PlanDto

    @PATCH("planes/{id}/paradas/{stop}")
    suspend fun patchStop(
        @Path("id") id: String,
        @Path("stop") stopId: String,
        @Header("If-Match") ifMatch: String,
        @Body body: StopPatchDto
    ): PlanDto

    @DELETE("planes/{id}/paradas/{stop}")
    suspend fun removeStop(
        @Path("id") id: String,
        @Path("stop") stopId: String,
        @Header("If-Match") ifMatch: String
    ): PlanDto

    @GET("planes/{id}/paradas/{stop}/alternativas")
    suspend fun alternatives(
        @Path("id") id: String,
        @Path("stop") stopId: String,
        @Query("q") query: String? = null
    ): AlternativesDto

    @POST("planes/{id}/paradas/{stop}/cambiar")
    suspend fun replaceStop(
        @Path("id") id: String,
        @Path("stop") stopId: String,
        @Header("If-Match") ifMatch: String,
        @Body body: ReplaceStopDto
    ): PlanDto

    @POST("planes/{id}/compartir")
    suspend fun share(@Path("id") id: String): ShareDto
}
