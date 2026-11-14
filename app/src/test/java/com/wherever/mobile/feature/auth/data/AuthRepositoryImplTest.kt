package com.wherever.mobile.feature.auth.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.auth.domain.DeviceInfo
import com.wherever.mobile.feature.auth.domain.OnboardingPreferences
import com.wherever.mobile.feature.auth.domain.PostAuthDestination
import com.wherever.mobile.feature.auth.domain.Registration
import com.wherever.mobile.feature.profile.data.UserProfileDto
import com.wherever.mobile.feature.profile.data.toEntity
import com.wherever.mobile.feature.profile.domain.Tastes
import com.wherever.mobile.testing.FakeDeviceRegistration
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.FakeUserProfileDao
import com.wherever.mobile.testing.TEST_PASSWORD
import com.wherever.mobile.testing.sampleProfile
import com.wherever.mobile.testing.testRetrofit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeOnboarding : OnboardingPreferences {
    val skipped = mutableSetOf<String>()
    var cleared = false

    override fun isTastesSkipped(userId: String) = userId in skipped

    override fun markTastesSkipped(userId: String) {
        skipped += userId
    }

    override fun clear() {
        cleared = true
    }
}

class AuthRepositoryImplTest {
    private val server = MockWebServer()
    private val tokenStore = FakeTokenStore()
    private val profiles = FakeProfileRepository()
    private val dao = FakeUserProfileDao()
    private val onboarding = FakeOnboarding()
    private val devices = FakeDeviceRegistration(tokenStore)
    private lateinit var repository: AuthRepositoryImpl

    private val tokensJson = """
        {"access_token":"a","refresh_token":"r","expira_en":"2026-11-04T15:15:00Z",
        "usuario":{"id":"u1","nombre":"Ana","correo":"ana@ejemplo.com","correo_verificado":false,"roles":["consumidor"]}}
    """.trimIndent()

    @Before
    fun setUp() {
        server.start()
        repository = AuthRepositoryImpl(
            api = testRetrofit(server.url("/")).create(AuthApi::class.java),
            tokenStore = tokenStore,
            profileRepository = profiles,
            profileDao = dao,
            onboarding = onboarding,
            deviceInfo = { DeviceInfo("Pixel 8", "Android 15", "0.1.0") },
            deviceRegistration = devices
        )
    }

    @After
    fun tearDown() = server.close()

    private fun respond(code: Int, body: String = "") {
        server.enqueue(MockResponse.Builder().code(code).body(body).build())
    }

