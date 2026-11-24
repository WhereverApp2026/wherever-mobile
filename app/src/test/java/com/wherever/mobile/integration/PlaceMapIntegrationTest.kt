package com.wherever.mobile.integration

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.places.data.PlaceMapApi
import com.wherever.mobile.feature.places.data.PlaceMapRepositoryImpl
import com.wherever.mobile.feature.places.domain.MapArea
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.RealNetwork
import com.wherever.mobile.testing.reply
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

/** 32_ExpMapa: `GET /lugares/mapa` (nv-21) con pines sueltos o celdas agrupadas. */
class PlaceMapIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private lateinit var repository: PlaceMapRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val tokens = FakeTokenStore(SessionTokens("acceso", "refresh", "2026-11-04T15:15:00Z"))
        repository = PlaceMapRepositoryImpl(RealNetwork(server, tokens).retrofit.create(PlaceMapApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `pines de la zona con su categoria y filtro`() = runTest {
        server.reply(
            200,
            """{"agrupado":false,"pines":[{"id":"n1","nombre":"Café Tacuba","categoria":"cafeteria",""" +
                """"calificacion":4.6,"ubicacion":{"lat":19.4336,"lng":-99.1374}}],"celdas":[]}"""
        )
        val map = (repository.area(MapArea.of(19.40, -99.16, 19.46, -99.10, 15.0), "cafeteria") as Result.Success).data

        val request = server.takeRequest()
        assertEquals("/api/v1/lugares/mapa", request.url.encodedPath)
        assertEquals("15", request.url.queryParameter("zoom"))
        assertEquals("cafeteria", request.url.queryParameter("categoria"))
        assertEquals("19.4", request.url.queryParameter("min_lat"))
        assertEquals(19.4336, map.pins.single().lat, 0.0)
        assertEquals("Café Tacuba", map.pins.single().name)
    }

    @Test
    fun `con zoom bajo llegan celdas y sin filtro no se manda categoria`() = runTest {
        server.reply(200, """{"agrupado":true,"pines":[],"celdas":[{"centro":{"lat":19.4,"lng":-99.1},"conteo":37}]}""")
        val map = (repository.area(MapArea.of(18.0, -100.0, 21.0, -98.0, 8.0)) as Result.Success).data

        assertNull(server.takeRequest().url.queryParameter("categoria"))
        assertTrue(map.grouped)
        assertEquals(37, map.cells.single().count)
    }
}
