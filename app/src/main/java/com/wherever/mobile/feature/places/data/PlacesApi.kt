package com.wherever.mobile.feature.places.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Suppress("LongParameterList")
interface PlacesApi {
    @GET("categorias")
    suspend fun categories(): CategoriesDto

    @GET("lugares")
    suspend fun list(
        @Query("categoria") category: String?,
        @Query("precio_max") maxPriceLevel: Int?,
        @Query("abierto_ahora") openNow: Boolean?,
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?,
        @Query("radio_m") radiusMeters: Int?,
        @Query("orden") order: String?,
        @Query("cursor") cursor: String?,
        @Query("limite") limit: Int
    ): PlacePageDto

    @GET("lugares/buscar")
    suspend fun search(@Query("q") query: String, @Query("limite") limit: Int): SearchResultDto

    @GET("busquedas/recientes")
    suspend fun recentSearches(): RecentSearchesDto

    @DELETE("busquedas/recientes")
    suspend fun clearRecentSearches()

    @GET("lugares/recientes")
    suspend fun recentlyViewed(@Query("limite") limit: Int): SearchResultDto

    @GET("lugares/{id}")
    suspend fun detail(@Path("id") id: String): PlaceDetailDto

    @GET("lugares/{id}/afluencia")
    suspend fun traffic(@Path("id") id: String): TrafficDto

    @GET("lugares/{id}/menu")
    suspend fun menu(@Path("id") id: String): MenuDto

    @GET("lugares/{id}/resenas")
    suspend fun reviews(
        @Path("id") id: String,
        @Query("estrellas") stars: Int?,
        @Query("orden") order: String,
        @Query("cursor") cursor: String?,
        @Query("limite") limit: Int
    ): ReviewPageDto

    @PUT("resenas/{id}/util")
    suspend fun useful(@Path("id") reviewId: String, @Body body: UsefulDto)

    @POST("lugares/{id}/vistas")
    suspend fun registerView(@Path("id") id: String, @Body body: ViewDto)

    @POST("lugares/{id}/reportes")
    suspend fun report(@Path("id") id: String, @Body body: ReportDto)
}

/** Favoritos (backend nv-54). `tipo` es `negocio` o `plan`. */
interface FavoritesApi {
    @GET("favoritos/ids")
    suspend fun favoriteIds(@Query("tipo") type: String = FAVORITE_PLACE): FavoriteIdsDto

    @PUT("favoritos/{tipo}/{id}")
    suspend fun addFavorite(@Path("id") id: String, @Path("tipo") type: String = FAVORITE_PLACE)

    @DELETE("favoritos/{tipo}/{id}")
    suspend fun removeFavorite(@Path("id") id: String, @Path("tipo") type: String = FAVORITE_PLACE)

    @GET("favoritos")
    suspend fun favorites(
        @Query("tipo") type: String = FAVORITE_PLACE,
        @Query("limite") limit: Int = 50
    ): FavoritePageDto

    companion object {
        const val FAVORITE_PLACE = "negocio"
    }
}
