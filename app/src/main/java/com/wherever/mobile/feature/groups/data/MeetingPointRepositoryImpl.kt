package com.wherever.mobile.feature.groups.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.groups.domain.Group
import com.wherever.mobile.feature.groups.domain.MeetingPlace
import com.wherever.mobile.feature.groups.domain.MeetingPointRepository
import com.wherever.mobile.feature.groups.domain.MeetingSuggestion
import com.wherever.mobile.feature.groups.domain.MemberTrip
import com.wherever.mobile.feature.groups.domain.TripRange
import javax.inject.Inject

class MeetingPointRepositoryImpl @Inject constructor(private val api: MeetingPointApi) : MeetingPointRepository {
    override suspend fun shareLocation(groupId: String, point: GeoPoint?): Result<Group> = safeApiCall {
        val body = ShareLocationDto(point?.let { LatLngDto(it.lat, it.lng) }, share = point != null)
        api.shareLocation(groupId, body).toGroup()
    }

    override suspend fun suggest(groupId: String): Result<MeetingSuggestion> = safeApiCall {
        val dto = api.suggestMeeting(groupId)
        val places = dto.places.mapNotNull { place ->
            // Sin ubicación no se puede dibujar ni fijar como punto.
            val at = place.location ?: return@mapNotNull null
            MeetingPlace(place.id, place.name, place.category, place.rating?.average ?: 0.0, at.lat, at.lng)
        }
        MeetingSuggestion(places, dto.participants)
    }

    override suspend fun setPoint(groupId: String, placeId: String, at: String): Result<Group> = safeApiCall {
        api.setMeetingPoint(groupId, SetMeetingPointDto(placeId, at)).toGroup()
    }

    override suspend fun trips(groupId: String): Result<List<MemberTrip>> = safeApiCall {
        api.trips(groupId).items.map {
            MemberTrip(
                userId = it.id,
                name = it.name,
                photoUrl = it.photoUrl?.takeIf(String::isNotBlank),
                mine = it.mine,
                minutes = it.minutes,
                cost = it.cost,
                range = TripRange.from(it.range),
                noLocation = it.noLocation
            )
        }
    }
}
