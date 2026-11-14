package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.auth.data.AuthApi
import com.wherever.mobile.feature.auth.data.AuthRepositoryImpl
import com.wherever.mobile.feature.auth.domain.DeviceInfo
import com.wherever.mobile.feature.auth.domain.OnboardingPreferences
import com.wherever.mobile.feature.auth.domain.PostAuthDestination
import com.wherever.mobile.feature.auth.domain.Registration
import com.wherever.mobile.feature.profile.data.ProfileRepositoryImpl
import com.wherever.mobile.feature.profile.data.UsersApi
import com.wherever.mobile.testing.ApiDoc
import com.wherever.mobile.testing.FakeDeviceRegistration
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.FakeUserProfileDao
import com.wherever.mobile.testing.RealNetwork
import com.wherever.mobile.testing.TEST_PASSWORD
import com.wherever.mobile.testing.reply
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

private class MemoryOnboarding : OnboardingPreferences {
    var cleared = false

    override fun isTastesSkipped(userId: String) = false

    override fun markTastesSkipped(userId: String) = Unit

    override fun clear() {
        cleared = true
    }
}

/**
 * Integración: repositorio real + Retrofit + cadena de red real (`RealNetwork`) contra MockWebServer,
 * con las respuestas exactas de `docs/contexto_api_sprint1.md`. Specs nv-07, nv-08, nv-09, nv-12 y nv-13.
 */
class AuthIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private val tokenStore = FakeTokenStore()
    private val dao = FakeUserProfileDao()
    private val onboarding = MemoryOnboarding()
    private val devices = FakeDeviceRegistration(tokenStore)
    private lateinit var network: RealNetwork
    private lateinit var repository: AuthRepositoryImpl

    private val login = "POST /api/v1/auth/login"
    private val me = "GET /api/v1/usuarios/yo"

    @Before
    fun setUp() {
        server.start()
        network = RealNetwork(server, tokenStore)
        repository = AuthRepositoryImpl(
            api = network.retrofit.create(AuthApi::class.java),
            tokenStore = tokenStore,
            profileRepository = ProfileRepositoryImpl(network.retrofit.create(UsersApi::class.java), dao),
            profileDao = dao,
            onboarding = onboarding,
            deviceInfo = { DeviceInfo("Pixel 8", "Android 15", "0.1.0") },
            deviceRegistration = devices
        )
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `login 200 de la doc guarda los tokens, pide el perfil y va a Inicio`() = runTest {
        server.reply(200, ApiDoc.response(login, 200))
        server.reply(200, ApiDoc.response(me, 200), "ETag" to "\"3\"")

        val result = repository.login("ana@ejemplo.com", TEST_PASSWORD)

        assertEquals(Result.Success(PostAuthDestination.HOME), result)
        assertEquals("q8Zs3v1nX0r4bE7yTk2mW9pLhA6cUfGd", tokenStore.tokens?.refreshToken)
        assertEquals("/api/v1/auth/login", server.takeRequest().url.encodedPath)
        val profileRequest = server.takeRequest()
        assertTrue(profileRequest.headers["Authorization"]!!.startsWith("Bearer eyJ"))
        assertEquals(3L, dao.state.value?.version)
    }

    @Test
    fun `login 401 no intenta refresh ni guarda tokens`() = runTest {
        server.reply(401, ApiDoc.error(401, "correo o contraseña incorrectos"))

        val result = repository.login("ana@ejemplo.com", "Mala2026") as Result.Failure

        assertEquals(AppError.Unauthorized::class, result.error::class)
        assertEquals("correo o contraseña incorrectos", result.error.apiMessage)
        assertNull(tokenStore.tokens)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `login 422 conserva los campos y 423 llega como bloqueo`() = runTest {
        server.reply(422, ApiDoc.error(422, "datos inválidos", mapOf("correo" to "no es un correo válido")))
        server.reply(423, ApiDoc.error(423, "cuenta bloqueada 15 minutos"))

        val validation = (repository.login("ana", TEST_PASSWORD) as Result.Failure).error as AppError.Validation
        val locked = (repository.login("ana@ejemplo.com", TEST_PASSWORD) as Result.Failure).error

        assertEquals("correo", validation.fields.single().field)
        assertEquals("f6bd9485da30dcf0fbcd3eb34cfe4915", validation.traceId)
        assertTrue(locked is AppError.Locked)
    }

    @Test
    fun `login 429 lee Retry-After`() = runTest {
        server.reply(429, ApiDoc.error(429, "demasiadas peticiones"), "Retry-After" to "60")

        val error = (repository.login("ana@ejemplo.com", TEST_PASSWORD) as Result.Failure).error

        assertEquals(60L, (error as AppError.RateLimited).retryAfterSeconds)
    }

    @Test
    fun `registro 201 de la doc deja sesion y 409 llega como conflicto`() = runTest {
        val registration = Registration("Ana López", "1999-04-12", "ana@ejemplo.com", TEST_PASSWORD, true)
        server.reply(201, ApiDoc.response("POST /api/v1/usuarios", 201))
        server.reply(200, ApiDoc.response(me, 200))
        server.reply(409, ApiDoc.error(409, "el correo ya está registrado"))

        assertEquals(Result.Success(Unit), repository.register(registration))
        assertEquals("q8Zs3v1nX0r4bE7yTk2mW9pLhA6cUfGd", tokenStore.tokens?.refreshToken)
        val conflict = (repository.register(registration) as Result.Failure).error

        assertTrue(conflict is AppError.Conflict)
    }

    @Test
    fun `registro 422 en fecha_nacimiento llega como error de ese campo`() = runTest {
        val registration = Registration("Ana López", "2027-01-01", "ana@ejemplo.com", TEST_PASSWORD, true)
        server.reply(
            422,
            ApiDoc.error(422, "datos inválidos", mapOf("fecha_nacimiento" to "debes ser mayor de 18 años"))
        )

        val error = (repository.register(registration) as Result.Failure).error

        assertTrue(error is AppError.Validation)
        val field = (error as AppError.Validation).fields.single()
        assertEquals("fecha_nacimiento", field.field)
        assertEquals("debes ser mayor de 18 años", field.message)
        assertTrue(server.takeRequest().body!!.utf8().contains("\"fecha_nacimiento\":\"2027-01-01\""))
        assertNull(tokenStore.tokens)
    }

    @Test
    fun `recuperar 202 sin cuerpo y 429 con Retry-After`() = runTest {
        server.reply(202)
        server.reply(429, ApiDoc.error(429, "espera antes de reenviar"), "Retry-After" to "45")

        assertEquals(Result.Success(Unit), repository.recoverPassword("ana@ejemplo.com"))
        val limited = (repository.recoverPassword("ana@ejemplo.com") as Result.Failure).error

        assertEquals(45L, (limited as AppError.RateLimited).retryAfterSeconds)
    }

    @Test
    fun `401 en ruta con JWT renueva una vez, guarda el refresh rotado y repite`() = runTest {
        tokenStore.tokens = SessionTokens("viejo", "refresh-viejo", "2026-11-04T15:00:00Z")
        server.reply(401, ApiDoc.error(401, "token vencido"))
        server.reply(200, ApiDoc.response("POST /api/v1/auth/refresh", 200))
        server.reply(200, ApiDoc.response("GET /api/v1/auth/sesiones", 200))

        val sessions = (repository.sessions() as Result.Success).data

        assertEquals(2, sessions.size)
        assertTrue(sessions.first().isCurrent)
        assertEquals("Bearer viejo", server.takeRequest().headers["Authorization"])
        val refresh = server.takeRequest()
        assertEquals("/api/v1/auth/refresh", refresh.url.encodedPath)
        assertTrue(refresh.body!!.utf8().contains("refresh-viejo"))
        assertTrue(server.takeRequest().headers["Authorization"]!!.startsWith("Bearer eyJ"))
        assertEquals("q8Zs3v1nX0r4bE7yTk2mW9pLhA6cUfGd", tokenStore.tokens?.refreshToken)
    }

    @Test
    fun `refresh rechazado limpia la sesion y la peticion falla con 401`() = runTest {
        tokenStore.tokens = SessionTokens("viejo", "refresh-reusado", "2026-11-04T15:00:00Z")
        server.reply(401, ApiDoc.error(401, "token vencido"))
        server.reply(401, ApiDoc.error(401, "refresh inválido"))

        val error = (repository.sessions() as Result.Failure).error

        assertTrue(error is AppError.Unauthorized)
        assertNull(tokenStore.tokens)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `logout 204 limpia tokens, Room y onboarding`() = runTest {
        tokenStore.tokens = SessionTokens("a", "r", "2026-11-04T15:15:00Z")
        server.reply(200, ApiDoc.response(me, 200))
        ProfileRepositoryImpl(network.retrofit.create(UsersApi::class.java), dao).refresh()
        server.reply(204)

        repository.logout()

        assertEquals(
            "/api/v1/auth/logout",
            server.run {
                takeRequest()
                takeRequest()
            }.url.encodedPath
        )
        assertNull(tokenStore.tokens)
        assertNull(dao.state.value)
        assertTrue(onboarding.cleared)
    }

    @Test
    fun `los logs de red nunca llevan tokens, contrasenas ni correos`() = runTest {
        server.reply(200, ApiDoc.response(login, 200))
        server.reply(200, ApiDoc.response(me, 200))

        repository.login("ana@ejemplo.com", TEST_PASSWORD)

        val logs = network.logs.joinToString("\n")
        assertTrue(logs.contains("POST /api/v1/auth/login"))
        assertTrue(logs.contains("request_id="))
        listOf(TEST_PASSWORD, "ana@ejemplo.com", "eyJ", "q8Zs3v1nX0r4bE7yTk2mW9pLhA6cUfGd", "Bearer").forEach {
            assertFalse("El log contiene $it", logs.contains(it))
        }
    }
}
