package com.wherever.mobile.feature.reviews.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** wherever-backend `docs/endpoints.md`, grupo reseñas. Propia de nv-32 (`PlacesApi` no se toca, D-32a). */
interface ReviewsApi {
    @GET("resenas/pendientes")
    suspend fun pending(): PendingReviewsDto

    @GET("checkins")
    suspend fun checkins(@Query("cursor") cursor: String?, @Query("limite") limit: Int): CheckinPageDto

    @GET("resenas/mias")
    suspend fun mine(@Query("cursor") cursor: String?, @Query("limite") limit: Int): MyReviewPageDto

    @POST("resenas")
    suspend fun create(@Body body: ReviewCreateDto): MyReviewDto

    @PATCH("resenas/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Header("If-Match") ifMatch: String,
        @Body body: ReviewPatchDto
    ): MyReviewDto

    @DELETE("resenas/{id}")
    suspend fun delete(@Path("id") id: String)

    @GET("lugares/{id}")
    suspend fun place(@Path("id") id: String): PlaceBriefDto

    @GET("lugares/{id}/resenas")
    @Suppress("LongParameterList") // Los filtros de la API son parámetros de consulta.
    suspend fun placeReviews(
        @Path("id") id: String,
        @Query("estrellas") stars: Int?,
        @Query("aspecto") aspect: String?,
        @Query("con_fotos") withPhotos: Boolean?,
        @Query("orden") order: String,
        @Query("cursor") cursor: String?,
        @Query("limite") limit: Int
    ): PlaceReviewPageDto

    @PUT("resenas/{id}/util")
    suspend fun useful(@Path("id") id: String, @Body body: UsefulRequestDto): UsefulResponseDto

    @POST("resenas/{id}/reportes")
    suspend fun report(@Path("id") id: String, @Body body: ReviewReportDto)
}
