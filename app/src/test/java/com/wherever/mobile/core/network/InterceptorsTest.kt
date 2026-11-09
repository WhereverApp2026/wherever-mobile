package com.wherever.mobile.core.network

import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.testRetrofit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.http.GET

private interface MixedApi {
    @GET("privada")
    suspend fun privada()

    @PublicEndpoint
    @GET("publica")
    suspend fun publica()
}

class InterceptorsTest {
    private val server = MockWebServer()
    private val tokenStore = FakeTokenStore(SessionTokens("acceso", "refresh", "2026-11-04T15:15:00Z"))

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.close()

    private fun mixedApi() = testRetrofit(
        server.url("/"),
        OkHttpClient.Builder().addInterceptor(AuthHeaderInterceptor(tokenStore)).build()
    ).create(MixedApi::class.java)

    @Test
    fun `agrega Bearer en rutas con JWT`() = runTest {
        server.enqueue(MockResponse.Builder().code(204).build())
        mixedApi().privada()
        assertEquals("Bearer acceso", server.takeRequest().headers["Authorization"])
    }

    @Test
    fun `no agrega Bearer en rutas publicas`() = runTest {
        server.enqueue(MockResponse.Builder().code(204).build())
        mixedApi().publica()
        assertNull(server.takeRequest().headers["Authorization"])
    }

    @Test
    fun `sin token no agrega cabecera`() = runTest {
        tokenStore.tokens = null
        server.enqueue(MockResponse.Builder().code(204).build())
        mixedApi().privada()
        assertNull(server.takeRequest().headers["Authorization"])
    }

    @Test
    fun `cada peticion lleva request id`() {
        server.enqueue(MockResponse.Builder().code(200).build())
        val client = OkHttpClient.Builder().addInterceptor(RequestIdInterceptor { "id-fijo" }).build()
        client.newCall(Request.Builder().url(server.url("/x")).build()).execute().close()
        assertEquals("id-fijo", server.takeRequest().headers["X-Request-Id"])
    }

    @Test
    fun `el log no contiene token, parametros ni cuerpo`() {
        server.enqueue(MockResponse.Builder().code(200).body("secreto-de-respuesta").build())
        val lines = mutableListOf<String>()
        val client = OkHttpClient.Builder().addInterceptor(RedactedLoggingInterceptor { lines += it }).build()
        val request = Request.Builder()
            .url(server.url("/usuarios/correo-disponible?correo=ana@ejemplo.com"))
            .header("Authorization", "Bearer token-secreto")
            .build()
        client.newCall(request).execute().close()

        val log = lines.joinToString()
        assertTrue(log.contains("/usuarios/correo-disponible"))
        assertTrue(log.contains("200"))
        assertFalse(log.contains("token-secreto"))
        assertFalse(log.contains("ana@ejemplo.com"))
        assertFalse(log.contains("secreto-de-respuesta"))
    }
}
