package com.wherever.mobile.feature.reviews.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PendingReviewDto(
    @SerialName("checkin_id") val checkinId: String,
    @SerialName("negocio_id") val placeId: String,
    @SerialName("negocio_nombre") val placeName: String = "",
    @SerialName("dia") val day: String = "",
    @SerialName("vence_en") val expiresAt: String = ""
)

@Serializable
data class PendingReviewsDto(@SerialName("items") val items: List<PendingReviewDto> = emptyList())

@Serializable
data class CheckinDto(
    @SerialName("id") val id: String,
    @SerialName("negocio_id") val placeId: String,
    @SerialName("valido") val valid: Boolean = false,
    @SerialName("dia") val day: String = "",
    @SerialName("resena_id") val reviewId: String? = null
)

@Serializable
data class CheckinPageDto(
    @SerialName("items") val items: List<CheckinDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class ReviewPhotoDto(@SerialName("id") val id: String = "", @SerialName("url") val url: String = "")

@Serializable
data class BusinessReplyDto(@SerialName("texto") val text: String = "")

@Serializable
data class MyReviewDto(
    @SerialName("id") val id: String,
    @SerialName("negocio_id") val placeId: String = "",
    @SerialName("checkin_id") val checkinId: String = "",
    @SerialName("calificacion") val rating: Int = 0,
    @SerialName("aspectos") val aspects: Map<String, Int> = emptyMap(),
    @SerialName("chips") val chips: List<String>? = null,
    @SerialName("comentario") val comment: String? = null,
    @SerialName("fotos") val photos: List<ReviewPhotoDto>? = null,
    @SerialName("estado") val state: String = "",
    @SerialName("util") val useful: Int = 0,
    @SerialName("respuesta_negocio") val businessReply: BusinessReplyDto? = null,
    @SerialName("editable") val editable: Boolean = false,
    @SerialName("version") val version: Long = 0,
    @SerialName("creado_en") val createdAt: String = ""
)

@Serializable
data class MyReviewPageDto(
    @SerialName("items") val items: List<MyReviewDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

/** Cuerpo de `POST /resenas`: sin ubicación, sin ids ajenos y sin chips (no hay catálogo publicado, D-32b). */
@Serializable
data class ReviewCreateDto(
    @SerialName("checkin_id") val checkinId: String,
    @SerialName("calificacion") val rating: Int,
    @SerialName("aspectos") val aspects: Map<String, Int>,
    @SerialName("comentario") val comment: String? = null,
    @SerialName("fotos") val photos: List<String>? = null
)

/** Cuerpo de `PATCH /resenas/{id}`: solo lo que cambia. */
@Serializable
data class ReviewPatchDto(
    @SerialName("calificacion") val rating: Int? = null,
    @SerialName("aspectos") val aspects: Map<String, Int>? = null,
    @SerialName("comentario") val comment: String? = null,
    @SerialName("fotos") val photos: List<String>? = null
)

@Serializable
data class ReviewAuthorDto(
    @SerialName("nombre_usuario") val username: String = "",
    @SerialName("foto_url") val photoUrl: String? = null
)

@Serializable
data class PlaceReviewDto(
    @SerialName("id") val id: String,
    @SerialName("autor") val author: ReviewAuthorDto? = null,
    @SerialName("calificacion") val rating: Int = 0,
    @SerialName("comentario") val comment: String? = null,
    @SerialName("fotos") val photos: List<ReviewPhotoDto>? = null,
    @SerialName("util") val useful: Int = 0,
    @SerialName("respuesta_negocio") val businessReply: BusinessReplyDto? = null,
    @SerialName("creado_en") val createdAt: String = ""
)

@Serializable
data class PlaceReviewPageDto(
    @SerialName("items") val items: List<PlaceReviewDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class UsefulRequestDto(@SerialName("util") val useful: Boolean)

@Serializable
data class UsefulResponseDto(@SerialName("util") val useful: Boolean = false, @SerialName("total") val total: Int = 0)

@Serializable
data class ReviewReportDto(@SerialName("motivo") val reason: String)

@Serializable
data class PlacePhotoDto(@SerialName("url") val url: String = "")

@Serializable
data class PlaceRatingDto(
    @SerialName("promedio") val average: Double = 0.0,
    @SerialName("total_resenas") val total: Int = 0
)

/** Lo que usan las reseñas de `GET /lugares/{id}`: nombre, foto y calificación. */
@Serializable
data class PlaceBriefDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("fotos") val photos: List<PlacePhotoDto>? = null,
    @SerialName("calificacion") val rating: PlaceRatingDto? = null
)
