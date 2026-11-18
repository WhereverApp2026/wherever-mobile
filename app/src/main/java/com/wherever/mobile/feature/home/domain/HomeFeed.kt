package com.wherever.mobile.feature.home.domain

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.feature.places.domain.PlaceCard

/** Con quién sale la persona (US-062). `apiValue` es el de `?compania=`. */
enum class Company(val apiValue: String) { SOLO("solo"), COUPLE("pareja"), FRIENDS("amigos"), FAMILY("familia") }

/** Clima de `GET /contexto/clima` (US-047). `adverse` = lluvia o calor extremo. */
data class Weather(val temperatureC: Int, val condition: String, val adverse: Boolean)

/** Antojo sugerido según la hora (US-025). */
data class MoodChip(val id: String, val name: String, val category: String)

data class Event(
    val id: String,
    val title: String,
    val type: String,
    val address: String,
    val startsAt: String,
    val priceCents: Int,
    val photoUrl: String?
)

data class HomeFeed(
    val greeting: String,
    val weather: Weather?,
    val chips: List<MoodChip>,
    val recommendations: List<PlaceCard>
)

interface HomeRepository {
    suspend fun feed(near: GeoPoint?, company: Company?): Result<HomeFeed>

    /** Eventos, ferias y tianguis de los próximos 7 días (US-030). La API pide ubicación. */
    suspend fun events(near: GeoPoint): Result<List<Event>>

    suspend fun notInterested(placeId: String): Result<Unit>

    suspend fun undoNotInterested(placeId: String): Result<Unit>
}
