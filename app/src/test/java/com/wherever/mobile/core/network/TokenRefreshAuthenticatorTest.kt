package com.wherever.mobile.core.network

import app.cash.turbine.test
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.testRetrofit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.test.runTest
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TokenRefreshAuthenticatorTest {
    private val server = MockWebServer()
    private val tokenStore = FakeTokenStore(SessionTokens("viejo", "refresh-viejo", "2026-11-04T15:15:00Z"))
    private val events = SessionEvents()
    private lateinit var client: OkHttpClient

    private val refreshed = """
        {"access_token":"nuevo","refresh_token":"refresh-nuevo","expira_en":"2026-11-04T15:30:00Z",
        "usuario":{"id":"u1","nombre":"Ana","correo":"ana@ejemplo.com"}}
    """.trimIndent()

    @Before
    fun setUp() {
        server.start()
        val refreshApi = testRetrofit(server.url("/api/v1/")).create(RefreshApi::class.java)
        client = OkHttpClient.Builder()
            .addInterceptor(AuthHeaderInterceptor(tokenStore))
            .authenticator(TokenRefreshAuthenticator(tokenStore, refreshApi, events))
            .build()
    }

    @After
    fun tearDown() = server.close()

    private fun get(): Int {
        val request = Request.Builder().url(server.url("/api/v1/usuarios/yo")).build()
        return client.newCall(request).execute().use { it.code }
    }

    private fun status(code: Int, body: String = "") {
        server.enqueue(MockResponse.Builder().code(code).body(body).build())
    }

    @Test
    fun `401 renueva, guarda el refresh rotado y repite con el token nuevo`() {
        status(401)
        status(200, refreshed)
        status(200)

        assertEquals(200, get())

        val original = server.takeRequest()
        assertEquals("Bearer viejo", original.headers["Authorization"])
        val refresh = server.takeRequest()
        assertEquals("/api/v1/auth/refresh", refresh.url.encodedPath)
        assertEquals("""{"refresh_token":"refresh-viejo"}""", refresh.body?.utf8())
        assertNull(refresh.headers["Authorization"])
        assertEquals("Bearer nuevo", server.takeRequest().headers["Authorization"])
        assertEquals("refresh-nuevo", tokenStore.tokens?.refreshToken)
    }

    @Test
    fun `si el reintento vuelve a dar 401 no renueva otra vez`() {
        status(401)
        status(200, refreshed)
        status(401)

        assertEquals(401, get())
        assertEquals(3, server.requestCount)
    }

    @Test
    fun `refresh rechazado limpia la sesion y avisa`() = runTest {
        events.sessionExpired.test {
            status(401)
            status(401, """{"codigo":"no_autenticado","mensaje":"x","campos":[]}""")

            assertEquals(401, get())

            assertNull(tokenStore.tokens)
            awaitItem()
        }
    }

    @Test
    fun `refresh con 503 conserva la sesion`() {
        status(401)
        status(503)

        assertEquals(401, get())
        assertEquals("refresh-viejo", tokenStore.tokens?.refreshToken)
    }

    @Test
    fun `401 sin Bearer no intenta renovar`() {
        tokenStore.tokens = null
        status(401)

        assertEquals(401, get())
        assertEquals(1, server.requestCount)
    }

    /** Reusar un refresh viejo cierra todas las sesiones: dos 401 simultáneos deben renovar una sola vez. */
    @Test
    fun `dos 401 simultaneos producen un solo refresh`() {
        val refreshCalls = AtomicInteger()
        val bothArrived = CountDownLatch(2)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.url.encodedPath.endsWith("auth/refresh") -> {
                    refreshCalls.incrementAndGet()
                    MockResponse.Builder().code(200).body(refreshed).build()
                }
                request.headers["Authorization"] == "Bearer viejo" -> {
                    bothArrived.countDown()
                    bothArrived.await()
                    MockResponse.Builder().code(401).build()
                }
                else -> MockResponse.Builder().code(200).build()
            }
        }
        val pool = Executors.newFixedThreadPool(2)
        val results = List(2) { pool.submit<Int> { get() } }.map { it.get() }
        pool.shutdown()

        assertEquals(listOf(200, 200), results)
        assertEquals(1, refreshCalls.get())
    }
}
