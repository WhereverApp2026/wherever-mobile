package com.wherever.mobile.feature.places.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.places.domain.MapArea
import com.wherever.mobile.feature.places.domain.MapCell
import com.wherever.mobile.feature.places.domain.MapPin
import com.wherever.mobile.feature.places.domain.PlaceMap
import com.wherever.mobile.feature.places.domain.PlaceMapRepository
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class MapPinDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("categoria") val category: String = "",
    @SerialName("calificacion") val rating: Double = 0.0,
    @SerialName("ubicacion") val location: PointDto = PointDto()
)

@Serializable
data class MapCellDto(@SerialName("centro") val center: PointDto = PointDto(), @SerialName("conteo") val count: Int = 0)

@Serializable
data class PlaceMapDto(
    @SerialName("agrupado") val grouped: Boolean = false,
    @SerialName("pines") val pins: List<MapPinDto> = emptyList(),
    @SerialName("celdas") val cells: List<MapCellDto> = emptyList()
)

/** wherever-backend `docs/endpoints.md`: `GET /lugares/mapa` (nv-21). */
interface PlaceMapApi {
    // Los seis son los parámetros de la API (zona, zoom y giro); agruparlos solo movería el problema.
    @Suppress("LongParameterList")
    @GET("lugares/mapa")
    suspend fun map(
        @Query("min_lat") minLat: Double,
        @Query("min_lng") minLng: Double,
        @Query("max_lat") maxLat: Double,
        @Query("max_lng") maxLng: Double,
        @Query("zoom") zoom: Int,
        @Query("categoria") category: String?
    ): PlaceMapDto
}

class PlaceMapRepositoryImpl @Inject constructor(private val api: PlaceMapApi) : PlaceMapRepository {
    override suspend fun area(area: MapArea, category: String?): Result<PlaceMap> = safeApiCall {
        val dto = api.map(area.minLat, area.minLng, area.maxLat, area.maxLng, area.zoom, category)
        PlaceMap(
            grouped = dto.grouped,
            pins = dto.pins.map { MapPin(it.id, it.name, it.category, it.rating, it.location.lat, it.location.lng) },
            cells = dto.cells.map { MapCell(it.center.lat, it.center.lng, it.count) }
        )
    }
}
