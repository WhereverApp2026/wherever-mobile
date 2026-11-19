package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.auth.data.AuthApi
import com.wherever.mobile.feature.auth.data.AuthRepositoryImpl
import com.wherever.mobile.feature.auth.domain.DeviceInfo
import com.wherever.mobile.feature.auth.domain.OnboardingPreferences
import com.wherever.mobile.feature.auth.domain.PostAuthDestination
import com.wherever.mobile.feature.profile.data.ProfileRepositoryImpl
import com.wherever.mobile.feature.profile.data.UsersApi
import com.wherever.mobile.testing.ApiDoc
import com.wherever.mobile.testing.FakeDeviceRegistration
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.FakeUserProfileDao
import com.wherever.mobile.testing.RealNetwork
import com.wherever.mobile.testing.reply
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

/** nv-08 (D11): restablecer contraseña y verificar correo con el token del enlace, contra MockWebServer. */
class EmailLinksIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private val tokenStore = FakeTokenStore()
    private val dao = FakeUserProfileDao()
    private lateinit var repository: AuthRepositoryImpl

    private val onboarding = object : OnboardingPreferences {
        override fun isTastesSkipped(userId: String) = true

        override fun markTastesSkipped(userId: String) = Unit

        override fun clear() = Unit
    }

    @Before
    fun setUp() {
        server.start()
        val network = RealNetwork(server, tokenStore)
        repository = AuthRepositoryImpl(
            api = network.retrofit.create(AuthApi::class.java),
            tokenStore = tokenStore,
            profileRepository = ProfileRepositoryImpl(network.retrofit.create(UsersApi::class.java), dao),
            profileDao = dao,
            onboarding = onboarding,
            deviceInfo = { DeviceInfo("Pixel 8", "Android 15", "0.1.0") },
            deviceRegistration = FakeDeviceRegistration(tokenStore)
        )
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `restablecer 200 manda token y contrasena, guarda la sesion y entra`() = runTest {
        server.reply(200, ApiDoc.response("POST /api/v1/auth/login", 200))
        server.reply(200, ApiDoc.response("GET /api/v1/usuarios/yo", 200), "ETag" to "\"3\"")

        val result = repository.resetPassword("abc123", "Nueva4567")

        assertEquals(Result.Success(PostAuthDestination.HOME), result)
        val request = server.takeRequest()
        assertEquals("/api/v1/auth/restablecer", request.url.encodedPath)
        val body = request.body!!.utf8()
        assertTrue(body.contains("\"token\":\"abc123\""))
        assertTrue(body.contains("\"password_nuevo\":\"Nueva4567\""))
        assertTrue(body.contains("\"dispositivo\""))
        assertEquals(null, request.headers["Authorization"])
        assertTrue(tokenStore.tokens != null)
    }

    @Test
    fun `restablecer con enlace vencido no guarda sesion`() = runTest {
        server.reply(422, ApiDoc.error(422, "enlace inválido o vencido"))

        val error = (repository.resetPassword("viejo", "Nueva4567") as Result.Failure).error

        assertTrue(error is AppError.Validation)
        assertNull(tokenStore.tokens)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `verificar correo manda el token y sin sesion no pide el perfil`() = runTest {
        server.reply(200, """{"correo_verificado":true}""")

        assertEquals(Result.Success(Unit), repository.verifyEmail("tok-1"))

        val request = server.takeRequest()
        assertEquals("/api/v1/auth/verificar-correo", request.url.encodedPath)
        assertEquals("""{"token":"tok-1"}""", request.body?.utf8())
        assertEquals(1, server.requestCount)
    }
}
