package com.wherever.mobile.feature.home.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.home.domain.Company
import com.wherever.mobile.feature.home.domain.Event
import com.wherever.mobile.feature.home.domain.HomeFeed
import com.wherever.mobile.feature.home.domain.HomeRepository
import com.wherever.mobile.feature.home.domain.MoodChip
import com.wherever.mobile.feature.home.domain.Weather
import com.wherever.mobile.feature.places.data.toDomain
import javax.inject.Inject
import kotlin.math.roundToInt

private const val EVENT_DAYS = 7
private const val EVENT_LIMIT = 10

class HomeRepositoryImpl @Inject constructor(private val api: HomeApi) : HomeRepository {
    override suspend fun feed(near: GeoPoint?, company: Company?): Result<HomeFeed> = safeApiCall {
        val dto = api.home(near?.lat, near?.lng, company?.apiValue)
        HomeFeed(
            greeting = dto.greeting,
            weather = dto.weather?.let { w ->
                w.temperatureC?.takeIf { w.available }?.let {
                    Weather(it.roundToInt(), w.condition.orEmpty(), w.adverse == true)
                }
            },
            chips = dto.chips.map { MoodChip(it.id, it.name, it.category) },
            recommendations = dto.recommendations.map { it.toDomain() }
        )
    }

    override suspend fun events(near: GeoPoint): Result<List<Event>> = safeApiCall {
        api.events(near.lat, near.lng, EVENT_DAYS, EVENT_LIMIT).items.map {
            Event(
                id = it.id,
                title = it.title,
                type = it.type,
                address = it.address.orEmpty(),
                startsAt = it.startsAt,
                priceCents = it.priceCents,
                photoUrl = it.photoUrl?.takeIf { url -> url.isNotBlank() }
            )
        }
    }

    override suspend fun notInterested(placeId: String): Result<Unit> = safeApiCall { api.notInterested(placeId) }

    override suspend fun undoNotInterested(placeId: String): Result<Unit> =
        safeApiCall { api.undoNotInterested(placeId) }
}
