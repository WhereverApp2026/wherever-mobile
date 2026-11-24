package com.wherever.mobile.feature.feed.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class MomentAuthorDto(
    @SerialName("id") val id: String,
    @SerialName("nombre_usuario") val username: String? = null,
    @SerialName("foto_url") val photoUrl: String? = null
)

@Serializable
data class MomentPostcardDto(
    @SerialName("id") val id: String,
    @SerialName("imagen_url") val imageUrl: String? = null,
    @SerialName("texto") val text: String? = null
)

@Serializable
data class MomentDto(
    @SerialName("id") val id: String,
    @SerialName("autor") val author: MomentAuthorDto,
    @SerialName("postal") val postcard: MomentPostcardDto? = null,
    @SerialName("negocio_id") val placeId: String? = null,
    @SerialName("texto") val text: String? = null,
    @SerialName("plan_id") val planId: String? = null,
    @SerialName("propio") val own: Boolean = false,
    @SerialName("creado_en") val createdAt: String = "",
    /** nv-83: me gusta y comentarios publicados. */
    @SerialName("reacciones_total") val likes: Int = 0,
    @SerialName("comentarios_total") val comments: Int = 0
)

@Serializable
data class MomentPageDto(
    @SerialName("items") val items: List<MomentDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

/** Solo interesa que respondió: el grupo se abrirá con nv-25. */
@Serializable
data class JoinedGroupDto(@SerialName("id") val id: String = "")

interface FeedApi {
    @GET("momentos/feed")
    suspend fun feed(@Query("cursor") cursor: String?, @Query("limite") limit: Int): MomentPageDto

    @GET("momentos/{id}")
    suspend fun moment(@Path("id") id: String): MomentDto

    @POST("planes/{id}/me-apunto")
    suspend fun joinPlan(@Path("id") planId: String): JoinedGroupDto
}
