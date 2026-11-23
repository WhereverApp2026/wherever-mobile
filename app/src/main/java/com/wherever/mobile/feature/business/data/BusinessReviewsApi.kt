package com.wherever.mobile.feature.business.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class SummaryPeriodDto(
    @SerialName("total") val total: Int = 0,
    @SerialName("promedio") val average: Double? = null,
    @SerialName("aspectos") val aspects: Map<String, Double?> = emptyMap()
)

@Serializable
data class ReviewSummaryDto(
    @SerialName("actual") val current: SummaryPeriodDto = SummaryPeriodDto(),
    @SerialName("anterior") val previous: SummaryPeriodDto? = null,
    /** Reseñas por estrellas: `{"1": n, …, "5": n}`. */
    @SerialName("distribucion") val distribution: Map<String, Int> = emptyMap()
)

@Serializable
data class ReplyRequestDto(@SerialName("texto") val text: String)

@Serializable
data class ReplyStateDto(@SerialName("texto") val text: String = "", @SerialName("estado") val status: String = "")

@Serializable
data class ReplyResponseDto(@SerialName("respuesta_negocio") val reply: ReplyStateDto? = null)

@Serializable
data class SuggestedReplyDto(@SerialName("texto") val text: String = "")

@Serializable
data class MomentAuthorRefDto(
    @SerialName("id") val id: String = "",
    @SerialName("nombre_usuario") val username: String? = null,
    @SerialName("foto_url") val photoUrl: String? = null
)

@Serializable
data class MomentImageDto(@SerialName("imagen_url") val imageUrl: String? = null)

@Serializable
data class BusinessMomentDto(
    @SerialName("id") val id: String,
    @SerialName("autor") val author: MomentAuthorRefDto = MomentAuthorRefDto(),
    @SerialName("postal") val postcard: MomentImageDto? = null,
    @SerialName("texto") val text: String? = null,
    @SerialName("creado_en") val createdAt: String = "",
    @SerialName("agradecido_por_negocio") val thanked: Boolean = false,
    @SerialName("destacado_por_negocio") val featured: Boolean = false
)

@Serializable
data class BusinessMomentPageDto(@SerialName("items") val items: List<BusinessMomentDto> = emptyList())

@Serializable
data class MomentMarkDto(
    @SerialName("agradecido") val thanked: Boolean? = null,
    @SerialName("destacado") val featured: Boolean? = null
)

/** wherever-backend `docs/endpoints.md`: reseñas, respuesta y momentos del negocio (specs/nv-36). */
interface BusinessReviewsApi {
    @GET("negocios/{id}/resenas/resumen")
    suspend fun summary(@Path("id") id: String, @Query("periodo") period: String): ReviewSummaryDto

    @PUT("negocios/{id}/resenas/{rid}/respuesta")
    suspend fun reply(
        @Path("id") id: String,
        @Path("rid") reviewId: String,
        @Body body: ReplyRequestDto
    ): ReplyResponseDto

    @POST("negocios/{id}/resenas/{rid}/sugerir-respuesta")
    suspend fun suggestReply(@Path("id") id: String, @Path("rid") reviewId: String): SuggestedReplyDto

    @GET("negocios/{id}/momentos")
    suspend fun moments(
        @Path("id") id: String,
        @Query("destacados") featured: Boolean,
        @Query("limite") limit: Int
    ): BusinessMomentPageDto

    @PATCH("negocios/{id}/momentos/{mid}")
    suspend fun markMoment(
        @Path("id") id: String,
        @Path("mid") momentId: String,
        @Body body: MomentMarkDto
    ): BusinessMomentDto
}
