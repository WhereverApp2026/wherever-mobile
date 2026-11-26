package com.wherever.mobile.feature.places.domain

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.tan

/** Zoom de la portada de mapa: una manzana alrededor del lugar (specs/nv-21, D-21b). */
const val COVER_ZOOM = 17

/**
 * Tesela de OpenStreetMap (esquema XYZ de Web Mercator) que contiene un punto, y dónde cae el punto dentro de
 * ella: `fx` y `fy` van de 0 (borde izquierdo o superior) a 1.
 */
data class TilePosition(val zoom: Int, val x: Int, val y: Int, val fx: Double, val fy: Double)

/** La tesela y la posición del punto dentro de ella, para centrar la portada en el lugar. */
fun tileFor(lat: Double, lng: Double, zoom: Int = COVER_ZOOM): TilePosition {
    val n = (1 shl zoom).toDouble()
    val worldX = (lng + HALF_TURN) / FULL_TURN * n
    val latRad = Math.toRadians(lat.coerceIn(-MAX_LAT, MAX_LAT))
    val worldY = (1 - ln(tan(latRad) + 1 / cos(latRad)) / PI) / 2 * n
    val x = floor(worldX)
    val y = floor(worldY)
    return TilePosition(zoom, x.toInt(), y.toInt(), worldX - x, worldY - y)
}

/** URL de una tesela de OSM; `x` se envuelve para no salirse del mundo. */
fun tileUrl(zoom: Int, x: Int, y: Int): String {
    val n = 1 shl zoom
    val wrapped = ((x % n) + n) % n
    return "$OSM_TILES/$zoom/$wrapped/$y.png"
}

/** Servidor de teselas estándar de OSM, el mismo que usa osmdroid (MAPNIK). */
const val OSM_TILES = "https://tile.openstreetmap.org"

private const val HALF_TURN = 180.0
private const val FULL_TURN = 360.0

/** Web Mercator no llega a los polos. */
private const val MAX_LAT = 85.0511
