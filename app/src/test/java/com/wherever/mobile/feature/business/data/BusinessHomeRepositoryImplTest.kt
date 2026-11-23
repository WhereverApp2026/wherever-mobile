package com.wherever.mobile.feature.business.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.business.domain.BlockReason
import com.wherever.mobile.feature.business.domain.ReservationStatus
import com.wherever.mobile.testing.testRetrofit
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** nv-35 (datos): parámetros de reservas, escanear, cambio de estado, bloqueos y PDF completo. */
class BusinessHomeRepositoryImplTest {
    private val server = MockWebServer()
    private val saved = mutableMapOf<String, ByteArray>()
    private val saver = ReportSaver { name, input ->
        saved[name] = ByteArrayOutputStream().also { input.copyTo(it) }.toByteArray()
        true
    }
    private val dispatchers = object : DispatcherProvider {
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
    }
    private lateinit var repository: BusinessHomeRepositoryImpl

    private val reservationJson = """
        {"id":"r1","codigo":"WH-4821","fecha_hora":"2026-10-17T23:00:00Z","cliente":"Mariana G.",
        "personas":2,"anticipo":7400,"estado":"confirmada","version":3}
    """.trimIndent()

    @Before
    fun setUp() {
        server.start()
        repository = BusinessHomeRepositoryImpl(
            testRetrofit(server.url("/")).create(BusinessHomeApi::class.java),
            saver,
            dispatchers
        )
    }

    @After
    fun tearDown() = server.close()

    private fun enqueue(code: Int, body: String) = server.enqueue(MockResponse.Builder().code(code).body(body).build())

    @Test
    fun `reservas manda fecha, estado y q y convierte la hora a la Ciudad de Mexico`() = runTest {
        enqueue(200, """{"fecha":"2026-10-17","items":[$reservationJson]}""")

        val result = repository.reservations("n1", LocalDate.of(2026, 10, 17), ReservationStatus.PENDING, " wh-48 ")

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("/negocios/n1/reservas", request.url.encodedPath)
        assertEquals("2026-10-17", request.url.queryParameter("fecha"))
        assertEquals("pendiente", request.url.queryParameter("estado"))
        assertEquals("wh-48", request.url.queryParameter("q"))
        val item = (result as Result.Success).data.items.single()
        assertEquals(17, item.dateTime.hour)
        assertEquals(ReservationStatus.CONFIRMED, item.status)
        assertEquals(7400L, item.deposit)
    }

    @Test
    fun `reservas sin filtros no manda parametros vacios`() = runTest {
        enqueue(200, """{"fecha":"2026-10-17","items":[]}""")

        repository.reservations("n1", null)

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals(null, request.url.query)
    }

    @Test
    fun `escanear manda el codigo y 409 es conflicto`() = runTest {
        enqueue(200, reservationJson.replace("confirmada", "asistio"))
        enqueue(409, """{"codigo":"codigo_ya_usado","mensaje":"código ya usado","campos":[],"traza_id":"t"}""")

        val ok = repository.checkIn("n1", " wh-4821 ")
        val used = repository.checkIn("n1", "WH-4821")

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("/negocios/n1/reservas/escanear", request.url.encodedPath)
        assertEquals("""{"codigo":"WH-4821"}""", request.body!!.utf8())
        assertEquals(ReservationStatus.ARRIVED, (ok as Result.Success).data.status)
        assertTrue((used as Result.Failure).error is AppError.Conflict)
    }

    @Test
    fun `cambiar el estado usa PATCH con estado`() = runTest {
        enqueue(200, reservationJson.replace("confirmada", "asistio"))

        repository.updateReservation("n1", "r1", ReservationStatus.ARRIVED)

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("PATCH", request.method)
        assertEquals("/negocios/n1/reservas/r1", request.url.encodedPath)
        assertEquals("""{"estado":"asistio"}""", request.body!!.utf8())
    }

    @Test
    fun `bloqueo manda hora local con desfase y su motivo`() = runTest {
        enqueue(
            201,
            """{"id":"b1","desde":"2026-10-17T18:00:00-06:00","hasta":"2026-10-17T20:00:00-06:00","motivo":"lleno"}"""
        )

        val result = repository.block(
            "n1",
            LocalDate.of(2026, 10, 17).atTime(18, 0),
            LocalDate.of(2026, 10, 17).atTime(20, 0),
            BlockReason.FULL
        )

        val body = server.takeRequest(5, TimeUnit.SECONDS)!!.body!!.utf8()
        assertTrue(body.contains(""""desde":"2026-10-17T18:00-06:00""""))
        assertTrue(body.contains(""""motivo":"lleno""""))
        assertEquals(BlockReason.FULL, (result as Result.Success).data.reason)
    }

    @Test
    fun `el PDF del reporte se guarda completo`() = runTest {
        val pdf = ByteArray(200_000) { (it % 251).toByte() }
        server.enqueue(
            MockResponse.Builder().code(
                200
            ).addHeader("Content-Type", "application/pdf").body(Buffer().write(pdf)).build()
        )

        val result = repository.downloadMonthlyReport("n1", YearMonth.of(2026, 10))

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("2026-10", request.url.queryParameter("mes"))
        assertEquals("wherever-2026-10.pdf", (result as Result.Success).data)
        assertArrayEquals(pdf, saved["wherever-2026-10.pdf"])
    }

    @Test
    fun `reporte fuera de rango es validacion y no guarda nada`() = runTest {
        enqueue(422, """{"codigo":"validacion","mensaje":"mes inválido","campos":[],"traza_id":"t"}""")

        val result = repository.downloadMonthlyReport("n1", YearMonth.of(2020, 1))

        assertTrue((result as Result.Failure).error is AppError.Validation)
        assertTrue(saved.isEmpty())
    }

    @Test
    fun `panel con datos nulos y modo negocio`() = runTest {
        enqueue(
            200,
            """{"negocio_id":"n1","nombre":"Café Raíz","estado":"publicado",
            "reservas_hoy":null,"sello_excelencia":true}"""
        )
        enqueue(403, """{"codigo":"sin_rol","mensaje":"sin rol","campos":[],"traza_id":"t"}""")

        val panel = repository.panel("n1")
        val mode = repository.enterBusinessMode()

        assertEquals(null, (panel as Result.Success).data.reservationsToday)
        assertTrue(panel.data.seal)
        server.takeRequest(5, TimeUnit.SECONDS)
        assertEquals("""{"modo":"negocio"}""", server.takeRequest(5, TimeUnit.SECONDS)!!.body!!.utf8())
        assertTrue((mode as Result.Failure).error is AppError.Forbidden)
    }

    @Test
    fun `estadisticas manda el rango y lee origen premium`() = runTest {
        enqueue(
            200,
            """{"desde":"2026-10-10","hasta":"2026-10-16","agrupar":"dia",
            "serie":[{"inicio":"2026-10-10","reservas":4,"llegadas":3}],
            "totales":{"reservas":4,"llegadas":3},"anterior":{},"cambio":{"reservas":18.0},"frase":"Vas bien",
            "avanzadas":{"por_hora":[],"origen":{"busqueda":2,"mapa":1,"inicio":0,"plan_ia":5,"cupon":0},"aspectos":[]},
            "premium_requerido":false}"""
        )

        val result = repository.stats("n1", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 16), "dia")

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("2026-10-10", request.url.queryParameter("desde"))
        assertEquals("dia", request.url.queryParameter("agrupar"))
        val stats = (result as Result.Success).data
        assertEquals(5, stats.origin?.aiPlan)
        assertEquals(75, stats.attendance)
    }
}
