package com.wherever.mobile.testing

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.feature.groups.domain.Group
import com.wherever.mobile.feature.groups.domain.MeetingPlace
import com.wherever.mobile.feature.groups.domain.MeetingPoint
import com.wherever.mobile.feature.groups.domain.MeetingPointRepository
import com.wherever.mobile.feature.groups.domain.MeetingSuggestion
import com.wherever.mobile.feature.groups.domain.MemberTrip

val SAMPLE_MEETING_PLACES = listOf(
    MeetingPlace("n1", "Restaurante Colonial", "restaurante", 4.6, 20.057, -99.337),
    MeetingPlace("n2", "Café del Centro", "cafeteria", 0.0, 20.056, -99.340)
)

class FakeMeetingPointRepository : MeetingPointRepository {
    var suggestResult: Result<MeetingSuggestion> = Result.Success(MeetingSuggestion(SAMPLE_MEETING_PLACES, 2))
    var tripsResult: Result<List<MemberTrip>> = Result.Success(emptyList())
    var setResult: ((Group) -> Result<Group>)? = null
    var group: Group = sampleGroup()
    val calls = mutableListOf<String>()

    override suspend fun shareLocation(groupId: String, point: GeoPoint?): Result<Group> {
        calls += "share:$groupId:${point?.let { "${it.lat},${it.lng}" } ?: "off"}"
        group =
            group.copy(
                members = group.members.map {
                    if (it.userId ==
                        "u1"
                    ) {
                        it.copy(sharesLocation = point != null)
                    } else {
                        it
                    }
                }
            )
        return Result.Success(group)
    }

    override suspend fun suggest(groupId: String): Result<MeetingSuggestion> {
        calls += "suggest:$groupId"
        return suggestResult
    }

    override suspend fun setPoint(groupId: String, placeId: String, at: String): Result<Group> {
        calls += "set:$groupId:$placeId:$at"
        val place = SAMPLE_MEETING_PLACES.first { it.id == placeId }
        group = group.copy(meetingPoint = MeetingPoint(place.name, place.lat, place.lng, at, placeId))
        return setResult?.invoke(group) ?: Result.Success(group)
    }

    override suspend fun trips(groupId: String): Result<List<MemberTrip>> {
        calls += "trips:$groupId"
        return tripsResult
    }
}
