package com.wherever.mobile.feature.places.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.places.domain.AspectRatings
import com.wherever.mobile.feature.places.domain.Category
import com.wherever.mobile.feature.places.domain.DaySchedule
import com.wherever.mobile.feature.places.domain.FavoritePlace
import com.wherever.mobile.feature.places.domain.FavoritesRepository
import com.wherever.mobile.feature.places.domain.MenuItem
import com.wherever.mobile.feature.places.domain.MenuSection
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.places.domain.PlaceDetail
import com.wherever.mobile.feature.places.domain.PlaceFilters
import com.wherever.mobile.feature.places.domain.PlacePage
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.places.domain.RecentSearch
import com.wherever.mobile.feature.places.domain.ReportReason
import com.wherever.mobile.feature.places.domain.Review
import com.wherever.mobile.feature.places.domain.ReviewPage
import com.wherever.mobile.feature.places.domain.ReviewQuery
import com.wherever.mobile.feature.places.domain.ReviewsRepository
import com.wherever.mobile.feature.places.domain.Traffic
import javax.inject.Inject
import kotlin.math.roundToInt

private const val PAGE_SIZE = 20
private const val SEARCH_LIMIT = 20
private const val RECENT_LIMIT = 10
private const val REVIEW_PAGE = 10

class PlacesRepositoryImpl @Inject constructor(private val api: PlacesApi) : PlacesRepository {
    override suspend fun categories(): Result<List<Category>> = safeApiCall {
        api.categories().categories.map { Category(it.id, it.name, it.count) }
    }

    override suspend fun list(filters: PlaceFilters, near: GeoPoint?, cursor: String?): Result<PlacePage> =
        safeApiCall {
            val page = api.list(
                category = filters.category,
                maxPriceLevel = filters.maxPriceLevel,
                openNow = filters.openNow.takeIf { it },
                lat = near?.lat,
                lng = near?.lng,
                // La API pide `lat` y `lng` para filtrar por distancia; sin ubicación el radio no aplica.
                radiusMeters = filters.radiusMeters.takeIf { near != null },
                order = filters.order?.apiValue,
                cursor = cursor,
                limit = PAGE_SIZE
            )
            PlacePage(page.items.map { it.toDomain() }, page.nextCursor?.takeIf { it.isNotBlank() })
        }

    override suspend fun search(query: String): Result<List<PlaceCard>> = safeApiCall {
        api.search(query.trim(), SEARCH_LIMIT).items.map { it.toDomain() }
    }

    override suspend fun recentSearches(): Result<List<RecentSearch>> = safeApiCall {
        api.recentSearches().items.map { RecentSearch(it.text) }
    }

    override suspend fun clearRecentSearches(): Result<Unit> = safeApiCall { api.clearRecentSearches() }

    override suspend fun recentlyViewed(): Result<List<PlaceCard>> = safeApiCall {
        api.recentlyViewed(RECENT_LIMIT).items.map { it.toDomain() }
    }

    override suspend fun detail(id: String): Result<PlaceDetail> = safeApiCall { api.detail(id).toDomain() }

    override suspend fun traffic(id: String): Result<Traffic> = safeApiCall {
        api.traffic(id).let { Traffic(it.day, it.hours, it.currentHour, it.enough && it.hours.isNotEmpty()) }
    }

    override suspend fun menu(id: String): Result<List<MenuSection>> = safeApiCall {
        api.menu(id).sections.map { section ->
            MenuSection(
                section.name,
                section.items.map { MenuItem(it.name, it.description.orEmpty(), it.price.roundToInt()) }
            )
        }
    }

    override suspend fun registerView(id: String, origin: String): Result<Unit> =
        safeApiCall { api.registerView(id, ViewDto(origin)) }

    override suspend fun report(id: String, reason: ReportReason, detail: String?): Result<Unit> = safeApiCall {
        api.report(id, ReportDto(reason.apiValue, detail?.trim()?.takeIf { it.isNotEmpty() }))
    }
}

