package com.wherever.mobile.feature.business.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.business.domain.BusinessMoment
import com.wherever.mobile.feature.business.domain.BusinessReviewsRepository
import com.wherever.mobile.feature.business.domain.ReplyStatus
import com.wherever.mobile.feature.business.domain.ReviewPeriod
import com.wherever.mobile.feature.business.domain.ReviewSummary
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.MAX_STARS
import javax.inject.Inject

/** Momentos que se piden de una vez. */
private const val MOMENTS_LIMIT = 30

/** Reseñas y momentos del negocio contra la API real (specs/nv-36). */
class BusinessReviewsRepositoryImpl @Inject constructor(private val api: BusinessReviewsApi) :
    BusinessReviewsRepository {
    override suspend fun summary(businessId: String, period: ReviewPeriod): Result<ReviewSummary> = safeApiCall {
        val dto = api.summary(businessId, period.apiValue)
        ReviewSummary(
            total = dto.current.total,
            average = dto.current.average,
            aspects = dto.current.aspects.mapNotNull { (key, value) ->
                val aspect = Aspect.fromApi(key)
                if (aspect != null && value != null) aspect to value else null
            }.toMap(),
            previousAverage = dto.previous?.average,
            distribution = dto.distribution.mapNotNull { (key, value) ->
                key.toIntOrNull()?.takeIf { it in 1..MAX_STARS }?.let { it to value }
            }.toMap()
        )
    }

    override suspend fun reply(businessId: String, reviewId: String, text: String): Result<ReplyStatus> = safeApiCall {
        val dto = api.reply(businessId, reviewId, ReplyRequestDto(text.trim()))
        if (dto.reply?.status == "en_moderacion") ReplyStatus.IN_MODERATION else ReplyStatus.PUBLISHED
    }

    override suspend fun suggestReply(businessId: String, reviewId: String): Result<String> =
        safeApiCall { api.suggestReply(businessId, reviewId).text }

    override suspend fun moments(businessId: String, featured: Boolean): Result<List<BusinessMoment>> = safeApiCall {
        api.moments(businessId, featured, MOMENTS_LIMIT).items.map { it.toDomain() }
    }

    override suspend fun markMoment(
        businessId: String,
        momentId: String,
        thanked: Boolean?,
        featured: Boolean?
    ): Result<BusinessMoment> = safeApiCall {
        api.markMoment(businessId, momentId, MomentMarkDto(thanked, featured)).toDomain()
    }
}

private fun BusinessMomentDto.toDomain() = BusinessMoment(
    id = id,
    author = author.username.orEmpty(),
    authorPhotoUrl = author.photoUrl,
    imageUrl = postcard?.imageUrl,
    text = text?.takeIf { it.isNotBlank() },
    createdAt = createdAt,
    thanked = thanked,
    featured = featured
)
