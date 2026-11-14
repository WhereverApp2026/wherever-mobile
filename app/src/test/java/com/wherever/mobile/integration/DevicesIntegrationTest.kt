package com.wherever.mobile.integration

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.notifications.DeviceRegistrationImpl
import com.wherever.mobile.core.notifications.DevicesApi
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.RealNetwork
import com.wherever.mobile.testing.reply
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val FCM_TOKEN = "fcm-token-de-prueba"

/**
 * Integración del registro del push (specs/nv-46): repositorio real + cadena de red real contra MockWebServer.
 * `PUT` y `DELETE /api/v1/dispositivos` con el token de FCM en el cuerpo, nunca en la ruta.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DevicesIntegrationTest {
    private val server = MockWebServer()
    private val tokenStore = FakeTokenStore()
    private var pushToken: String? = FCM_TOKEN
    private lateinit var network: RealNetwork

    @Before
    fun setUp() {
        server.start()
        network = RealNetwork(server, tokenStore)
    }

    @After
    fun tearDown() = server.close()

    private fun registration(scope: CoroutineScope) = DeviceRegistrationImpl(
        api = network.retrofit.create(DevicesApi::class.java),
        tokenStore = tokenStore,
        tokenSource = { pushToken },
        scope = scope
    )

    private fun TestScope.signedIn() = registration(backgroundScope).also {
        tokenStore.tokens = SessionTokens("access", "refresh", "2026-11-14T15:15:00Z")
    }

    @Test
    fun `PUT manda token y plataforma en el cuerpo con el JWT`() = runTest {
        val registration = signedIn()
        server.reply(204)

        assertEquals(Result.Success(Unit), registration.register())

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/dispositivos", request.url.encodedPath)
        assertNull(request.url.query)
        assertEquals("Bearer access", request.headers["Authorization"])
        assertEquals("""{"token":"$FCM_TOKEN","plataforma":"android"}""", request.body?.utf8())
    }

    @Test
    fun `onNewToken manda el token nuevo igual, en segundo plano`() = runTest {
        val registration = signedIn()
        server.reply(204)

        registration.registerInBackground("token-nuevo")
        advanceUntilIdle()

        val request = server.takeRequest(5, TimeUnit.SECONDS)
        assertEquals("""{"token":"token-nuevo","plataforma":"android"}""", request?.body?.utf8())
    }

    @Test
    fun `DELETE manda el token en el cuerpo`() = runTest {
        val registration = signedIn()
        server.reply(204)

        assertEquals(Result.Success(Unit), registration.unregister())

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/v1/dispositivos", request.url.encodedPath)
        assertEquals("""{"token":"$FCM_TOKEN"}""", request.body?.utf8())
    }

    @Test
    fun `sin sesion o sin token de FCM no llama a la API`() = runTest {
        val registration = registration(backgroundScope)
        assertEquals(Result.Success(Unit), registration.register())
        assertEquals(Result.Success(Unit), registration.unregister())

        tokenStore.tokens = SessionTokens("access", "refresh", "2026-11-14T15:15:00Z")
        pushToken = null
        assertEquals(Result.Success(Unit), registration.register())
        assertEquals(Result.Success(Unit), registration.unregister())

        assertEquals(0, server.requestCount)
    }

    @Test
    fun `422 por el sexto telefono es un fallo que no rompe nada`() = runTest {
        val registration = signedIn()
        server.reply(422, """{"codigo":"validacion","mensaje":"máximo 5 dispositivos","campos":[],"traza_id":"t1"}""")

        assertTrue(registration.register() is Result.Failure)
        assertEquals("access", tokenStore.tokens?.accessToken)
    }

    @Test
    fun `Security - el token de FCM no aparece en los logs`() = runTest {
        val registration = signedIn()
        server.reply(204)
        server.reply(204)

        registration.register()
        registration.unregister()

        assertTrue(network.logs.isNotEmpty())
        assertFalse(network.logs.any { it.contains(FCM_TOKEN) })
    }
}
