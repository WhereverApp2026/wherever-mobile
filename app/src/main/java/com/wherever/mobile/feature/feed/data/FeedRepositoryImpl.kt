package com.wherever.mobile.feature.feed.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.feed.domain.FeedRepository
import com.wherever.mobile.feature.feed.domain.Moment
import com.wherever.mobile.feature.feed.domain.MomentAuthor
import com.wherever.mobile.feature.feed.domain.MomentPage
import javax.inject.Inject

private const val PAGE_SIZE = 20

class FeedRepositoryImpl @Inject constructor(private val api: FeedApi) : FeedRepository {
    override suspend fun feed(cursor: String?): Result<MomentPage> = safeApiCall {
        api.feed(cursor?.takeIf { it.isNotBlank() }, PAGE_SIZE)
    }.map { page -> MomentPage(page.items.map { it.toDomain() }, page.nextCursor?.takeIf { it.isNotBlank() }) }

    override suspend fun moment(id: String): Result<Moment> = safeApiCall { api.moment(id).toDomain() }

    override suspend fun joinPlan(planId: String): Result<Unit> = safeApiCall { api.joinPlan(planId) }.map { }
}

/** La API manda "" en vez de null en usuario, foto y textos vacíos. */
private fun String?.orNullIfBlank(): String? = this?.takeIf { it.isNotBlank() }

internal fun MomentDto.toDomain() = Moment(
    id = id,
    author = MomentAuthor(author.id, author.username.orNullIfBlank(), author.photoUrl.orNullIfBlank()),
    imageUrl = postcard?.imageUrl.orNullIfBlank(),
    text = text.orNullIfBlank() ?: postcard?.text.orNullIfBlank(),
    placeId = placeId.orNullIfBlank(),
    planId = planId.orNullIfBlank(),
    createdAt = createdAt,
    own = own
)
