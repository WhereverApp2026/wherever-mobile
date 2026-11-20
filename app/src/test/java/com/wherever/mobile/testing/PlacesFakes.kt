package com.wherever.mobile.testing

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.location.LocationPermissionPreferences
import com.wherever.mobile.core.location.LocationProvider
import com.wherever.mobile.feature.home.domain.Company
import com.wherever.mobile.feature.home.domain.Event
import com.wherever.mobile.feature.home.domain.HomeFeed
import com.wherever.mobile.feature.home.domain.HomeRepository
import com.wherever.mobile.feature.places.domain.AspectRatings
import com.wherever.mobile.feature.places.domain.Category
import com.wherever.mobile.feature.places.domain.FavoritePlace
import com.wherever.mobile.feature.places.domain.FavoritesRepository
import com.wherever.mobile.feature.places.domain.MenuSection
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.places.domain.PlaceDetail
import com.wherever.mobile.feature.places.domain.PlaceFilters
import com.wherever.mobile.feature.places.domain.PlacePage
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.places.domain.RecentSearch
import com.wherever.mobile.feature.places.domain.ReportReason
import com.wherever.mobile.feature.places.domain.ReviewPage
import com.wherever.mobile.feature.places.domain.ReviewQuery
import com.wherever.mobile.feature.places.domain.ReviewsRepository
import com.wherever.mobile.feature.places.domain.Traffic

/** Coordenadas del centro de Guadalajara: datos de prueba, no de una persona. */
val TEST_POINT = GeoPoint(20.6767, -103.3475)

class FakeLocationProvider(var granted: Boolean = false, var point: GeoPoint? = TEST_POINT) : LocationProvider {
    override fun hasPermission(): Boolean = granted

    override suspend fun current(): GeoPoint? = point.takeIf { granted }
}

class FakeLocationPermissionPreferences(
    override var deniedCount: Int = 0,
    override var dismissed: Boolean = false,
    override var notificationsDecided: Boolean = false
) : LocationPermissionPreferences {
    override fun recordNotificationsDecided() {
        notificationsDecided = true
    }

    override fun recordDenied() {
        deniedCount++
    }

    override fun dismiss() {
        dismissed = true
    }
}

fun samplePlace(
    id: String = "p1",
    name: String = "Café Andrade",
    category: String = "cafeteria",
    openNow: Boolean = true
) = PlaceCard(
    id = id,
    name = name,
    category = category,
    priceLevel = 2,
    averagePrice = 150,
    rating = 4.5,
    reviews = 12,
    coverUrl = null,
    distanceMeters = 800.0,
    openNow = openNow,
    why = "Te gustan los cafés"
)

fun samplePlaceDetail(id: String = "p1") = PlaceDetail(
    id = id,
    name = "Café Andrade",
    category = "cafeteria",
    description = "Café de especialidad",
    tags = emptyList(),
    priceLevel = 2,
    averagePrice = 150,
    location = TEST_POINT,
    address = "Av. Juárez 10, Centro, Guadalajara",
    phone = null,
    schedules = emptyList(),
    photos = emptyList(),
    services = emptyList(),
    accessibility = emptyList(),
    dietOptions = emptyList(),
    acceptsReservations = false,
    rating = 4.5,
    reviews = 12,
    aspects = AspectRatings(4.5, 4.0, 4.2, 3.9, 4.1),
    verified = true,
    excellenceSeal = false,
    openNow = true
)

