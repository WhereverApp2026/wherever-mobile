package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.groups.data.MeetingPointApi
import com.wherever.mobile.feature.groups.data.MeetingPointRepositoryImpl
import com.wherever.mobile.feature.groups.domain.TripRange
import com.wherever.mobile.testing.ApiDoc
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

/** 47_PuntoMedio con la cadena de red real (backend nv-69). */
class MeetingPointIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private lateinit var repository: MeetingPointRepositoryImpl

    private val group = """{"id":"g1","plan_id":"p1","version":2,"mi_rol":"organizador","mi_estado":"confirmado",""" +
        """"integrantes":[{"usuario_id":"u1","persona":{"id":"u1","nombre":"Ana"},"rol":"organizador",""" +
        """"estado":"confirmado","comparte_ubicacion":true}],""" +
        """"punto_encuentro":{"nombre":"Restaurante Colonial","lat":20.057,"lng":-99.337,""" +
        """"hora":"2026-11-29T00:00:00Z","propuesto_por":"u1","negocio_id":"n1"},""" +
        """"actualizado_en":"2026-11-24T15:00:00Z"}"""

    @Before
    fun setUp() {
        server.start()
        val tokens = FakeTokenStore(SessionTokens("acceso", "refresh", "2026-11-04T15:15:00Z"))
        repository =
            MeetingPointRepositoryImpl(RealNetwork(server, tokens).retrofit.create(MeetingPointApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `compartir y dejar de compartir con PATCH integrantes yo sin estado`() = runTest {
        server.reply(200, group)
        server.reply(200, group)

        val shared = (repository.shareLocation("g1", GeoPoint(20.054, -99.341)) as Result.Success).data
        assertTrue(repository.shareLocation("g1", null) is Result.Success)

        val on = server.takeRequest()
        assertEquals("PATCH", on.method)
        assertEquals("/api/v1/grupos/g1/integrantes/yo", on.url.encodedPath)
        assertEquals("""{"ubicacion":{"lat":20.054,"lng":-99.341},"compartir_ubicacion":true}""", on.body?.utf8())
        assertEquals("""{"compartir_ubicacion":false}""", server.takeRequest().body?.utf8())
        assertTrue(shared.members.single().sharesLocation)
        assertEquals("n1", shared.meetingPoint?.placeId)
    }

    @Test
    fun `sugeridos con ubicacion y PUT del punto con negocio y hora`() = runTest {
        server.reply(
            200,
            """{"lugares":[{"id":"n1","nombre":"Restaurante Colonial","categoria":"restaurante",""" +
                """"calificacion":{"promedio":4.6,"total_resenas":3},"ubicacion":{"lat":20.057,"lng":-99.337}},""" +
                """{"id":"n9","nombre":"Sin mapa","categoria":"bar"}],"participantes":2}"""
        )
        server.reply(200, group)

        val suggestion = (repository.suggest("g1") as Result.Success).data
        val updated = (repository.setPoint("g1", "n1", "2026-11-29T00:00:00Z") as Result.Success).data

        assertEquals("/api/v1/grupos/g1/punto-encuentro/sugerido", server.takeRequest().url.encodedPath)
        assertEquals("un lugar sin ubicación no se puede dibujar", listOf("n1"), suggestion.places.map { it.id })
        assertEquals(2, suggestion.participants)
        val put = server.takeRequest()
        assertEquals("PUT", put.method)
        assertEquals("""{"negocio_id":"n1","hora":"2026-11-29T00:00:00Z"}""", put.body?.utf8())
        assertEquals("Restaurante Colonial", updated.meetingPoint?.name)
    }

    @Test
    fun `traslados exactos para mi y rangos para los demas`() = runTest {
        server.reply(
            200,
            """{"traslados":[{"id":"u1","nombre":"Ana","propio":true,"minutos":12,"costo_estimado":25},""" +
                """{"id":"u2","nombre":"Luis","propio":false,"rango":"15_30","costo_rango":"hasta_50"},""" +
                """{"id":"u3","nombre":"Eva","propio":false,"sin_ubicacion":true}]}"""
        )

        val trips = (repository.trips("g1") as Result.Success).data

        assertEquals(12, trips[0].minutes)
        assertEquals(TripRange.FROM_15_TO_30, trips[1].range)
        assertNull(trips[1].minutes)
        assertTrue(trips[2].noLocation)
    }

    @Test
    fun `con menos de dos ubicaciones la API da 422`() = runTest {
        server.reply(422, ApiDoc.error(422, "faltan_ubicaciones"))
        val result = repository.suggest("g1")
        assertTrue((result as Result.Failure).error is AppError.Validation)
    }
}
