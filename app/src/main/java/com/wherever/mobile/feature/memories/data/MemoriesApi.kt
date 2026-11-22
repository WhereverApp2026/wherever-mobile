package com.wherever.mobile.feature.memories.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class PostcardDto(
    @SerialName("id") val id: String,
    @SerialName("imagen_url") val imageUrl: String? = null,
    @SerialName("estilo") val style: String = "",
    @SerialName("texto") val text: String? = null,
    @SerialName("visibilidad") val visibility: String = "",
    @SerialName("estado") val state: String = "",
    @SerialName("plan_id") val planId: String? = null,
    @SerialName("negocio_id") val placeId: String? = null,
    @SerialName("version") val version: Long = 0,
    @SerialName("creado_en") val createdAt: String = ""
)

@Serializable
data class PostcardPageDto(
    @SerialName("items") val items: List<PostcardDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

/** Cuerpo de `POST /postales`: sin ubicación ni ids ajenos. */
@Serializable
data class PostcardCreateDto(
    @SerialName("imagen_id") val imageId: String,
    @SerialName("estilo") val style: String,
    @SerialName("texto") val text: String? = null,
    @SerialName("plan_id") val planId: String? = null,
    @SerialName("negocio_id") val placeId: String? = null,
    @SerialName("visibilidad") val visibility: String
)

@Serializable
data class MemoryStopDto(
    @SerialName("nombre") val name: String = "",
    @SerialName("hora") val time: String = "",
    @SerialName("foto_url") val photoUrl: String? = null
)

@Serializable
data class MemoryDto(
    @SerialName("id") val id: String,
    @SerialName("plan_id") val planId: String = "",
    @SerialName("titulo") val title: String = "",
    @SerialName("fecha") val date: String = "",
    @SerialName("paradas") val stops: List<MemoryStopDto> = emptyList(),
    @SerialName("postales_ids") val postcardIds: List<String> = emptyList()
)

@Serializable
data class MemoryPageDto(
    @SerialName("items") val items: List<MemoryDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class MomentCreateDto(
    @SerialName("postal_id") val postcardId: String,
    @SerialName("texto") val text: String? = null,
    @SerialName("visibilidad") val visibility: String,
    @SerialName("etiquetados") val tagged: List<String>? = null
)

@Serializable
data class MemoryAddressDto(@SerialName("calle") val street: String = "", @SerialName("numero") val number: String = "")

/** Lo que 75_PostalVer usa de `GET /lugares/{id}`. */
@Serializable
data class MemoryPlaceDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("categoria") val category: String = "",
    @SerialName("direccion") val address: MemoryAddressDto? = null
)

@Serializable
data class MemoryPlanStopDto(@SerialName("id") val id: String = "")

/** Lo que 75_PostalVer usa de `GET /planes/{id}`. */
@Serializable
data class MemoryPlanDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("fecha") val date: String = "",
    @SerialName("paradas") val stops: List<MemoryPlanStopDto> = emptyList()
)

/** wherever-backend `docs/endpoints.md`: postales, recuerdos y momentos (nv-31). */
interface MemoriesApi {
    @GET("postales")
    suspend fun postcards(@Query("cursor") cursor: String?, @Query("limite") limit: Int): PostcardPageDto

    @GET("postales/{id}")
    suspend fun postcard(@Path("id") id: String): PostcardDto

    /** 202 sin cuerpo; el id viene en la cabecera `Location`. */
    @POST("postales")
    suspend fun createPostcard(@Body body: PostcardCreateDto): Response<Unit>

    @DELETE("postales/{id}")
    suspend fun deletePostcard(@Path("id") id: String, @Header("If-Match") ifMatch: String)

    @DELETE("imagenes/{id}")
    suspend fun deleteImage(@Path("id") id: String)

    @GET("recuerdos")
    suspend fun memories(@Query("cursor") cursor: String?, @Query("limite") limit: Int): MemoryPageDto

    @POST("momentos")
    suspend fun createMoment(@Body body: MomentCreateDto)

    @GET("lugares/{id}")
    suspend fun place(@Path("id") id: String): MemoryPlaceDto

    @GET("planes/{id}")
    suspend fun plan(@Path("id") id: String): MemoryPlanDto
}
