package com.wherever.mobile.feature.business.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.RedactedLoggingInterceptor
import com.wherever.mobile.feature.business.domain.BusinessDataChange
import com.wherever.mobile.feature.business.domain.DaySchedule
import com.wherever.mobile.feature.business.domain.TaxData
import com.wherever.mobile.testing.testRetrofit
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** nv-38 (datos y seguridad): perfil, If-Match, cuenta-deposito, liquidaciones, cobros y nada sensible en el log. */
class BusinessProfileRepositoryImplTest {
    private val server = MockWebServer()
    private val logs = mutableListOf<String>()
    private lateinit var repository: BusinessProfileRepositoryImpl

    private val mineJson = """{"items":[{"id":"n1","nombre":"Café Raíz","estado":"aprobado","version":3,
        "telefono":"5512345678","pausado":null,"fotos":[{"id":"f1","url":"https://img/1.jpg"}],
        "horarios":[{"dia":1,"abre":"08:00","cierra":"20:00","cerrado":false}],
        "reservas_config":{"acepta_reservas":true,"capacidad_por_horario":12,"duracion_turno_min":60},
        "cobros":{"estado":"activo"}}]}"""

    private val panelJson = """{"negocio_id":"n1","datos_fiscales_completos":false,
        "cuenta_deposito":{"clabe_ultimos4":"1234","banco":"BBVA","estado":"registrada"}}"""

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().addInterceptor(RedactedLoggingInterceptor { logs += it }).build()
        repository =
            BusinessProfileRepositoryImpl(testRetrofit(server.url("/"), client).create(BusinessProfileApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    private fun enqueue(code: Int, body: String) = server.enqueue(MockResponse.Builder().code(code).body(body).build())

    private fun take() = server.takeRequest(5, TimeUnit.SECONDS)!!

    /** El perfil pide `mios` y el panel a la vez: se responde por ruta. */
    private fun routeProfile(vararg extra: Pair<String, MockResponse>) {
        val routes = mapOf(
            "/negocios/mios" to MockResponse.Builder().code(200).body(mineJson).build(),
            "/negocios/n1/panel" to MockResponse.Builder().code(200).body(panelJson).build()
        ) + extra
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                routes["${request.method} ${request.url.encodedPath}"] ?: routes[request.url.encodedPath]
                    ?: MockResponse.Builder().code(404).build()
        }
    }

    @Test
    fun `perfil junta mios y panel`() = runTest {
        routeProfile()

        val profile = (repository.profile("n1") as Result.Success).data

        assertEquals("Café Raíz", profile.name)
        assertFalse(profile.paused)
        assertEquals(true, profile.acceptsReservations)
        assertTrue(profile.paymentsConnected)
        assertEquals("1234", profile.depositAccount?.lastFour)
        assertEquals(false, profile.taxDataComplete)
        assertTrue(profile.keepsMinPhotos)
    }

    @Test
    fun `negocio ajeno es NotFound`() = runTest {
        routeProfile()

        assertTrue((repository.profile("otro") as Result.Failure).error is AppError.NotFound)
    }

    @Test
    fun `datos con PATCH e If-Match, recibir reservaciones conserva la configuracion`() = runTest {
        routeProfile(
            "PATCH /negocios/n1" to
                MockResponse.Builder().code(200).body("""{"id":"n1","nombre":"Café Raíz","version":4}""").build(),
            "PUT /negocios/n1/reservas-config" to MockResponse.Builder().code(200)
                .body("""{"id":"n1","version":5,"reservas_config":{"acepta_reservas":false}}""").build()
        )
        val profile = (repository.profile("n1") as Result.Success).data
        repeat(2) { take() }

        val updated = (
            repository.updateData(
                profile,
                BusinessDataChange(
                    " Café de especialidad ",
                    "",
                    listOf(DaySchedule(1, "09:00", "18:00", closed = false))
                )
            ) as Result.Success
            ).data
        val patch = take()
        assertEquals("PATCH", patch.method)
        assertEquals("\"3\"", patch.headers["If-Match"])
        val body = patch.body!!.utf8()
        assertTrue(body.contains("\"descripcion\":\"Café de especialidad\""))
        assertFalse(body.contains("telefono"))
        assertTrue(body.contains("\"horarios\":[{\"dia\":1,\"abre\":\"09:00\",\"cierra\":\"18:00\""))
        assertEquals("1234", updated.depositAccount?.lastFour)

        val saved = (repository.setAcceptsReservations(updated, accepts = false) as Result.Success).data
        val put = take()
        assertEquals("\"4\"", put.headers["If-Match"])
        val config = put.body!!.utf8()
        assertTrue(config.contains("\"acepta_reservas\":false"))
        assertTrue(config.contains("\"capacidad_por_horario\":12"))
        assertEquals(false, saved.acceptsReservations)
    }

    @Test
    fun `pausa, eliminar con confirmacion y fotos`() = runTest {
        enqueue(200, """{"id":"n1","estado":"pausado"}""")
        enqueue(204, "")
        enqueue(201, """{"id":"n1","fotos":[{"id":"f1","url":"u1"},{"id":"f2","url":"u2"}]}""")
        enqueue(200, """{"id":"n1","fotos":[{"id":"f2","url":"u2"},{"id":"f1","url":"u1"}]}""")
        enqueue(422, """{"codigo":"minimo_fotos","mensaje":"mínimo 3 fotos","campos":[],"traza_id":"t"}""")

        repository.pause("n1", paused = true, until = LocalDate.of(2026, 12, 1), reason = " Vacaciones ")
        assertEquals("""{"pausado":true,"hasta":"2026-12-01","motivo":"Vacaciones"}""", take().body!!.utf8())

        repository.delete("n1", "Café Raíz")
        val delete = take()
        assertEquals("DELETE", delete.method)
        assertEquals("""{"confirmacion":"Café Raíz"}""", delete.body!!.utf8())

        val photos = (
            repository.addPhoto(
                "n1",
                "img9",
                com.wherever.mobile.feature.business.domain.PhotoType.INTERIOR
            ) as Result.Success
            ).data
        assertEquals("""{"imagen_id":"img9","tipo":"interior"}""", take().body!!.utf8())
        assertEquals(2, photos.size)

        repository.reorderPhotos("n1", listOf("f2", "f1", "f2"))
        assertEquals("""{"fotos_ids":["f2","f1"]}""", take().body!!.utf8())

        val refused = repository.deletePhoto("n1", "f1")
        assertEquals("/negocios/n1/fotos/f1", take().url.encodedPath)
        assertTrue((refused as Result.Failure).error is AppError.Validation)
    }

    @Test
    fun `saldo, liquidaciones con cursor y conectar devuelve url`() = runTest {
        enqueue(200, """{"disponible":342000,"en_espera":124000,"proximo_deposito":"2026-11-30"}""")
        enqueue(
            200,
            """{"items":[{"id":"s1","semana_inicio":"2026-10-12","neto":218000,"estado":"pagada"}],
            "siguiente_cursor":"c2"}"""
        )
        enqueue(200, """{"items":[],"siguiente_cursor":null}""")
        enqueue(200, """{"url":"https://connect.stripe.com/setup/abc"}""")
        enqueue(503, """{"codigo":"sin_stripe","mensaje":"sin llave","campos":[],"traza_id":"t"}""")

        val balance = (repository.balance("n1") as Result.Success).data
        assertEquals(342_000L, balance.available)
        assertEquals(LocalDate.of(2026, 11, 30), balance.nextDeposit)
        take()

        val first = (repository.settlements("n1", cursor = null) as Result.Success).data
        assertNull(take().url.queryParameter("cursor"))
        assertEquals("c2", first.nextCursor)
        assertEquals(LocalDate.of(2026, 10, 12), first.items.single().weekStart)
        repository.settlements("n1", cursor = "c2")
        assertEquals("c2", take().url.queryParameter("cursor"))

        assertEquals("https://connect.stripe.com/setup/abc", (repository.connectPayments("n1") as Result.Success).data)
        assertEquals("POST", take().method)
        assertTrue((repository.connectPayments("n1") as Result.Failure).error is AppError.Server)
    }

    @Test
    fun `cuenta de deposito con 422 clabe_invalida y fiscales, nada sensible en el log`() = runTest {
        enqueue(200, """{"clabe_ultimos4":"7899","banco":"BBVA","titular":"Café Raíz","estado":"sin_verificar"}""")
        enqueue(422, """{"codigo":"clabe_invalida","mensaje":"CLABE inválida","campos":[],"traza_id":"t"}""")
        enqueue(200, "{}")
        enqueue(
            422,
            """{"codigo":"rfc_invalido","mensaje":"RFC inválido","campos":[{"campo":"rfc","mensaje":"rfc_invalido"}],
            "traza_id":"t"}"""
        )

        val account = (repository.saveDepositAccount("n1", "012180001234567899", " Café Raíz ") as Result.Success).data
        assertEquals("7899", account.lastFour)
        assertEquals("""{"clabe":"012180001234567899","titular":"Café Raíz"}""", take().body!!.utf8())
        assertTrue(
            (
                repository.saveDepositAccount(
                    "n1",
                    "012180001234567895",
                    "x"
                ) as Result.Failure
                ).error is AppError.Validation
        )
        take()

        val tax = TaxData(" cra180512k73 ", " Café Raíz S.A. de C.V. ", "601", "G03", "42800")
        repository.saveTaxData("n1", tax)
        val body = take().body!!.utf8()
        assertTrue(body.contains("\"rfc\":\"CRA180512K73\""))
        assertTrue(body.contains("\"cp_fiscal\":\"42800\""))
        val refused = (repository.saveTaxData("n1", tax) as Result.Failure).error
        assertTrue(refused is AppError.Validation)

        val all = logs.joinToString("\n")
        assertFalse(all.contains("012180001234567899"))
        assertFalse(all.contains("012180001234567895"))
        assertFalse(all.contains("CRA180512K73"))
    }

    @Test
    fun `volver a explorar manda el modo`() = runTest {
        enqueue(200, """{"modo":"explorar"}""")

        assertTrue(repository.exploreMode() is Result.Success)
        val request = take()
        assertEquals("/usuarios/yo/modo", request.url.encodedPath)
        assertEquals("""{"modo":"explorar"}""", request.body!!.utf8())
    }
}
