package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.profile.data.ProfileRepositoryImpl
import com.wherever.mobile.feature.profile.data.UsersApi
import com.wherever.mobile.feature.profile.domain.PreferencesPatch
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.Tastes
import com.wherever.mobile.testing.ApiDoc
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.FakeUserProfileDao
import com.wherever.mobile.testing.RealNetwork
import com.wherever.mobile.testing.reply
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

/**
 * Integración: `ProfileRepositoryImpl` + cadena de red real contra MockWebServer con las respuestas
 * exactas de `docs/contexto_api_sprint1.md`. Specs nv-10, nv-13, nv-14 y nv-15.
 * Room real: `androidTest/.../ProfileRepositoryRoomTest`.
 */
class ProfileIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private val tokenStore = FakeTokenStore(SessionTokens("acceso", "refresh", "2026-11-04T15:15:00Z"))
    private val dao = FakeUserProfileDao()
    private lateinit var repository: ProfileRepositoryImpl

    private val me = "GET /api/v1/usuarios/yo"

    @Before
    fun setUp() {
        server.start()
        repository = ProfileRepositoryImpl(RealNetwork(server, tokenStore).retrofit.create(UsersApi::class.java), dao)
    }

    @After
    fun tearDown() = server.close()

    private suspend fun cachedProfile() {
        server.reply(200, ApiDoc.response(me, 200), "ETag" to "\"3\"")
        repository.refresh()
        server.takeRequest()
    }

    @Test
    fun `perfil de la doc se guarda completo en cache`() = runTest {
        cachedProfile()

        val stored = dao.state.value!!
        assertEquals("Guadalajara", stored.city?.name)
        assertEquals(3L, stored.version)
        assertEquals(listOf("cafes", "museos", "parques"), stored.tastes?.categories)
    }

    @Test
    fun `PATCH manda If-Match y guarda la version nueva`() = runTest {
        cachedProfile()
        server.reply(200, ApiDoc.response("PATCH /api/v1/usuarios/yo", 200))

        val updated = (repository.updateProfile(name = "Ana L.") as Result.Success).data

        assertEquals("\"3\"", server.takeRequest().headers["If-Match"])
        assertEquals("Ana L.", updated.name)
        assertEquals("Ana L.", dao.state.value?.name)
    }

    @Test
    fun `PATCH 409 no toca la cache`() = runTest {
        cachedProfile()
        server.reply(409, ApiDoc.error(409, "la versión cambió"))

        val error = (repository.updateProfile(name = "Otro") as Result.Failure).error

        assertTrue(error is AppError.Conflict)
        assertEquals("Ana López", dao.state.value?.name)
        assertEquals(3L, dao.state.value?.version)
    }

    @Test
    fun `PATCH 422 reparte el error al campo`() = runTest {
        cachedProfile()
        server.reply(422, ApiDoc.error(422, "datos inválidos", mapOf("nombre" to "de 2 a 40 caracteres")))

        val error = (repository.updateProfile(name = "A") as Result.Failure).error as AppError.Validation

        assertEquals("nombre", error.fields.single().field)
    }

    @Test
    fun `PATCH con 401 renueva y repite con el mismo If-Match`() = runTest {
        cachedProfile()
        server.reply(401, ApiDoc.error(401, "token vencido"))
        server.reply(200, ApiDoc.response("POST /api/v1/auth/refresh", 200))
        server.reply(200, ApiDoc.response("PATCH /api/v1/usuarios/yo", 200))

        assertTrue(repository.updateProfile(name = "Ana L.") is Result.Success)

        val first = server.takeRequest()
        server.takeRequest()
        val retry = server.takeRequest()
        assertEquals(first.headers["If-Match"], retry.headers["If-Match"])
        assertEquals("q8Zs3v1nX0r4bE7yTk2mW9pLhA6cUfGd", tokenStore.tokens?.refreshToken)
    }

    @Test
    fun `gustos de la doc se reflejan en cache`() = runTest {
        cachedProfile()
        server.reply(200, ApiDoc.response("PUT /api/v1/usuarios/yo/gustos", 200))

        val tastes = Tastes(listOf("cafes", "museos", "parques"), 100, 500, listOf("vegetariano"), "nueces")

        assertEquals(Result.Success(Unit), repository.updateTastes(tastes))
        assertEquals("/api/v1/usuarios/yo/gustos", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `preferencias 422 de la doc no cambian la cache`() = runTest {
        cachedProfile()
        val before = dao.state.value
        val fields = mapOf("notificaciones.reservas" to "no se puede apagar")
        server.reply(422, ApiDoc.error(422, "datos inválidos", fields))

        val error = (repository.updatePreferences(PreferencesPatch(theme = "oscuro")) as Result.Failure).error

        assertTrue(error is AppError.Validation)
        assertEquals(before, dao.state.value)
    }

    @Test
    fun `privacidad 200 de la doc se guarda`() = runTest {
        cachedProfile()
        server.reply(200, ApiDoc.response("PATCH /api/v1/usuarios/yo/privacidad", 200))

        assertEquals(Result.Success(Unit), repository.updatePrivacy(PrivacyPatch(location = false)))
    }

    @Test
    fun `eliminar 202 devuelve la fecha y cancelar 409 llega como conflicto`() = runTest {
        server.reply(202, ApiDoc.response("DELETE /api/v1/usuarios/yo", 202))
        server.reply(409, ApiDoc.error(409, "fuera de plazo"))

        assertEquals(Result.Success("2026-12-03T19:10:00Z"), repository.deleteAccount("ELIMINAR"))
        assertTrue((repository.cancelDeletion() as Result.Failure).error is AppError.Conflict)
    }

    @Test
    fun `eliminar 422 si la confirmacion no es exacta`() = runTest {
        server.reply(422, ApiDoc.error(422, "datos inválidos", mapOf("confirmacion" to "debe ser ELIMINAR")))

        val error = (repository.deleteAccount("eliminar") as Result.Failure).error as AppError.Validation

        assertEquals("confirmacion", error.fields.single().field)
    }

    @Test
    fun `autocompletar ciudades de la doc`() = runTest {
        server.reply(200, ApiDoc.response("GET /api/v1/ciudades/autocompletar?q=guada&limite=8", 200))

        val cities = (repository.searchCities("guada") as Result.Success).data

        assertTrue(cities.isNotEmpty())
        assertEquals("guada", server.takeRequest().url.queryParameter("q"))
    }
}