class ReviewsRepositoryImpl @Inject constructor(private val api: PlacesApi) : ReviewsRepository {
    override suspend fun reviews(placeId: String, query: ReviewQuery): Result<ReviewPage> = safeApiCall {
        val page = api.reviews(
            id = placeId,
            stars = query.stars,
            order = if (query.recent) "recientes" else "utiles",
            cursor = query.cursor,
            limit = REVIEW_PAGE
        )
        ReviewPage(page.items.map { it.toDomain() }, page.nextCursor?.takeIf { it.isNotBlank() })
    }

    override suspend fun markReviewUseful(reviewId: String, useful: Boolean): Result<Unit> =
        safeApiCall { api.useful(reviewId, UsefulDto(useful)) }
}

class FavoritesRepositoryImpl @Inject constructor(private val favoritesApi: FavoritesApi) : FavoritesRepository {
    override suspend fun favoriteIds(): Result<Set<String>> = safeApiCall { favoritesApi.favoriteIds().ids.toSet() }

    override suspend fun setFavorite(id: String, favorite: Boolean): Result<Unit> = safeApiCall {
        if (favorite) favoritesApi.addFavorite(id) else favoritesApi.removeFavorite(id)
    }

    override suspend fun favorites(): Result<List<FavoritePlace>> = safeApiCall {
        favoritesApi.favorites().items.map {
            FavoritePlace(
                id = it.targetId,
                name = it.summary?.name.orEmpty(),
                category = it.summary?.category,
                photoUrl = it.summary?.photoUrl?.takeIf { url -> url.isNotBlank() },
                openNow = it.openNow
            )
        }
    }
}

fun PlaceCardDto.toDomain(): PlaceCard = PlaceCard(
    id = id,
    name = name,
    category = category,
    priceLevel = priceLevel,
    averagePrice = averagePrice,
    rating = rating?.average ?: 0.0,
    reviews = rating?.reviews ?: 0,
    coverUrl = cover?.takeIf { it.isNotBlank() },
    distanceMeters = distanceMeters,
    openNow = openNow,
    why = why?.takeIf { it.isNotBlank() }
)

internal fun PlaceDetailDto.toDomain(): PlaceDetail = PlaceDetail(
    id = id,
    name = name,
    category = category,
    description = description.orEmpty(),
    tags = tags.orEmpty(),
    priceLevel = priceLevel,
    averagePrice = averagePrice,
    location = GeoPoint(location.lat, location.lng),
    address = address?.let { a ->
        listOf("${a.street} ${a.number}".trim(), a.neighborhood, a.city, a.state).filter { it.isNotBlank() }
            .joinToString(", ")
    }.orEmpty(),
    phone = phone?.takeIf { it.isNotBlank() },
    schedules = schedules.orEmpty().map { DaySchedule(it.day, it.opens, it.closes, it.closed) },
    photos = photos.orEmpty().sortedBy { it.order }.map { it.url }.filter { it.isNotBlank() },
    services = services.orEmpty(),
    accessibility = buildList {
        accessibility?.let {
            if (it.wheelchair) add("silla_ruedas")
            if (it.kids) add("apto_ninos")
            if (it.pets) add("pet_friendly")
        }
    },
    dietOptions = dietOptions.orEmpty(),
    acceptsReservations = acceptsReservations,
    rating = rating?.average ?: 0.0,
    reviews = rating?.reviews ?: 0,
    aspects = (rating?.aspects ?: AspectsDto()).let {
        AspectRatings(it.taste, it.service, it.ambience, it.wait, it.value)
    },
    verified = verified,
    excellenceSeal = excellenceSeal,
    openNow = openNow
)

private fun ReviewDto.toDomain(): Review = Review(
    id = id,
    author = author?.username.orEmpty(),
    rating = rating,
    comment = comment.orEmpty(),
    chips = chips.orEmpty(),
    useful = useful,
    businessReply = businessReply?.text?.takeIf { it.isNotBlank() },
    createdAt = createdAt
)
