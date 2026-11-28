package com.wherever.mobile.feature.places.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** nv-21 D-21b: la portada de mapa pide la tesela correcta y centra el lugar. */
class MapTilesTest {
    @Test
    fun `el origen cae en el centro del mundo`() {
        val tile = tileFor(0.0, 0.0, zoom = 1)

        assertEquals(1, tile.x)
        assertEquals(1, tile.y)
        assertEquals(0.0, tile.fx, 1e-9)
        assertEquals(0.0, tile.fy, 1e-9)
    }

    @Test
    fun `Restaurante Cafe Madero en Tula cae en su tesela de zoom 17`() {
        // Coordenadas reales de staging (GET /lugares/{id}).
        val tile = tileFor(20.05889995, -99.33517675)

        assertEquals(COVER_ZOOM, tile.zoom)
        assertEquals(29_369, tile.x)
        assertEquals(58_078, tile.y)
        assertTrue(tile.fx in 0.0..1.0)
        assertTrue(tile.fy in 0.0..1.0)
    }

    @Test
    fun `la URL es la de OSM y envuelve x fuera del mundo`() {
        assertEquals("https://tile.openstreetmap.org/17/29369/58078.png", tileUrl(17, 29_369, 58_078))
        assertEquals("https://tile.openstreetmap.org/2/0/1.png", tileUrl(2, 4, 1))
        assertEquals("https://tile.openstreetmap.org/2/3/1.png", tileUrl(2, -1, 1))
    }

    @Test
    fun `latitudes fuera de Web Mercator se recortan`() {
        val north = tileFor(89.9, 0.0, zoom = 3)

        assertEquals(0, north.y)
    }
}
