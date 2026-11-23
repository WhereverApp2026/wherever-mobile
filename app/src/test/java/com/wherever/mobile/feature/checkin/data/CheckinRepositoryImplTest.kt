package com.wherever.mobile.feature.checkin.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.network.RedactedLoggingInterceptor
import com.wherever.mobile.testing.testRetrofit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** nv-30 (datos y seguridad): cuerpo por ubicación o código, 422 neutro y ninguna coordenada en el log. */
class CheckinRepositoryImplTest {
    private val server = MockWebServer()
    private val logs = mutableListOf<String>()
    private lateinit var repository: CheckinRepositoryImpl
    private val point = GeoPoint(19.432607, -99.133209)

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().addInterceptor(RedactedLoggingInterceptor { logs += it }).build()
        repository = CheckinRepositoryImpl(testRetrofit(server.url("/"), client).create(CheckinApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    private fun enqueue(code: Int, body: String) = server.enqueue(MockResponse.Builder().code(code).body(body).build())

    private val ok = """{"id":"c1","negocio_id":"n3","metodo":"ubicacion","valido":true,"distancia_m":40,
        "dia":"2026-10-18","resena_id":null,"creado_en":"2026-10-18T23:02:00Z"}"""

    @Test
    fun `por ubicacion manda lat, lng y plan`() = runTest {
        enqueue(201, ok)

        val checkin = (repository.checkIn("n3", "p1", point, null) as Result.Success).data

        val body = server.takeRequest(5, TimeUnit.SECONDS)!!.body!!.utf8()
        assertTrue(body.contains("\"metodo\":\"ubicacion\""))
        assertTrue(body.contains("\"lat\":19.432607"))
        assertTrue(body.contains("\"plan_id\":\"p1\""))
        assertFalse(body.contains("codigo"))
        assertTrue(checkin.valid)
    }

    @Test
    fun `con codigo no manda coordenadas`() = runTest {
        enqueue(201, ok)

        repository.checkIn("n3", "", point, "WH-ABC123")

        val body = server.takeRequest(5, TimeUnit.SECONDS)!!.body!!.utf8()
        assertTrue(body.contains("\"metodo\":\"qr_negocio\""))
        assertTrue(body.contains("\"codigo\":\"WH-ABC123\""))
        assertFalse(body.contains("lat"))
        assertFalse(body.contains("plan_id"))
    }

    @Test
    fun `fuera de radio es 422 con el mensaje neutro y el segundo del dia es 409`() = runTest {
        enqueue(
            422,
            """{"codigo":"fuera_de_radio","mensaje":"parece que aún no llegas. ¿Escaneas el código?",
            "campos":[],"traza_id":"t"}"""
        )
        enqueue(409, """{"codigo":"ya_registrado","mensaje":"ya registrado","campos":[],"traza_id":"t"}""")

        val far = repository.checkIn("n3", null, point, null) as Result.Failure
        val again = repository.checkIn("n3", null, point, null) as Result.Failure

        assertTrue(far.error is AppError.Validation)
        assertEquals("parece que aún no llegas. ¿Escaneas el código?", far.error.apiMessage)
        assertTrue(again.error is AppError.Conflict)
    }

    @Test
    fun `la ubicacion no sale en el log`() = runTest {
        enqueue(201, ok)

        repository.checkIn("n3", "p1", point, null)

        server.takeRequest(5, TimeUnit.SECONDS)
        assertTrue(logs.isNotEmpty())
        logs.forEach { line ->
            assertFalse(line.contains("19.43"))
            assertFalse(line.contains("99.13"))
        }
    }
}
