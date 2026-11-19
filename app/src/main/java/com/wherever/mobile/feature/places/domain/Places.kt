package com.wherever.mobile.feature.places.domain

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint

/** Tarjeta de lugar de `GET /lugares`, `/buscar`, `/recientes`, `/recomendaciones` e `/inicio`. */
data class PlaceCard(
    val id: String,
    val name: String,
    val category: String,
    val priceLevel: Int,
    val averagePrice: Int,
    val rating: Double,
    val reviews: Int,
    val coverUrl: String?,
    val distanceMeters: Double?,
    val openNow: Boolean,
    val why: String? = null
)

data class PlacePage(val items: List<PlaceCard>, val nextCursor: String?)

enum class SortOrder(val apiValue: String) { DISTANCE("distancia"), RATING("calificacion"), PRICE("precio") }

/** Filtros de Explorar (US-028). `null` = sin filtro. */
data class PlaceFilters(
    val category: String? = null,
    val maxPriceLevel: Int? = null,
    val openNow: Boolean = false,
    val radiusMeters: Int? = null,
    val order: SortOrder? = null
) {
    val isEmpty: Boolean get() = this == PlaceFilters()
}

data class Category(val id: String, val name: String, val count: Int)

data class DaySchedule(val day: Int, val opens: String, val closes: String, val closed: Boolean)

/** Promedio por aspecto (US-068). 0 = sin calificaciones de ese aspecto. */
data class AspectRatings(
    val taste: Double,
    val service: Double,
    val ambience: Double,
    val wait: Double,
    val value: Double
)

data class PlaceDetail(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val tags: List<String>,
    val priceLevel: Int,
    val averagePrice: Int,
    val location: GeoPoint,
    val address: String,
    val phone: String?,
    val schedules: List<DaySchedule>,
    val photos: List<String>,
    val services: List<String>,
    val accessibility: List<String>,
    val dietOptions: List<String>,
    val acceptsReservations: Boolean,
    val rating: Double,
    val reviews: Int,
    val aspects: AspectRatings,
    val verified: Boolean,
    val excellenceSeal: Boolean,
    val openNow: Boolean
)

/** Afluencia por hora (US-069). `enough = false`: no hay datos suficientes. */
data class Traffic(val day: Int, val hours: List<Int>, val currentHour: Int?, val enough: Boolean)

data class MenuItem(val name: String, val description: String, val price: Int)

data class MenuSection(val name: String, val items: List<MenuItem>)

/** Reseña pública (US-074, US-111). Todas vienen de una visita verificada (backend nv-62). */
data class Review(
    val id: String,
    val author: String,
    val rating: Int,
    val comment: String,
    val chips: List<String>,
    val useful: Int,
    val businessReply: String?,
    val createdAt: String
)

data class ReviewPage(val items: List<Review>, val nextCursor: String?)

data class ReviewQuery(val stars: Int? = null, val recent: Boolean = false, val cursor: String? = null)

enum class ReportReason(val apiValue: String) {
    SCHEDULE("horario"),
    PRICE("precio"),
    CLOSED("cerrado_permanente"),
    LOCATION("ubicacion"),
    OTHER("otro")
}

data class RecentSearch(val text: String)

data class FavoritePlace(
    val id: String,
    val name: String,
    val category: String?,
    val photoUrl: String?,
    val openNow: Boolean?
)

interface PlacesRepository {
    suspend fun categories(): Result<List<Category>>

    suspend fun list(filters: PlaceFilters, near: GeoPoint?, cursor: String?): Result<PlacePage>

    suspend fun search(query: String): Result<List<PlaceCard>>

    suspend fun recentSearches(): Result<List<RecentSearch>>

    suspend fun clearRecentSearches(): Result<Unit>

    suspend fun recentlyViewed(): Result<List<PlaceCard>>

    suspend fun detail(id: String): Result<PlaceDetail>

    suspend fun traffic(id: String): Result<Traffic>

    suspend fun menu(id: String): Result<List<MenuSection>>

    /** Registra la vista para "Vistos recientemente" (US-034). Un fallo no se muestra. */
    suspend fun registerView(id: String, origin: String): Result<Unit>

    suspend fun report(id: String, reason: ReportReason, detail: String?): Result<Unit>
}

/** Reseñas públicas de un negocio (US-074, US-121). */
interface ReviewsRepository {
    suspend fun reviews(placeId: String, query: ReviewQuery): Result<ReviewPage>

    suspend fun markReviewUseful(reviewId: String, useful: Boolean): Result<Unit>
}

/** Favoritos de negocios (US-029). */
interface FavoritesRepository {
    suspend fun favoriteIds(): Result<Set<String>>

    suspend fun setFavorite(id: String, favorite: Boolean): Result<Unit>

    suspend fun favorites(): Result<List<FavoritePlace>>
}
