package com.wherever.mobile.feature.reviews.data

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.MyReview
import com.wherever.mobile.feature.reviews.domain.PendingReview
import com.wherever.mobile.feature.reviews.domain.PlaceBrief
import com.wherever.mobile.feature.reviews.domain.PlaceReview
import com.wherever.mobile.feature.reviews.domain.PlaceReviewQuery
import com.wherever.mobile.feature.reviews.domain.ReportReason
import com.wherever.mobile.feature.reviews.domain.ReviewDraft
import com.wherever.mobile.feature.reviews.domain.ReviewPhoto
import com.wherever.mobile.feature.reviews.domain.ReviewRepository
import com.wherever.mobile.feature.reviews.domain.ReviewState
import com.wherever.mobile.feature.reviews.domain.ReviewedVisit
import com.wherever.mobile.feature.reviews.domain.UsefulCount
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private const val PAGE_SIZE = 20

/** Check-ins que se revisan para «Ya reseñadas» (la API pide reseñar en 30 días). */
private const val CHECKIN_PAGE = 50

/**
 * Reseñas contra la API real. Room no guarda reseñas (spec §4): las listas se leen por cursor y las reseñas
 * propias quedan en memoria solo para abrir Editar con su `version`.
 */
@Singleton
class ReviewRepositoryImpl @Inject constructor(private val api: ReviewsApi) : ReviewRepository {
    private val mine = ConcurrentHashMap<String, MyReview>()
    private val places = ConcurrentHashMap<String, PlaceBrief>()

    override suspend fun pending(): Result<List<PendingReview>> = safeApiCall { api.pending().items }.map { items ->
        val briefs = briefs(items.map { it.placeId })
        items.map { dto ->
            PendingReview(
                checkinId = dto.checkinId,
                placeId = dto.placeId,
                placeName = dto.placeName.ifBlank { briefs[dto.placeId]?.name.orEmpty() },
                day = dto.day,
                expiresAt = dto.expiresAt,
                photoUrl = briefs[dto.placeId]?.photoUrl
            )
        }
    }

    override suspend fun reviewedVisits(): Result<List<ReviewedVisit>> =
        safeApiCall { api.checkins(cursor = null, limit = CHECKIN_PAGE).items }.map { items ->
            val reviewed = items.filter { it.valid && it.reviewId != null }
            val briefs = briefs(reviewed.map { it.placeId })
            reviewed.map { dto ->
                val brief = briefs[dto.placeId]
                ReviewedVisit(dto.id, dto.placeId, brief?.name.orEmpty(), dto.day, brief?.photoUrl)
            }
        }

    override suspend fun mine(cursor: String?): Result<PageResult<MyReview>> =
        safeApiCall { api.mine(cursor, PAGE_SIZE) }.map { page ->
            val briefs = briefs(page.items.map { it.placeId })
            val items = page.items.map { it.toDomain(briefs[it.placeId]) }
            items.forEach { mine[it.id] = it }
            PageResult(items, page.nextCursor?.takeIf { it.isNotBlank() })
        }

    override fun cached(id: String): MyReview? = mine[id]

    override suspend fun create(draft: ReviewDraft): Result<MyReview> = safeApiCall {
        api.create(
            ReviewCreateDto(
                checkinId = draft.checkinId,
                rating = draft.rating,
                aspects = draft.aspects.toApi(),
                comment = draft.comment.trim().takeIf { it.isNotEmpty() },
                photos = draft.photoIds.takeIf { it.isNotEmpty() }
            )
        )
    }.map { dto -> dto.toDomain(briefs(listOf(dto.placeId))[dto.placeId]).also { mine[it.id] = it } }

    override suspend fun update(review: MyReview, draft: ReviewDraft): Result<MyReview> = safeApiCall {
        api.update(review.id, "\"${review.version}\"", review.patchTo(draft))
    }.map { dto ->
        dto.toDomain(null).copy(placeName = review.placeName, placePhotoUrl = review.placePhotoUrl).also {
            mine[it.id] = it
        }
    }

