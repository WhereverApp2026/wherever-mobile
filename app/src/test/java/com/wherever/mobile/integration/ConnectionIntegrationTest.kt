package com.wherever.mobile.integration

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.RedactedLoggingInterceptor
import com.wherever.mobile.core.network.RefreshRequestDto
import com.wherever.mobile.core.network.RequestIdInterceptor
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.testing.testRetrofit
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.ConnectionSpec
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import retrofit2.http.GET

private interface PingApi {
    @GET("ping")
    suspend fun ping(): RefreshRequestDto
}

/**
 * Integración (specs/nv-40): cadena RequestId → RedactedLogging real contra MockWebServer.
 * Cada falla llega a la UI con su propio mensaje; "Sin conexión" solo cuando no hay red.
 */
class ConnectionIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private val logs = mutableListOf<String>()

    @After
    fun tearDown() = server.close()

    private fun client(builder: OkHttpClient.Builder = OkHttpClient.Builder()): OkHttpClient = builder
        .addInterceptor(RequestIdInterceptor())
        .addInterceptor(RedactedLoggingInterceptor { synchronized(logs) { logs += it } })
        .build()

    private suspend fun call(url: HttpUrl, client: OkHttpClient = client()): AppError {
        val api = testRetrofit(url, client).create(PingApi::class.java)
        return (safeApiCall { api.ping() } as Result.Failure).error
    }

    @Test
    fun `servidor que no responde a tiempo es Timeout`() = runTest {
        server.start()
        server.enqueue(MockResponse.Builder().headersDelay(2, TimeUnit.SECONDS).body("{}").build())
        val slow = client(OkHttpClient.Builder().readTimeout(300, TimeUnit.MILLISECONDS))

        val error = call(server.url("/api/v1/"), slow)

        assertEquals(AppError.Timeout, error)
        assertEquals(UiText.Res(R.string.error_timeout), error.toUiText())
        assertTrue(logs.single(), logs.single().contains("FAILED java.net.SocketTimeoutException"))
    }

    @Test
    fun `503 en HTML del balanceador es Server y no Sin conexion`() = runTest {
        server.start()
        server.enqueue(
            MockResponse.Builder().code(503).addHeader("Content-Type", "text/html")
                .body("<html><body><h1>503 Service Temporarily Unavailable</h1></body></html>").build()
        )

        val error = call(server.url("/api/v1/"))

        assertEquals(AppError.Server(), error)
        assertEquals(UiText.Res(R.string.error_server), error.toUiText())
        assertTrue(logs.single(), logs.single().contains("-> 503"))
    }

    @Test
    fun `error JSON de la API muestra el mensaje de la API`() = runTest {
        server.start()
        server.enqueue(
            MockResponse.Builder().code(422).addHeader("Content-Type", "application/json")
                .body("""{"codigo":"validacion","mensaje":"datos inválidos","campos":[],"traza_id":"t-422"}""")
                .build()
        )

        val error = call(server.url("/api/v1/")) as AppError.Validation

        assertEquals("t-422", error.traceId)
        assertEquals(UiText.Plain("Datos inválidos"), error.toUiText())
    }

    @Test
    fun `200 que no es JSON es InvalidResponse`() = runTest {
        server.start()
        server.enqueue(MockResponse.Builder().code(200).body("<html>portal cautivo</html>").build())

        val error = call(server.url("/api/v1/"))

        assertEquals(AppError.InvalidResponse, error)
        assertEquals(UiText.Res(R.string.error_invalid_response), error.toUiText())
    }

    @Test
    fun `conexion rechazada es Sin conexion`() = runTest {
        server.start()
        val url = server.url("/api/v1/")
        server.close()

        val error = call(url)

        assertEquals(AppError.Network, error)
        assertEquals(UiText.Res(R.string.error_network), error.toUiText())
        assertTrue(logs.single(), logs.single().contains("FAILED java.net.ConnectException"))
    }

    @Test
    fun `certificado no confiable es SecureConnection y no Sin conexion`() = runTest {
        val localhost = InetAddress.getByName("localhost").canonicalHostName
        val certificate = HeldCertificate.Builder().addSubjectAlternativeName(localhost).build()
        server.useHttps(HandshakeCertificates.Builder().heldCertificate(certificate).build().sslSocketFactory())
        server.start()
        server.enqueue(MockResponse.Builder().body("{}").build())

        // Cliente con solo las CA del sistema, como `network_security_config`: el certificado propio no pasa.
        val error = call(server.url("/api/v1/"))

        assertEquals(logs.toString(), AppError.SecureConnection, error)
        assertEquals(UiText.Res(R.string.error_secure_connection), error.toUiText())
        assertTrue(logs.single(), logs.single().contains("javax.net.ssl.SSL"))
    }

    @Test
    fun `trafico en claro bloqueado es SecureConnection`() = runTest {
        server.start()
        val httpsOnly = client(OkHttpClient.Builder().connectionSpecs(listOf(ConnectionSpec.MODERN_TLS)))

        val error = call(server.url("/api/v1/"), httpsOnly)

        assertEquals(AppError.SecureConnection, error)
        assertTrue(logs.single(), logs.single().contains("FAILED java.net.UnknownServiceException"))
    }
}
