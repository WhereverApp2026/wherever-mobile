package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.account.data.AccountApi
import com.wherever.mobile.feature.account.data.AccountRepositoryImpl
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.profile.domain.UserProfile
import com.wherever.mobile.testing.ApiDoc
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.RealNetwork
import com.wherever.mobile.testing.reply
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

/**
 * nv-51 (Data): contraseña, correo, exportación e historial con la cadena de red real
 * (wherever-backend `docs/endpoints.md`, nv-04 y nv-05).
 */
class AccountIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private val tokenStore = FakeTokenStore(SessionTokens("vigente", "refresh-vigente", "2026-11-19T15:00:00Z"))
    private val profiles = FakeProfileRepository(sampleProfile())
    private var refreshes = 0
    private lateinit var network: RealNetwork
    private lateinit var repository: AccountRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        network = RealNetwork(server, tokenStore)
        refreshes = 0
        repository = AccountRepositoryImpl(network.retrofit.create(AccountApi::class.java), countingRefresh())
    }

    private fun countingRefresh() = object : ProfileRepository by profiles {
        override suspend fun refresh(): Result<UserProfile> {
            refreshes++
            return profiles.refresh()
        }
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `cambiar contrasena manda PUT con los dos campos`() = runTest {
        server.reply(204)

        assertTrue(repository.changePassword("Actual123", "Nueva4567") is Result.Success)

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/auth/password", request.url.encodedPath)
        assertEquals("""{"password_actual":"Actual123","password_nuevo":"Nueva4567"}""", request.body?.utf8())
        assertEquals("Bearer vigente", request.headers["Authorization"])
    }

    @Test
    fun `contrasena actual incorrecta da 401 sin cerrar la sesion`() = runTest {
        // El Authenticator renueva una vez, repite y el segundo 401 llega a la app.
        server.reply(401, ApiDoc.error(401, "credenciales inválidas"))
        server.reply(200, ApiDoc.response("POST /api/v1/auth/refresh", 200))
        server.reply(401, ApiDoc.error(401, "credenciales inválidas"))

        val result = repository.changePassword("Mala1234", "Nueva4567")

        assertTrue((result as Result.Failure).error is AppError.Unauthorized)
        assertNotNull(tokenStore.tokens)
        assertEquals(3, server.requestCount)
    }

    @Test
    fun `cambiar correo recorta, manda la contrasena y refresca el perfil`() = runTest {
        server.reply(200, """{"id":"u1","correo":"nuevo@ejemplo.com","correo_verificado":false}""")

        assertTrue(repository.changeEmail(" nuevo@ejemplo.com ", "Actual123") is Result.Success)

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/usuarios/yo/correo", request.url.encodedPath)
        assertEquals("""{"correo_nuevo":"nuevo@ejemplo.com","password":"Actual123"}""", request.body?.utf8())
        assertEquals(1, refreshes)
    }

    @Test
    fun `correo en uso da 409 y no refresca`() = runTest {
        server.reply(409, ApiDoc.error(409, "correo ya registrado"))

        val result = repository.changeEmail("otro@ejemplo.com", "Actual123")

        assertTrue((result as Result.Failure).error is AppError.Conflict)
        assertEquals(0, refreshes)
    }

    @Test
    fun `exportar da 202 y la segunda del dia 429 con Retry-After`() = runTest {
        server.reply(202)
        server.reply(429, ApiDoc.error(429, "demasiadas solicitudes"), "Retry-After" to "3600")

        assertTrue(repository.requestDataExport() is Result.Success)
        val limited = (repository.requestDataExport() as Result.Failure).error

        assertEquals("/api/v1/usuarios/yo/exportar", server.takeRequest().url.encodedPath)
        assertEquals(3600L, (limited as AppError.RateLimited).retryAfterSeconds)
    }

    @Test
    fun `borrar historial manda DELETE`() = runTest {
        server.reply(204)

        assertTrue(repository.clearHistory() is Result.Success)

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/v1/usuarios/yo/historial", request.url.encodedPath)
        assertFalse(network.logs.any { it.contains("Actual123") })
    }
}
