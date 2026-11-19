package com.wherever.mobile.feature.home.data

import com.wherever.mobile.feature.places.data.PlaceCardDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Contratos de wherever-backend docs/endpoints.md (nv-24, nv-25, nv-65).

@Serializable
data class WeatherDto(
    @SerialName("disponible") val available: Boolean = false,
    @SerialName("temperatura_c") val temperatureC: Double? = null,
    @SerialName("estado") val condition: String? = null,
    @SerialName("adverso") val adverse: Boolean? = null
)

@Serializable
data class MoodChipDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("categoria") val category: String = ""
)

@Serializable
data class HomeDto(
    @SerialName("saludo") val greeting: String = "",
    @SerialName("clima") val weather: WeatherDto? = null,
    @SerialName("chips") val chips: List<MoodChipDto> = emptyList(),
    @SerialName("recomendaciones") val recommendations: List<PlaceCardDto> = emptyList()
)

@Serializable
data class EventDto(
    @SerialName("id") val id: String,
    @SerialName("titulo") val title: String = "",
    @SerialName("tipo") val type: String = "",
    @SerialName("direccion") val address: String? = null,
    @SerialName("inicio") val startsAt: String = "",
    @SerialName("precio") val priceCents: Int = 0,
    @SerialName("foto_url") val photoUrl: String? = null
)

@Serializable
data class EventPageDto(@SerialName("items") val items: List<EventDto> = emptyList())

interface HomeApi {
    @GET("inicio")
    suspend fun home(
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?,
        @Query("compania") company: String?
    ): HomeDto

    @GET("eventos")
    suspend fun events(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("dias") days: Int,
        @Query("limite") limit: Int
    ): EventPageDto

    @POST("recomendaciones/{id}/no-me-interesa")
    suspend fun notInterested(@Path("id") placeId: String)

    @DELETE("recomendaciones/{id}/no-me-interesa")
    suspend fun undoNotInterested(@Path("id") placeId: String)
}
