package com.wherever.mobile.testing

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.photos.domain.PhotoRepository
import com.wherever.mobile.feature.photos.domain.UploadedImage
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.MyReview
import com.wherever.mobile.feature.reviews.domain.PendingReview
import com.wherever.mobile.feature.reviews.domain.PlaceBrief
import com.wherever.mobile.feature.reviews.domain.PlaceReview
import com.wherever.mobile.feature.reviews.domain.PlaceReviewQuery
import com.wherever.mobile.feature.reviews.domain.ReportReason
import com.wherever.mobile.feature.reviews.domain.ReviewDraft
import com.wherever.mobile.feature.reviews.domain.ReviewRepository
import com.wherever.mobile.feature.reviews.domain.ReviewState
import com.wherever.mobile.feature.reviews.domain.ReviewedVisit
import com.wherever.mobile.feature.reviews.domain.UsefulCount

fun sampleMyReview(
    id: String = "r1",
    editable: Boolean = true,
    version: Long = 1,
    createdAt: String = "2026-11-20T17:00:00Z"
) = MyReview(
    id = id,
    placeId = "p1",
    checkinId = "c1",
    rating = 4,
    aspects = Aspect.entries.associateWith { 4 },
    chips = emptyList(),
    comment = "Muy rico",
    photos = emptyList(),
    state = ReviewState.PUBLISHED,
    useful = 0,
    businessReply = null,
    editable = editable,
    version = version,
    createdAt = createdAt,
    placeName = "Café Raíz"
)

/** Repositorio de reseñas en memoria: anota las llamadas y responde lo que diga cada prueba. */
class FakeReviewRepository : ReviewRepository {
    val calls = mutableListOf<String>()
    var pendingResult: Result<List<PendingReview>> = Result.Success(emptyList())
    var visitsResult: Result<List<ReviewedVisit>> = Result.Success(emptyList())
    var mineResult: Result<PageResult<MyReview>> = Result.Success(PageResult(emptyList(), null))
    var createResult: Result<MyReview>? = null
    var updateResult: Result<MyReview>? = null
    var deleteResult: Result<Unit> = Result.Success(Unit)
    var placeResult: Result<PlaceBrief> = Result.Success(PlaceBrief("p1", "Café Raíz", null, 4.5, 10))
    var placeReviewsResult: Result<PageResult<PlaceReview>> = Result.Success(PageResult(emptyList(), null))
    var usefulResult: Result<UsefulCount> = Result.Success(UsefulCount(true, 1))
    var reportResult: Result<Unit> = Result.Success(Unit)
    val cache = mutableMapOf<String, MyReview>()
    val drafts = mutableListOf<ReviewDraft>()
    val queries = mutableListOf<PlaceReviewQuery>()

    override suspend fun pending() = pendingResult.also { calls += "pending" }

    override suspend fun reviewedVisits() = visitsResult.also { calls += "visits" }

    override suspend fun mine(cursor: String?): Result<PageResult<MyReview>> {
        calls += "mine:$cursor"
        (mineResult as? Result.Success)?.data?.items?.forEach { cache[it.id] = it }
        return mineResult
    }

    override fun cached(id: String): MyReview? = cache[id]

    override suspend fun create(draft: ReviewDraft): Result<MyReview> {
        calls += "create"
        drafts += draft
        return createResult ?: Result.Success(sampleMyReview().copy(rating = draft.rating, comment = draft.comment))
    }

    override suspend fun update(review: MyReview, draft: ReviewDraft): Result<MyReview> {
        calls += "update:${review.id}:v${review.version}"
        drafts += draft
        return updateResult ?: Result.Success(review.copy(rating = draft.rating, version = review.version + 1))
    }

    override suspend fun delete(id: String): Result<Unit> = deleteResult.also { calls += "delete:$id" }

    override suspend fun place(placeId: String) = placeResult.also { calls += "place:$placeId" }

    override suspend fun placeReviews(
        placeId: String,
        query: PlaceReviewQuery,
        cursor: String?
    ): Result<PageResult<PlaceReview>> {
        calls += "placeReviews:$cursor"
        queries += query
        return placeReviewsResult
    }

    override suspend fun markUseful(reviewId: String, useful: Boolean) = usefulResult.also {
        calls +=
            "useful:$reviewId:$useful"
    }

    override suspend fun report(reviewId: String, reason: ReportReason) =
        reportResult.also { calls += "report:$reviewId:${reason.apiKey}" }
}

/** Subida de fotos falsa: devuelve [result] y anota el origen. */
class FakePhotoRepository(var result: Result<UploadedImage>) : PhotoRepository {
    val sources = mutableListOf<String>()

    override suspend fun upload(source: String): Result<UploadedImage> = result.also { sources += source }
}
