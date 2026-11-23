package com.wherever.mobile.feature.business.domain

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.reviews.domain.Aspect

/** Respuesta del negocio: 1 a 500 caracteres (la maqueta dice 300; manda la API). */
const val REPLY_MAX = 500

/** Periodo del resumen (`?periodo=`). */
enum class ReviewPeriod(val apiValue: String) { MONTH("mes"), QUARTER("trimestre") }

/** `GET /negocios/{id}/resenas/resumen`: promedio y aspectos de las reseñas publicadas. */
data class ReviewSummary(
    val total: Int,
    val average: Double?,
    val aspects: Map<Aspect, Double>,
    val previousAverage: Double?,
    /** Reseñas por estrellas (1 a 5); vacío si la API no la manda. */
    val distribution: Map<Int, Int> = emptyMap()
)

/** Estado de la respuesta publicada: la moderación puede dejarla en revisión. */
enum class ReplyStatus { PUBLISHED, IN_MODERATION }

/** Momento público de un cliente en el negocio (`GET /negocios/{id}/momentos`). */
data class BusinessMoment(
    val id: String,
    val author: String,
    val authorPhotoUrl: String?,
    val imageUrl: String?,
    val text: String?,
    val createdAt: String,
    val thanked: Boolean,
    val featured: Boolean
)

/** Reseñas, respuesta y momentos del modo negocio (specs/nv-36). La lista de reseñas es la de la ficha. */
interface BusinessReviewsRepository {
    suspend fun summary(businessId: String, period: ReviewPeriod): Result<ReviewSummary>

    /** `PUT respuesta` (1–500). Moderación rechazada = `Validation` con el motivo de la API. */
    suspend fun reply(businessId: String, reviewId: String, text: String): Result<ReplyStatus>

    /** «Sugerir con IA»: solo viaja el id de la reseña; la IA nunca publica sola. Sin IA = 503. */
    suspend fun suggestReply(businessId: String, reviewId: String): Result<String>

    suspend fun moments(businessId: String, featured: Boolean): Result<List<BusinessMoment>>

    /** Agradecer o destacar (al menos uno); avisa al autor. */
    suspend fun markMoment(
        businessId: String,
        momentId: String,
        thanked: Boolean?,
        featured: Boolean?
    ): Result<BusinessMoment>
}

/** Respuesta válida: 1 a 500 caracteres sin contar espacios de los extremos. */
fun isValidReply(text: String): Boolean = text.trim().length in 1..REPLY_MAX
