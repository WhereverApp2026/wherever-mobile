package com.wherever.mobile.core.network

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.testing.testRetrofit
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.http.GET

private interface ProbeApi {
    @GET("probe")
    suspend fun probe(): RefreshRequestDto
}

class SafeApiCallTest {
    private val server = MockWebServer()
    private lateinit var api: ProbeApi

    @Before
    fun setUp() {
        server.start()
        api = testRetrofit(server.url("/")).create(ProbeApi::class.java)
    }

    @After
    fun tearDown() = server.close()

    private fun error(code: String, status: Int, fields: String = "[]", header: Pair<String, String>? = null) {
        val builder = MockResponse.Builder()
            .code(status)
            .body("""{"codigo":"$code","mensaje":"mensaje api","campos":$fields,"traza_id":"t1"}""")
        header?.let { builder.addHeader(it.first, it.second) }
        server.enqueue(builder.build())
    }

    private suspend fun failure(): AppError = (safeApiCall { api.probe() } as Result.Failure).error

    @Test
    fun `cada codigo de la API se mapea a su AppError con mensaje y traza`() = runTest {
        val cases = listOf(
            Triple("no_autenticado", 401, AppError.Unauthorized("mensaje api", "t1")),
            Triple("prohibido", 403, AppError.Forbidden("mensaje api", "t1")),
            Triple("no_encontrado", 404, AppError.NotFound("mensaje api", "t1")),
            Triple("conflicto", 409, AppError.Conflict("mensaje api", "t1")),
            Triple("vencido", 410, AppError.Gone("mensaje api", "t1")),
            Triple("bloqueado", 423, AppError.Locked("mensaje api", "t1")),
            Triple("no_disponible", 503, AppError.Server("mensaje api", "t1")),
            Triple("solicitud_invalida", 400, AppError.Unknown("mensaje api", "t1"))
        )
        cases.forEach { (code, status, expected) ->
            error(code, status)
            assertEquals(expected, failure())
        }
    }

    @Test
    fun `validacion conserva los campos`() = runTest {
        error("validacion", 422, """[{"campo":"correo","mensaje":"no es un correo válido"}]""")
        val failure = failure() as AppError.Validation
        assertEquals(listOf(FieldError("correo", "no es un correo válido")), failure.fields)
    }

    @Test
    fun `429 lee Retry-After`() = runTest {
        error("demasiadas_peticiones", 429, header = "Retry-After" to "45")
        assertEquals(45L, (failure() as AppError.RateLimited).retryAfterSeconds)
    }

    @Test
    fun `429 sin Retry-After deja el valor en null`() = runTest {
        error("demasiadas_peticiones", 429)
        assertEquals(null, (failure() as AppError.RateLimited).retryAfterSeconds)
    }

    @Test
    fun `cuerpo ilegible en 500 es Server y en 400 es Unknown`() = runTest {
        server.enqueue(MockResponse.Builder().code(500).body("<html>").build())
        assertEquals(AppError.Server(), failure())
        server.enqueue(MockResponse.Builder().code(400).body("<html>").build())
        assertEquals(AppError.Unknown(), failure())
    }

    @Test
    fun `sin cuerpo se usa el estado HTTP`() = runTest {
        server.enqueue(MockResponse.Builder().code(423).build())
        assertTrue(failure() is AppError.Locked)
    }

    @Test
    fun `respuesta 200 ilegible es Unknown`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body("""{"otro":1}""").build())
        assertTrue(failure() is AppError.Unknown)
    }

    @Test
    fun `excepciones de red se distinguen`() = runTest {
        assertEquals(
            Result.Failure(AppError.Network),
            safeApiCall<Unit> { throw UnknownHostException() }
        )
        assertEquals(
            Result.Failure(AppError.Timeout),
            safeApiCall<Unit> { throw SocketTimeoutException() }
        )
    }
}
