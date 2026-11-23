package com.wherever.mobile.feature.places.domain

import com.wherever.mobile.core.common.Result

/** Zona visible del mapa. La API acepta `zoom` de 3 a 20 y un ancho menor a 180°. */
data class MapArea(val minLat: Double, val minLng: Double, val maxLat: Double, val maxLng: Double, val zoom: Int) {
    companion object {
        const val MIN_ZOOM = 3
        const val MAX_ZOOM = 20
        private const val MAX_LAT = 90.0
        private const val MAX_LNG = 180.0

        /** Recorta lo que pide la API para no recibir un 422 por una zona fuera de rango. */
        fun of(south: Double, west: Double, north: Double, east: Double, zoom: Double): MapArea = MapArea(
            minLat = south.coerceIn(-MAX_LAT, MAX_LAT),
            minLng = west.coerceIn(-MAX_LNG, MAX_LNG),
            maxLat = north.coerceIn(-MAX_LAT, MAX_LAT),
            maxLng = east.coerceIn(-MAX_LNG, MAX_LNG),
            zoom = zoom.toInt().coerceIn(MIN_ZOOM, MAX_ZOOM)
        )
    }
}

data class MapPin(
    val id: String,
    val name: String,
    val category: String,
    val rating: Double,
    val lat: Double,
    val lng: Double
)

/** Varios lugares juntos: con zoom bajo la API agrupa en celdas. */
data class MapCell(val lat: Double, val lng: Double, val count: Int)

data class PlaceMap(val grouped: Boolean, val pins: List<MapPin>, val cells: List<MapCell>)

/** 32_ExpMapa (specs/nv-21): lugares de la zona visible. */
interface PlaceMapRepository {
    suspend fun area(area: MapArea, category: String? = null): Result<PlaceMap>
}
