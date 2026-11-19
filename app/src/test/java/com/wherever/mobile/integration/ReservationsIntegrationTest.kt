package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.reservations.data.ReservationsApi
import com.wherever.mobile.feature.reservations.data.ReservationsRepositoryImpl
import com.wherever.mobile.feature.reservations.domain.NewReservation
import com.wherever.mobile.feature.reservations.domain.ReservationStatus
import com.wherever.mobile.testing.ApiDoc
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.RealNetwork
import com.wherever.mobile.testing.reply
import java.time.LocalDate
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

/** nv-54 (Data): reservas con la cadena de red real (wherever-backend `docs/endpoints.md`, nv-59). */
class ReservationsIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private lateinit var repository: ReservationsRepositoryImpl

    private val reservationJson =
        """{"id":"r1","codigo":"K7M2P9","negocio_id":"p1","fecha_hora":"2026-11-20T01:00:00Z","personas":2,""" +
            """"estado":"bloqueada","bloqueo_expira_en":"2026-11-19T18:10:00Z",""" +
            """"anticipo":{"monto_centavos":20000,"tipo":"fijo"},""" +
            """"politica_cancelacion":{"tipo":"moderada","horas_limite":24,"porcentaje_antes":100,""" +
            """"porcentaje_despues":50},"reembolso_centavos":0,"puede_cambiar":true,"puede_cancelar":true,""" +
            """"version":3,"creado_en":"2026-11-19T18:00:00Z","mensajes":[]}"""

    @Before
    fun setUp() {
        server.start()
        val network = RealNetwork(server, FakeTokenStore(SessionTokens("vigente", "r", "2026-11-19T15:00:00Z")))
        repository = ReservationsRepositoryImpl(network.retrofit.create(ReservationsApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `disponibilidad con fecha y personas`() = runTest {
        server.reply(
            200,
            """{"fecha":"2026-11-19","horarios":[{"hora":"2026-11-20T01:00:00Z","disponible":true,""" +
                """"lugares_libres":6},{"hora":"2026-11-20T02:00:00Z","disponible":false,"lugares_libres":0}]}"""
        )

        val slots = (repository.availability("p1", LocalDate.of(2026, 11, 19), 4) as Result.Success).data

        val request = server.takeRequest()
        assertEquals("/api/v1/lugares/p1/disponibilidad", request.url.encodedPath)
        assertEquals("2026-11-19", request.url.queryParameter("fecha"))
        assertEquals("4", request.url.queryParameter("personas"))
        assertEquals(listOf(true, false), slots.map { it.available })
    }

    @Test
    fun `crear manda Idempotency-Key y omite la nota vacia`() = runTest {
        server.reply(201, reservationJson)

        val created = repository.create(NewReservation("p1", "2026-11-20T01:00:00Z", 2, "  ", "llave-1"))

        val request = server.takeRequest()
        assertEquals("llave-1", request.headers["Idempotency-Key"])
        assertEquals(
            """{"negocio_id":"p1","fecha_hora":"2026-11-20T01:00:00Z","personas":2}""",
            request.body?.utf8()
        )
        val reservation = (created as Result.Success).data
        assertEquals(ReservationStatus.HELD, reservation.status)
        assertEquals(20_000L, reservation.depositCents)
        assertEquals(24, reservation.policy.hoursLimit)
    }

    @Test
    fun `horario ocupado 409 y correo sin verificar 403`() = runTest {
        server.reply(409, ApiDoc.error(409, "horario ocupado"))
        server.reply(403, ApiDoc.error(403, "correo no verificado"))
        val request = NewReservation("p1", "2026-11-20T01:00:00Z", 2, null, "llave-2")

        assertTrue((repository.create(request) as Result.Failure).error is AppError.Conflict)
        assertTrue((repository.create(request) as Result.Failure).error is AppError.Forbidden)
    }

    @Test
    fun `lista proximas con cursor y limite`() = runTest {
        server.reply(200, """{"items":[$reservationJson],"siguiente_cursor":"","limite":20}""")

        val page = (repository.list(upcoming = true) as Result.Success).data

        val request = server.takeRequest()
        assertEquals("proximas", request.url.queryParameter("cuando"))
        assertEquals("20", request.url.queryParameter("limite"))
        assertNull(page.nextCursor)
        assertEquals("K7M2P9", page.items.single().code)
    }

    @Test
    fun `reembolso, cancelar con motivo y cambiar con If-Match`() = runTest {
        server.reply(200, """{"reembolso_centavos":10000,"porcentaje":50,"politica":{"tipo":"moderada"}}""")
        server.reply(200, reservationJson.replace("bloqueada", "cancelada"))
        server.reply(200, reservationJson)
        server.reply(200, reservationJson.replace("\"version\":3", "\"version\":4"))

        assertEquals(50, (repository.refundQuote("r1") as Result.Success).data.percent)
        repository.cancel("r1", " Me enfermé ")
        val current = (repository.get("r1") as Result.Success).data
        val changed = (repository.changeTime(current, "2026-11-20T02:00:00Z") as Result.Success).data

        assertEquals("/api/v1/reservas/r1/reembolso", server.takeRequest().url.encodedPath)
        val cancel = server.takeRequest()
        assertEquals("/api/v1/reservas/r1/cancelar", cancel.url.encodedPath)
        assertEquals("""{"motivo":"Me enfermé"}""", cancel.body?.utf8())
        server.takeRequest()
        val patch = server.takeRequest()
        assertEquals("PATCH", patch.method)
        assertEquals("\"3\"", patch.headers["If-Match"])
        assertEquals("""{"fecha_hora":"2026-11-20T02:00:00Z"}""", patch.body?.utf8())
        assertEquals(4, changed.version)
    }
}
