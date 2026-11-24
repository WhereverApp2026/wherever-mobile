package com.wherever.mobile.testing

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.business.domain.BusinessMoment
import com.wherever.mobile.feature.business.domain.BusinessReviewsRepository
import com.wherever.mobile.feature.business.domain.ReplyStatus
import com.wherever.mobile.feature.business.domain.ReviewPeriod
import com.wherever.mobile.feature.business.domain.ReviewSummary
import com.wherever.mobile.feature.reviews.domain.Aspect

fun sampleMoment(id: String = "m1", thanked: Boolean = false, featured: Boolean = false) =
    BusinessMoment(id, "sofi", null, null, null, "2026-11-20T19:30:00Z", thanked, featured)

/** Reseñas y momentos del negocio en memoria (nv-36); cada llamada queda en [calls]. */
class FakeBusinessReviewsRepository : BusinessReviewsRepository {
    val calls = mutableListOf<String>()
    var summaryResult: Result<ReviewSummary> = Result.Success(
        ReviewSummary(10, 4.8, mapOf(Aspect.TASTE to 9.4), 4.6)
    )
    var replyResult: Result<ReplyStatus> = Result.Success(ReplyStatus.PUBLISHED)
    var suggestResult: Result<String> = Result.Success("¡Gracias por venir!")
    var momentsResult: Result<List<BusinessMoment>> = Result.Success(
        listOf(sampleMoment("m1"), sampleMoment("m2", thanked = true, featured = true))
    )
    var markResult: Result<BusinessMoment>? = null
    val periods = mutableListOf<ReviewPeriod>()
    val replies = mutableListOf<String>()

    override suspend fun summary(businessId: String, period: ReviewPeriod) = summaryResult.also { periods += period }

    override suspend fun reply(businessId: String, reviewId: String, text: String): Result<ReplyStatus> {
        replies += text
        return replyResult
    }

    override suspend fun suggestReply(businessId: String, reviewId: String) =
        suggestResult.also { calls += "sugerir $reviewId" }

    override suspend fun moments(businessId: String, featured: Boolean) = momentsResult.let { result ->
        calls += "momentos $featured"
        if (featured && result is Result.Success) Result.Success(result.data.filter { it.featured }) else result
    }

    override suspend fun markMoment(
        businessId: String,
        momentId: String,
        thanked: Boolean?,
        featured: Boolean?
    ): Result<BusinessMoment> {
        calls += "marcar $momentId $thanked $featured"
        val current = (momentsResult as Result.Success).data.first { it.id == momentId }
        return markResult ?: Result.Success(
            current.copy(thanked = thanked ?: current.thanked, featured = featured ?: current.featured)
        )
    }
}
