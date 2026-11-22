package com.wherever.mobile.feature.memories.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.ApiJson
import com.wherever.mobile.feature.memories.domain.MomentDraft
import com.wherever.mobile.feature.memories.domain.NewPostcard
import com.wherever.mobile.feature.memories.domain.PostcardState
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import com.wherever.mobile.feature.memories.domain.Visibility
import com.wherever.mobile.testing.samplePostcard
import com.wherever.mobile.testing.testRetrofit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private fun postcardJson(id: String, state: String = "lista") = """
    {"id":"$id","imagen_id":"0123456789abcdef0123456789abcdef","imagen_url":"https://img/$id.jpg","estilo":"polaroid",
    "texto":"Hola","visibilidad":"privada","estado":"$state","plan_id":"p1","negocio_id":"n1","propia":true,
    "version":5,"creado_en":"2026-10-11T18:00:00Z","actualizado_en":"x"}
""".trimIndent()

/** nv-31 (datos): MockWebServer real con las respuestas del contrato. */
class MemoriesRepositoryImplTest {
    private val server = MockWebServer()
    private val responses = mutableMapOf<String, MockResponse>()
    private lateinit var repository: MemoriesRepositoryImpl

    @Before
    fun setUp() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val query = request.url.queryParameter("cursor")?.let { "?cursor=$it" }.orEmpty()
                return responses["${request.method} ${request.url.encodedPath}$query"]
                    ?: responses["${request.method} ${request.url.encodedPath}"]
                    ?: MockResponse.Builder().code(404).build()
            }
        }
        server.start()
        repository = MemoriesRepositoryImpl(testRetrofit(server.url("/")).create(MemoriesApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    private fun respond(key: String, code: Int, body: String = "", headers: Map<String, String> = emptyMap()) {
        responses[key] = MockResponse.Builder().code(code).body(body).apply {
            headers.forEach { (name, value) -> addHeader(name, value) }
        }.build()
    }

    private fun requests() = buildList {
        while (true) add(server.takeRequest(100, TimeUnit.MILLISECONDS) ?: break)
    }

    @Test
    fun `POST postales responde 202 y el id sale de Location`() = runTest {
        respond("POST /postales", 202, headers = mapOf("Location" to "/api/v1/postales/abc123"))

        val result = repository.createPostcard(
            NewPostcard(
                "0123456789abcdef0123456789abcdef",
                PostcardStyle.PASSPORT,
                "  Hola  ",
                Visibility.PRIVATE,
                "n1",
                "p1"
            )
        )

        assertEquals("abc123", (result as Result.Success).data)
        val body = ApiJson.decodeFromString(JsonObject.serializer(), requests().single().body!!.utf8())
        assertEquals("pasaporte", body["estilo"]!!.jsonPrimitive.content)
        assertEquals("Hola", body["texto"]!!.jsonPrimitive.content)
        assertEquals("privada", body["visibilidad"]!!.jsonPrimitive.content)
    }

    @Test
    fun `POST postales con 409 llega como Conflict aunque sea Response`() = runTest {
        respond("POST /postales", 409, """{"codigo":"conflicto","mensaje":"imagen ya usada","campos":[]}""")

        val error = (
            repository.createPostcard(
                NewPostcard("i", PostcardStyle.CLASSIC, "", Visibility.PRIVATE)
            ) as Result.Failure
            )
            .error

        assertTrue(error is AppError.Conflict)
        assertEquals("imagen ya usada", error.apiMessage)
    }

    @Test
    fun `GET postales pagina con cursor`() = runTest {
        respond("GET /postales", 200, """{"items":[${postcardJson("c1")}],"siguiente_cursor":"n2","limite":20}""")
        respond(
            "GET /postales?cursor=n2",
            200,
            """{"items":[${postcardJson("c2", "en_moderacion")}],"siguiente_cursor":null,"limite":20}"""
        )

        val first = (repository.postcards() as Result.Success).data
        val second = (repository.postcards(first.nextCursor) as Result.Success).data

        assertEquals("n2", first.nextCursor)
        assertEquals(PostcardStyle.POLAROID, first.items.single().style)
        assertEquals(5L, first.items.single().version)
        assertEquals(PostcardState.IN_MODERATION, second.items.single().state)
        assertNull(second.nextCursor)
        assertEquals("n2", requests().last().url.queryParameter("cursor"))
    }

    @Test
    fun `DELETE manda If-Match con la version`() = runTest {
        respond("DELETE /postales/c1", 204)

        assertTrue(repository.deletePostcard(samplePostcard("c1", version = 5)) is Result.Success)

        assertEquals("\"5\"", requests().single().headers["If-Match"])
    }

    @Test
    fun `postal ajena es NotFound`() = runTest {
        respond("GET /postales/ajena", 404, """{"codigo":"no_encontrado","mensaje":"no existe","campos":[]}""")

        assertTrue((repository.postcard("ajena") as Result.Failure).error is AppError.NotFound)
    }

    @Test
    fun `ver una postal trae su lugar y su plan`() = runTest {
        respond("GET /postales/c1", 200, postcardJson("c1"))
        respond(
            "GET /lugares/n1",
            200,
            """
            {"id":"n1","nombre":"Café Raíz","categoria":"cafeteria",
            "direccion":{"calle":"Av. Hidalgo","numero":"23"}}
            """.trimIndent()
        )
        respond(
            "GET /planes/p1",
            200,
            """{"id":"p1","nombre":"Paseo en pareja","fecha":"2026-10-11","paradas":[{"id":"s1"},{"id":"s2"}]}"""
        )

        val view = (repository.postcard("c1") as Result.Success).data

        assertEquals("Café Raíz", view.place?.name)
        assertEquals("Av. Hidalgo 23", view.place?.address)
        assertEquals(2, view.plan?.stops)
    }

    @Test
    fun `si el lugar falla la postal se muestra igual`() = runTest {
        respond("GET /postales/c1", 200, postcardJson("c1"))

        val view = (repository.postcard("c1") as Result.Success).data

        assertNull(view.place)
        assertNull(view.plan)
    }

    @Test
    fun `recuerdos por plan`() = runTest {
        respond(
            "GET /recuerdos",
            200,
            """{"items":[{"id":"m1","plan_id":"p1","titulo":"Paseo","fecha":"2026-10-11",
            "paradas":[{"nombre":"Café","hora":"10:00","foto_url":"https://img/s.jpg"}],"postales_ids":["c1","c2"],
            "gasto_total":0,"puntos_ganados":0}],"siguiente_cursor":null,"limite":20}"""
        )

        val memory = (repository.memories() as Result.Success).data.items.single()

        assertEquals(listOf("c1", "c2"), memory.postcardIds)
        assertEquals("https://img/s.jpg", memory.stops.single().photoUrl)
    }

    @Test
    fun `POST momentos con etiquetados y sin texto vacio`() = runTest {
        respond("POST /momentos", 201, """{"id":"mo1"}""")

        repository.createMoment(MomentDraft("c1", "   ", Visibility.FRIENDS, listOf("u1", "u2")))

        val body = ApiJson.decodeFromString(JsonObject.serializer(), requests().single().body!!.utf8())
        assertEquals(setOf("postal_id", "visibilidad", "etiquetados"), body.keys)
        assertEquals(2, body["etiquetados"]!!.jsonArray.size)
    }
}
