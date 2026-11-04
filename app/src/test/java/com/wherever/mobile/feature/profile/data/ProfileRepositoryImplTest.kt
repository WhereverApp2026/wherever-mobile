package com.wherever.mobile.feature.profile.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.profile.domain.PreferencesPatch
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.Tastes
import com.wherever.mobile.testing.FakeUserProfileDao
import com.wherever.mobile.testing.testRetrofit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Red real con MockWebServer. El DAO es falso aquí; Room real está en androidTest (UserProfileDaoTest). */
class ProfileRepositoryImplTest {
    private val server = MockWebServer()
    private val dao = FakeUserProfileDao()
    private lateinit var repository: ProfileRepositoryImpl

    private fun profileJson(version: Int = 3, name: String = "Ana López") = """
        {"id":"u1","nombre":"$name","correo":"ana@ejemplo.com","correo_verificado":true,"foto_url":"",
        "codigo_amigo":"K7M2Q9XA","roles":["consumidor"],"modo_actual":"explorar",
        "ciudad":{"id":"c1","nombre":"Guadalajara"},
        "gustos":{"categorias":["cafes"],"presupuesto_min":100,"presupuesto_max":500,"restricciones":[],"alergias":""},
        "preferencias":{"movilidad_default":"a_pie","tema":"sistema","unidades":"km","horario_silencioso":null,
        "notificaciones":{"reservas":true,"cupones":true,"grupos":true,"resenas":true,"promociones":false}},
        "privacidad":{"ubicacion":true,"aprendizaje_personalizado":true,"estado_animo":false,"datos_uso":true,
        "buscable_por_telefono":false,"version_terminos":"2026-10","fecha_aceptacion":"x","promociones_correo":false},
        "telefono_registrado":false,"puntos":50,"nivel":"Explorador","racha_semanas":0,"estado":"activo",
        "eliminar_en":null,"version":$version,"creado_en":"x","actualizado_en":"x"}
    """.trimIndent()

    @Before
    fun setUp() {
        server.start()
        repository = ProfileRepositoryImpl(testRetrofit(server.url("/")).create(UsersApi::class.java), dao)
    }

    @After
    fun tearDown() = server.close()

    private fun respond(code: Int, body: String = "") {
        server.enqueue(MockResponse.Builder().code(code).body(body).build())
    }

    @Test
    fun `refresh guarda el perfil en Room y campos desconocidos no rompen`() = runTest {
        respond(200, profileJson())

        val result = repository.refresh() as Result.Success

        assertEquals("Guadalajara", result.data.city?.name)
        assertEquals(3L, dao.state.value?.version)
        assertEquals("/usuarios/yo", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `PATCH manda If-Match con la version guardada y solo lo que cambia`() = runTest {
        dao.state.value = dtoEntity(version = 7)
        respond(200, profileJson(version = 8, name = "Ana L."))

        val result = repository.updateProfile(name = "Ana L.") as Result.Success

        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("\"7\"", request.headers["If-Match"])
        assertEquals("""{"nombre":"Ana L."}""", request.body?.utf8())
        assertEquals(8L, result.data.version)
        assertEquals(8L, dao.state.value?.version)
    }

    @Test
    fun `sin perfil en cache pide la version antes del PATCH`() = runTest {
        respond(200, profileJson(version = 5))
        respond(200, profileJson(version = 6))

        repository.updateProfile(cityId = "c2")

        server.takeRequest()
        val patch = server.takeRequest()
        assertEquals("\"5\"", patch.headers["If-Match"])
        assertEquals("""{"ciudad_id":"c2"}""", patch.body?.utf8())
    }

    @Test
    fun `409 llega como Conflict y no toca Room`() = runTest {
        dao.state.value = dtoEntity(version = 7)
        respond(409, """{"codigo":"conflicto","mensaje":"versión vieja","campos":[]}""")

        val result = repository.updateProfile(name = "Otro") as Result.Failure

        assertTrue(result.error is AppError.Conflict)
        assertEquals(7L, dao.state.value?.version)
    }

    @Test
    fun `gustos actualiza Room y vuelve a pedir el perfil por la version`() = runTest {
        dao.state.value = dtoEntity(version = 7)
        val saved = """{"categorias":["tacos","museos","cafes"],"presupuesto_min":150,"presupuesto_max":300,
            "restricciones":[],"alergias":"nueces"}"""
        respond(200, saved)
        respond(200, profileJson(version = 9))

        val result = repository.updateTastes(
            Tastes(listOf("tacos", "museos", "cafes"), 150, 300, emptyList(), "nueces")
        )

        assertEquals(Result.Success(Unit), result)
        val put = server.takeRequest()
        assertEquals("PUT", put.method)
        assertEquals("/usuarios/yo/gustos", put.url.encodedPath)
        assertEquals(9L, dao.state.value?.version)
    }

    @Test
    fun `preferencias manda solo lo que cambia`() = runTest {
        respond(200, """{"movilidad_default":"a_pie","tema":"oscuro","unidades":"km","notificaciones":{}}""")
        respond(200, profileJson())

        repository.updatePreferences(PreferencesPatch(theme = "oscuro"))

        assertEquals("""{"tema":"oscuro"}""", server.takeRequest().body?.utf8())
    }

    @Test
    fun `notificaciones van anidadas`() = runTest {
        respond(200, """{"notificaciones":{}}""")
        respond(200, profileJson())

        repository.updatePreferences(PreferencesPatch(promotions = true))

        assertEquals("""{"notificaciones":{"promociones":true}}""", server.takeRequest().body?.utf8())
    }

    @Test
    fun `privacidad usa los nombres de la API`() = runTest {
        respond(200, """{"ubicacion":true}""")
        respond(200, profileJson())

        repository.updatePrivacy(PrivacyPatch(searchableByPhone = true))

        assertEquals("""{"buscable_por_telefono":true}""", server.takeRequest().body?.utf8())
    }

    @Test
    fun `eliminar cuenta manda la confirmacion en un DELETE con cuerpo`() = runTest {
        respond(202, """{"eliminar_en":"2026-12-03T19:10:00Z"}""")

        val result = repository.deleteAccount("ELIMINAR")

        assertEquals(Result.Success("2026-12-03T19:10:00Z"), result)
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("""{"confirmacion":"ELIMINAR"}""", request.body?.utf8())
    }

    @Test
    fun `autocompletar ciudades manda q y limite`() = runTest {
        respond(200, """[{"id":"c1","nombre":"Guadalajara","estado":"Jalisco"}]""")

        val result = repository.searchCities("guada") as Result.Success

        assertEquals("Jalisco", result.data.single().state)
        assertEquals("guada", server.takeRequest().url.queryParameter("q"))
    }

    private fun dtoEntity(version: Long) = UserProfileDto(id = "u1", name = "Ana", email = "a@b.co", version = version)
        .toEntity()
}