    @Test
    fun `login guarda tokens y manda el dispositivo`() = runTest {
        respond(200, tokensJson)
        profiles.profile.value = sampleProfile(tastes = Tastes(listOf("a"), 0, 1, emptyList(), ""))

        val result = repository.login(" ana@ejemplo.com ", TEST_PASSWORD)

        assertEquals(Result.Success(PostAuthDestination.HOME), result)
        assertEquals(SessionTokens("a", "r", "2026-11-04T15:15:00Z"), tokenStore.tokens)
        val body = server.takeRequest().body?.utf8().orEmpty()
        assertTrue(body.contains(""""correo":"ana@ejemplo.com""""))
        assertTrue(body.contains(""""modelo":"Pixel 8""""))
        assertEquals(listOf("registerInBackground"), devices.calls)
    }

    @Test
    fun `login fallido no registra el telefono para push`() = runTest {
        respond(401)

        repository.login("a@b.co", TEST_PASSWORD)

        assertTrue(devices.calls.isEmpty())
    }

    @Test
    fun `login sin gustos lleva a Gustos, salvo que se hayan omitido`() = runTest {
        respond(200, tokensJson)
        profiles.profile.value = sampleProfile(tastes = null)
        assertEquals(Result.Success(PostAuthDestination.TASTES), repository.login("a@b.co", TEST_PASSWORD))

        onboarding.markTastesSkipped("u1")
        respond(200, tokensJson)
        assertEquals(Result.Success(PostAuthDestination.HOME), repository.login("a@b.co", TEST_PASSWORD))
    }

    @Test
    fun `login con eliminacion pendiente lleva a su pantalla`() = runTest {
        respond(200, tokensJson)
        profiles.profile.value = sampleProfile(deleteAt = "2026-12-03T19:10:00Z")

        assertEquals(Result.Success(PostAuthDestination.DELETION_PENDING), repository.login("a@b.co", TEST_PASSWORD))
    }

    @Test
    fun `login fallido no guarda tokens`() = runTest {
        respond(401, """{"codigo":"no_autenticado","mensaje":"correo o contraseña incorrectos","campos":[]}""")

        val result = repository.login("a@b.co", TEST_PASSWORD) as Result.Failure

        assertTrue(result.error is AppError.Unauthorized)
        assertNull(tokenStore.tokens)
    }

    @Test
    fun `registro manda terminos y deja sesion`() = runTest {
        respond(201, tokensJson)

        repository.register(Registration("Ana", "1999-04-12", "a@b.co", TEST_PASSWORD, true))

        val body = server.takeRequest().body?.utf8().orEmpty()
        assertTrue(body.contains(""""fecha_nacimiento":"1999-04-12""""))
        assertTrue(body.contains(""""acepta_terminos":true"""))
        assertTrue(body.contains(""""promociones_correo":false"""))
        assertNotNull(tokenStore.tokens)
    }

    @Test
    fun `logout limpia todo aunque la llamada falle`() = runTest {
        tokenStore.tokens = SessionTokens("a", "r", "x")
        dao.state.value = UserProfileDto(id = "u1", name = "A", email = "a@b.co", version = 1).toEntity()
        respond(503)

        repository.logout()

        assertNull(tokenStore.tokens)
        assertNull(dao.state.value)
        assertTrue(onboarding.cleared)
    }

    @Test
    fun `logout quita el telefono del push antes de cerrar la sesion`() = runTest {
        tokenStore.tokens = SessionTokens("a", "r", "x")
        respond(204)

        repository.logout()

        assertEquals(listOf("unregister"), devices.calls)
        assertEquals(true, devices.unregisteredWithSession)
    }

    @Test
    fun `logout-todos quita el telefono del push con la sesion abierta`() = runTest {
        tokenStore.tokens = SessionTokens("a", "r", "x")
        respond(204)

        repository.logoutAll()

        assertEquals(listOf("unregister"), devices.calls)
        assertEquals(true, devices.unregisteredWithSession)
    }

    @Test
    fun `logout-todos solo limpia si el servidor confirma`() = runTest {
        tokenStore.tokens = SessionTokens("a", "r", "x")
        respond(503)
        assertTrue(repository.logoutAll() is Result.Failure)
        assertNotNull(tokenStore.tokens)

        respond(204)
        assertEquals(Result.Success(Unit), repository.logoutAll())
        assertNull(tokenStore.tokens)
    }

    @Test
    fun `sesiones se leen y se cierran por id`() = runTest {
        respond(
            200,
            """[{"id":"s1","dispositivo":{"modelo":"Pixel","sistema":"Android","app_version":"1"},
            "ultimo_uso":"2026-11-03T19:05:00Z","actual":true}]"""
        )
        respond(204)

        val sessions = (repository.sessions() as Result.Success).data
        repository.closeSession("s1")

        assertEquals("Pixel", sessions.single().model)
        assertTrue(sessions.single().isCurrent)
        server.takeRequest()
        val delete = server.takeRequest()
        assertEquals("DELETE", delete.method)
        assertEquals("/auth/sesiones/s1", delete.url.encodedPath)
    }

    @Test
    fun `correo disponible y recuperar`() = runTest {
        respond(200, """{"disponible":false}""")
        respond(202)

        assertEquals(Result.Success(false), repository.isEmailAvailable("a@b.co"))
        assertEquals(Result.Success(Unit), repository.recoverPassword("a@b.co"))
        assertEquals("a@b.co", server.takeRequest().url.queryParameter("correo"))
        assertFalse(server.takeRequest().body?.utf8().isNullOrEmpty())
    }
}
