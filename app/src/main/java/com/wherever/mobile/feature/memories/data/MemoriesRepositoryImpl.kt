package com.wherever.mobile.feature.memories.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.network.ApiErrorDto
import com.wherever.mobile.core.network.ApiJson
import com.wherever.mobile.core.network.mapApiError
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.memories.domain.MemoriesRepository
import com.wherever.mobile.feature.memories.domain.Memory
import com.wherever.mobile.feature.memories.domain.MemoryStop
import com.wherever.mobile.feature.memories.domain.MomentDraft
import com.wherever.mobile.feature.memories.domain.NewPostcard
import com.wherever.mobile.feature.memories.domain.PlaceInfo
import com.wherever.mobile.feature.memories.domain.PlanInfo
import com.wherever.mobile.feature.memories.domain.Postcard
import com.wherever.mobile.feature.memories.domain.PostcardState
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import com.wherever.mobile.feature.memories.domain.PostcardView
import com.wherever.mobile.feature.memories.domain.Visibility
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.Response

private const val PAGE_SIZE = 20

/** Postales, recuerdos y momentos contra la API real. Sin Room (spec §4): todo se lee por cursor. */
class MemoriesRepositoryImpl @Inject constructor(private val api: MemoriesApi) : MemoriesRepository {
    override suspend fun postcards(cursor: String?): Result<PageResult<Postcard>> =
        safeApiCall { api.postcards(cursor, PAGE_SIZE) }.map { page ->
            PageResult(page.items.map { it.toDomain() }, page.nextCursor?.takeIf { it.isNotBlank() })
        }

    override suspend fun memories(cursor: String?): Result<PageResult<Memory>> =
        safeApiCall { api.memories(cursor, PAGE_SIZE) }.map { page ->
            PageResult(page.items.map { it.toDomain() }, page.nextCursor?.takeIf { it.isNotBlank() })
        }

    /** La postal manda; si el lugar o el plan no se pueden leer, la pantalla se muestra sin esas filas. */
    override suspend fun postcard(id: String): Result<PostcardView> = when (
        val result = safeApiCall { api.postcard(id).toDomain() }
    ) {
        is Result.Failure -> result
        is Result.Success -> coroutineScope {
            val card = result.data
            val place = async { card.placeId?.let { placeId -> safeApiCall { api.place(placeId).toDomain() } } }
            val plan = async { card.planId?.let { planId -> safeApiCall { api.plan(planId).toDomain() } } }
            Result.Success(
                PostcardView(
                    postcard = card,
                    place = (place.await() as? Result.Success)?.data,
                    plan = (plan.await() as? Result.Success)?.data
                )
            )
        }
    }

    override suspend fun createPostcard(postcard: NewPostcard): Result<String> = safeApiCall {
        api.createPostcard(
            PostcardCreateDto(
                imageId = postcard.imageId,
                style = postcard.style.apiKey,
                text = postcard.text.trim().takeIf { it.isNotEmpty() },
                planId = postcard.planId?.takeIf { it.isNotBlank() },
                placeId = postcard.placeId?.takeIf { it.isNotBlank() },
                visibility = postcard.visibility.apiKey
            )
        )
    }.let { result ->
        when (result) {
            is Result.Failure -> result
            is Result.Success -> {
                val response = result.data
                val id = response.headers()["Location"]?.trimEnd('/')?.substringAfterLast('/')
                when {
                    !response.isSuccessful -> Result.Failure(response.toAppError())
                    id.isNullOrBlank() -> Result.Failure(AppError.InvalidResponse)
                    else -> Result.Success(id)
                }
            }
        }
    }

    override suspend fun deletePostcard(postcard: Postcard): Result<Unit> =
        safeApiCall { api.deletePostcard(postcard.id, "\"${postcard.version}\"") }

    override suspend fun deleteImage(imageId: String): Result<Unit> = safeApiCall { api.deleteImage(imageId) }

    override suspend fun createMoment(draft: MomentDraft): Result<Unit> = safeApiCall {
        api.createMoment(
            MomentCreateDto(
                postcardId = draft.postcardId,
                text = draft.text.trim().takeIf { it.isNotEmpty() },
                visibility = draft.visibility.apiKey,
                tagged = draft.tagged.takeIf { it.isNotEmpty() }
            )
        )
    }
}

/** Con `Response<T>` Retrofit no lanza en 4xx/5xx: se traduce igual que `safeApiCall`. */
private fun Response<*>.toAppError(): AppError {
    val body = runCatching {
        errorBody()?.string()?.let { ApiJson.decodeFromString<ApiErrorDto>(it) }
    }.getOrNull()
    return mapApiError(code(), body, headers()["Retry-After"]?.trim()?.toLongOrNull())
}

private fun PostcardDto.toDomain() = Postcard(
    id = id,
    imageUrl = imageUrl?.takeIf { it.isNotBlank() },
    style = PostcardStyle.fromApi(style),
    text = text.orEmpty(),
    visibility = Visibility.fromApi(visibility),
    state = PostcardState.fromApi(state),
    planId = planId?.takeIf { it.isNotBlank() },
    placeId = placeId?.takeIf { it.isNotBlank() },
    version = version,
    createdAt = createdAt
)

private fun MemoryDto.toDomain() = Memory(
    id = id,
    planId = planId,
    title = title,
    date = date,
    stops = stops.map { MemoryStop(it.name, it.time, it.photoUrl?.takeIf { url -> url.isNotBlank() }) },
    postcardIds = postcardIds
)

private fun MemoryPlaceDto.toDomain() = PlaceInfo(
    id = id,
    name = name,
    category = category,
    address = listOfNotNull(address?.street, address?.number).filter { it.isNotBlank() }.joinToString(" ")
)

private fun MemoryPlanDto.toDomain() = PlanInfo(id = id, title = name, date = date, stops = stops.size)
