package com.wherever.mobile.feature.reviews.domain

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result

/** Límites de `POST /resenas` (specs/nv-32). */
const val COMMENT_MAX_LENGTH = 500
const val MAX_PHOTOS = 3
const val MAX_STARS = 5

/** Aspectos que califica `78_ResCom`, con su nombre en la API. La maqueta trae cuatro; la API pide cinco (D-32a). */
enum class Aspect(val apiKey: String) {
    TASTE("sabor"),
    SERVICE("atencion"),
    AMBIENCE("ambiente"),
    WAIT("espera"),
    VALUE("precio_calidad");

    companion object {
        fun fromApi(key: String): Aspect? = entries.firstOrNull { it.apiKey == key }
    }
}

enum class ReviewState(val apiKey: String) {
    PUBLISHED("publicada"),
    IN_MODERATION("en_moderacion"),
    HIDDEN("oculta");

    companion object {
        fun fromApi(key: String): ReviewState = entries.firstOrNull { it.apiKey == key } ?: PUBLISHED
    }
}

/** Motivos de `POST /resenas/{id}/reportes`. */
enum class ReportReason(val apiKey: String) {
    OFFENSIVE("ofensiva"),
    FAKE("falsa"),
    SPAM("spam"),
    PERSONAL_DATA("datos_personales"),
    OTHER("otro")
}

/** Nombre y foto del lugar, de `GET /lugares/{id}`. */
data class PlaceBrief(val id: String, val name: String, val photoUrl: String?, val average: Double, val total: Int)

/** Visita con check-in válido sin reseña (`GET /resenas/pendientes`). */
data class PendingReview(
    val checkinId: String,
    val placeId: String,
    val placeName: String,
    val day: String,
    val expiresAt: String,
    val photoUrl: String? = null
)

/** Visita ya reseñada, de `GET /checkins` (`resena_id` no nulo). */
data class ReviewedVisit(
    val checkinId: String,
    val placeId: String,
    val placeName: String,
    val day: String,
    val photoUrl: String?
)

data class ReviewPhoto(val id: String, val url: String)

/** Reseña propia (`GET /resenas/mias`, `POST` y `PATCH /resenas`). */
data class MyReview(
    val id: String,
    val placeId: String,
    val checkinId: String,
    val rating: Int,
    val aspects: Map<Aspect, Int>,
    val chips: List<String>,
    val comment: String,
    val photos: List<ReviewPhoto>,
    val state: ReviewState,
    val useful: Int,
    val businessReply: String?,
    val editable: Boolean,
    val version: Long,
    val createdAt: String,
    val placeName: String = "",
    val placePhotoUrl: String? = null
)

/** Reseña pública de un lugar (`GET /lugares/{id}/resenas`). Solo `nombre_usuario` y foto del autor. */
data class PlaceReview(
    val id: String,
    val author: String,
    val authorPhotoUrl: String?,
    val rating: Int,
    val comment: String,
    val photos: List<String>,
    val useful: Int,
    val businessReply: String?,
    val createdAt: String
)

/** Filtros de `80_ResLugar`. `aspect` = ese aspecto ≥ 4. */
data class PlaceReviewQuery(
    val stars: Int? = null,
    val aspect: Aspect? = null,
    val withPhotos: Boolean = false,
    val recent: Boolean = false
)

/** Lo que se publica o se edita. Solo los campos que pide la API. */
data class ReviewDraft(
    val checkinId: String,
    val rating: Int,
    val aspects: Map<Aspect, Int>,
    val comment: String,
    val photoIds: List<String>
)

data class UsefulCount(val useful: Boolean, val total: Int)

/** Reseñas (specs/nv-32). La implementación vive en `data`; la UI solo ve esta interfaz. */
interface ReviewRepository {
    suspend fun pending(): Result<List<PendingReview>>

    suspend fun reviewedVisits(): Result<List<ReviewedVisit>>

    suspend fun mine(cursor: String? = null): Result<PageResult<MyReview>>

    /** La última versión leída de una reseña propia (para Editar); null si no se ha cargado. */
    fun cached(id: String): MyReview?

    suspend fun create(draft: ReviewDraft): Result<MyReview>

    /** `PATCH` con `If-Match` de [MyReview.version]; 409 = versión vieja o en moderación. */
    suspend fun update(review: MyReview, draft: ReviewDraft): Result<MyReview>

    suspend fun delete(id: String): Result<Unit>

    suspend fun place(placeId: String): Result<PlaceBrief>

    suspend fun placeReviews(placeId: String, query: PlaceReviewQuery, cursor: String?): Result<PageResult<PlaceReview>>

    suspend fun markUseful(reviewId: String, useful: Boolean): Result<UsefulCount>

    suspend fun report(reviewId: String, reason: ReportReason): Result<Unit>
}

/** Una reseña completa: estrellas y los cinco aspectos (la API los pide todos). */
fun Map<Aspect, Int>.isComplete(): Boolean = Aspect.entries.all { (this[it] ?: 0) in 1..MAX_STARS }
