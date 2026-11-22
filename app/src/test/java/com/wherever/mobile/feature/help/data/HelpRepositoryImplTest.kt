package com.wherever.mobile.feature.help.data

import android.content.Context
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.ApiJson
import com.wherever.mobile.testing.testRetrofit
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val VERSION = "0.7.0"

/** nv-33 (datos): `POST /errores` con MockWebServer real. */
class HelpRepositoryImplTest {
    private val server = MockWebServer()
    private val context = mockk<Context> {
        every { getString(any()) } answers { "texto ${firstArg<Int>()}" }
    }
    private lateinit var repository: HelpRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        repository = HelpRepositoryImpl(
            testRetrofit(server.url("/")).create(HelpApi::class.java),
            context,
            VERSION
        )
    }

    @After
    fun tearDown() = server.close()

    private fun respond(code: Int, body: String = "") {
        server.enqueue(MockResponse.Builder().code(code).body(body).build())
    }

    private fun sentBody(): JsonObject {
        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("POST", request.method)
        assertEquals("/errores", request.url.encodedPath)
        return ApiJson.decodeFromString(JsonObject.serializer(), request.body!!.utf8())
    }

    @Test
    fun `manda modulo, mensaje y version y acepta el 202 sin cuerpo`() = runTest {
        respond(202)

        val result = repository.reportProblem("  El horario del lugar está mal  ")

        assertTrue(result is Result.Success)
        val body = sentBody()
        assertEquals("reporte_usuario", body["modulo"]!!.jsonPrimitive.content)
        assertEquals("El horario del lugar está mal", body["mensaje"]!!.jsonPrimitive.content)
        assertEquals(VERSION, body["version"]!!.jsonPrimitive.content)
    }

    @Test
    fun `sin traza no manda el campo traza_id`() = runTest {
        respond(202)

        repository.reportProblem("Algo falló")

        assertTrue("traza_id" !in sentBody())
    }

    @Test
    fun `un 422 llega como Validation con el campo`() = runTest {
        respond(
            422,
            """
            {"codigo":"validacion","mensaje":"datos invalidos",
            "campos":[{"campo":"mensaje","mensaje":"vacio"}],"traza_id":"t1"}
            """.trimIndent()
        )

        val error = (repository.reportProblem("x") as Result.Failure).error

        assertTrue(error is AppError.Validation)
        assertEquals("mensaje", (error as AppError.Validation).fields.single().field)
        assertEquals("t1", error.traceId)
    }

    @Test
    fun `un 429 llega como RateLimited con Retry-After`() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .code(429)
                .addHeader("Retry-After", "30")
                .body("""{"codigo":"demasiadas_peticiones","mensaje":"espera","campos":[]}""")
                .build()
        )

        val error = (repository.reportProblem("x") as Result.Failure).error

        assertEquals(30L, (error as AppError.RateLimited).retryAfterSeconds)
    }

    @Test
    fun `las preguntas salen de strings en el orden de la maqueta`() {
        val faqs = repository.faqs()

        assertEquals(9, faqs.size)
        assertEquals((0..8).toList(), faqs.map { it.id })
        assertTrue(faqs.all { it.question.startsWith("texto ") && it.answer.startsWith("texto ") })
    }
}