    override suspend fun delete(id: String): Result<Unit> = safeApiCall { api.delete(id) }.also {
        if (it is Result.Success) mine.remove(id)
    }

    override suspend fun place(placeId: String): Result<PlaceBrief> =
        safeApiCall { api.place(placeId).toDomain() }.also { if (it is Result.Success) places[placeId] = it.data }

    override suspend fun placeReviews(
        placeId: String,
        query: PlaceReviewQuery,
        cursor: String?
    ): Result<PageResult<PlaceReview>> = safeApiCall {
        api.placeReviews(
            id = placeId,
            stars = query.stars,
            aspect = query.aspect?.apiKey,
            withPhotos = query.withPhotos.takeIf { it },
            order = if (query.recent) "recientes" else "utiles",
            cursor = cursor,
            limit = PAGE_SIZE
        )
    }.map { page -> PageResult(page.items.map { it.toDomain() }, page.nextCursor?.takeIf { it.isNotBlank() }) }

    override suspend fun markUseful(reviewId: String, useful: Boolean): Result<UsefulCount> =
        safeApiCall { api.useful(reviewId, UsefulRequestDto(useful)) }.map { UsefulCount(it.useful, it.total) }

    override suspend fun report(reviewId: String, reason: ReportReason): Result<Unit> =
        safeApiCall { api.report(reviewId, ReviewReportDto(reason.apiKey)) }

    /** Nombre y foto de cada lugar, una sola vez por lugar. Si una ficha falla, la fila sale sin foto. */
    private suspend fun briefs(ids: List<String>): Map<String, PlaceBrief> = coroutineScope {
        ids.distinct().filter { it.isNotBlank() && !places.containsKey(it) }.map { id ->
            async { (safeApiCall { api.place(id).toDomain() } as? Result.Success)?.data }
        }.awaitAll().filterNotNull().forEach { places[it.id] = it }
        ids.mapNotNull { id -> places[id]?.let { id to it } }.toMap()
    }
}

private fun Map<Aspect, Int>.toApi(): Map<String, Int> = entries.associate { (aspect, value) -> aspect.apiKey to value }

/** Solo los campos que cambian; la API vuelve a moderar el texto nuevo. */
private fun MyReview.patchTo(draft: ReviewDraft) = ReviewPatchDto(
    rating = draft.rating.takeIf { it != rating },
    aspects = draft.aspects.takeIf { it != aspects }?.toApi(),
    comment = draft.comment.trim().takeIf { it != comment },
    photos = draft.photoIds.takeIf { it != photos.map(ReviewPhoto::id) }
)

private fun PlaceBriefDto.toDomain() = PlaceBrief(
    id = id,
    name = name,
    photoUrl = photos?.firstOrNull()?.url?.takeIf { it.isNotBlank() },
    average = rating?.average ?: 0.0,
    total = rating?.total ?: 0
)

private fun MyReviewDto.toDomain(place: PlaceBrief?) = MyReview(
    id = id,
    placeId = placeId,
    checkinId = checkinId,
    rating = rating,
    aspects = aspects.mapNotNull { (key, value) -> Aspect.fromApi(key)?.let { it to value } }.toMap(),
    chips = chips.orEmpty(),
    comment = comment.orEmpty(),
    photos = photos.orEmpty().filter { it.url.isNotBlank() }.map { ReviewPhoto(it.id, it.url) },
    state = ReviewState.fromApi(state),
    useful = useful,
    businessReply = businessReply?.text?.takeIf { it.isNotBlank() },
    editable = editable,
    version = version,
    createdAt = createdAt,
    placeName = place?.name.orEmpty(),
    placePhotoUrl = place?.photoUrl
)

private fun PlaceReviewDto.toDomain() = PlaceReview(
    id = id,
    author = author?.username.orEmpty(),
    authorPhotoUrl = author?.photoUrl?.takeIf { it.isNotBlank() },
    rating = rating,
    comment = comment.orEmpty(),
    photos = photos.orEmpty().map { it.url }.filter { it.isNotBlank() },
    useful = useful,
    businessReply = businessReply?.text?.takeIf { it.isNotBlank() },
    createdAt = createdAt
)