@Suppress("TooManyFunctions")
class FakePlacesRepository :
    PlacesRepository,
    ReviewsRepository,
    FavoritesRepository {
    var listResult: Result<PlacePage> = Result.Success(PlacePage(listOf(samplePlace()), nextCursor = null))
    var searchResult: Result<List<PlaceCard>> = Result.Success(listOf(samplePlace()))
    var detailResult: Result<PlaceDetail> = Result.Success(samplePlaceDetail())
    var favoriteResult: Result<Unit> = Result.Success(Unit)
    var reportResult: Result<Unit> = Result.Success(Unit)
    var usefulResult: Result<Unit> = Result.Success(Unit)
    var favoriteIdsResult: Result<Set<String>> = Result.Success(emptySet())
    val listCalls = mutableListOf<Pair<PlaceFilters, String?>>()
    val searches = mutableListOf<String>()
    val favoriteCalls = mutableListOf<Pair<String, Boolean>>()
    val reports = mutableListOf<Pair<ReportReason, String?>>()
    val views = mutableListOf<Pair<String, String>>()
    val reviewQueries = mutableListOf<ReviewQuery>()
    var recentCleared = false

    override suspend fun categories(): Result<List<Category>> =
        Result.Success(listOf(Category("cafeteria", "Cafeterías", 3)))

    override suspend fun list(filters: PlaceFilters, near: GeoPoint?, cursor: String?): Result<PlacePage> {
        listCalls += filters to cursor
        return listResult
    }

    override suspend fun search(query: String): Result<List<PlaceCard>> {
        searches += query
        return searchResult
    }

    override suspend fun recentSearches(): Result<List<RecentSearch>> = Result.Success(listOf(RecentSearch("tacos")))

    override suspend fun clearRecentSearches(): Result<Unit> {
        recentCleared = true
        return Result.Success(Unit)
    }

    override suspend fun recentlyViewed(): Result<List<PlaceCard>> = Result.Success(emptyList())

    val detailCalls = mutableListOf<String>()

    override suspend fun detail(id: String): Result<PlaceDetail> {
        detailCalls += id
        return detailResult
    }

    override suspend fun traffic(id: String): Result<Traffic> =
        Result.Success(Traffic(day = 3, hours = List(24) { it }, currentHour = 12, enough = true))

    override suspend fun menu(id: String): Result<List<MenuSection>> = Result.Success(emptyList())

    override suspend fun reviews(placeId: String, query: ReviewQuery): Result<ReviewPage> {
        reviewQueries += query
        return Result.Success(ReviewPage(emptyList(), nextCursor = null))
    }

    override suspend fun markReviewUseful(reviewId: String, useful: Boolean): Result<Unit> = usefulResult

    override suspend fun registerView(id: String, origin: String): Result<Unit> {
        views += id to origin
        return Result.Success(Unit)
    }

    override suspend fun report(id: String, reason: ReportReason, detail: String?): Result<Unit> {
        reports += reason to detail
        return reportResult
    }

    override suspend fun favoriteIds(): Result<Set<String>> = favoriteIdsResult

    override suspend fun setFavorite(id: String, favorite: Boolean): Result<Unit> {
        favoriteCalls += id to favorite
        return favoriteResult
    }

    override suspend fun favorites(): Result<List<FavoritePlace>> =
        Result.Success(listOf(FavoritePlace("p1", "Café Andrade", "cafeteria", null, true)))
}

class FakeHomeRepository : HomeRepository {
    var feedResult: Result<HomeFeed> = Result.Success(
        HomeFeed("Buenos días", null, emptyList(), listOf(samplePlace("p1"), samplePlace("p2", "Tacos Don Gus")))
    )
    var dismissResult: Result<Unit> = Result.Success(Unit)
    val feedCalls = mutableListOf<Pair<GeoPoint?, Company?>>()
    val eventCalls = mutableListOf<GeoPoint>()
    val dismissed = mutableListOf<String>()
    val undone = mutableListOf<String>()

    override suspend fun feed(near: GeoPoint?, company: Company?): Result<HomeFeed> {
        feedCalls += near to company
        return feedResult
    }

    override suspend fun events(near: GeoPoint): Result<List<Event>> {
        eventCalls += near
        val event = Event("e1", "Tianguis cultural", "tianguis", "Centro", "2026-11-21T16:00:00Z", 0, null)
        return Result.Success(listOf(event))
    }

    override suspend fun notInterested(placeId: String): Result<Unit> {
        dismissed += placeId
        return dismissResult
    }

    override suspend fun undoNotInterested(placeId: String): Result<Unit> {
        undone += placeId
        return Result.Success(Unit)
    }
}

val NETWORK_FAILURE: Result.Failure = Result.Failure(AppError.Network)
