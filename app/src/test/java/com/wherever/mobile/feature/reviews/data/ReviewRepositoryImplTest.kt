package com.wherever.mobile.feature.reviews.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.ApiJson
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.PlaceReviewQuery
import com.wherever.mobile.feature.reviews.domain.ReportReason
import com.wherever.mobile.feature.reviews.domain.ReviewDraft
import com.wherever.mobile.feature.reviews.domain.ReviewState
import com.wherever.mobile.testing.testRetrofit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
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

private const val PLACE = """{"id":"p1","nombre":"Café Raíz","fotos":[{"url":"https://img/p1.jpg","orden":0}],
"calificacion":{"promedio":4.8,"total_resenas":126,"aspectos":{"sabor":4.9}}}"""

private fun reviewJson(version: Int = 3) = """
    {"id":"r1","negocio_id":"p1","checkin_id":"c1","calificacion":4,
    "aspectos":{"sabor":5,"atencion":4,"ambiente":4,"espera":3,"precio_calidad":4},"chips":[],
    "comentario":"Muy rico","fotos":[],"estado":"publicada","util":0,"respuesta_negocio":{"texto":"Gracias"},
    "editable":true,"version":$version,"creado_en":"2026-11-20T17:00:00Z","actualizado_en":"x","campo_nuevo":1}
""".trimIndent()

/** nv-32 (datos): MockWebServer real con las respuestas del contrato de `docs/endpoints.md`. */
class ReviewRepositoryImplTest {
    private val server = MockWebServer()
    private lateinit var repository: ReviewRepositoryImpl
    private val responses = mutableMapOf<String, MockResponse>()

    @Before
    fun setUp() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val key = "${request.method} ${request.url.encodedPath}"
                return responses[key] ?: MockResponse.Builder().code(404).build()
            }
        }
        server.start()
        repository = ReviewRepositoryImpl(testRetrofit(server.url("/")).create(ReviewsApi::class.java))
        respond("GET /lugares/p1", 200, PLACE)
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

    private fun draft() = ReviewDraft(
        checkinId = "c1",
        rating = 4,
        aspects = Aspect.entries.associateWith { 4 },
        comment = "  Muy rico  ",
        photoIds = listOf("i1")
    )

    @Test
    fun `pendientes con nombre y foto del lugar`() = runTest {
        respond(
            "GET /resenas/pendientes",
            200,
            """
            {"items":[{"checkin_id":"c1","negocio_id":"p1","negocio_nombre":"Café Raíz",
            "dia":"2026-11-20","vence_en":"2026-12-20T06:00:00Z"}]}
            """.trimIndent()
        )

        val item = (repository.pending() as Result.Success).data.single()

        assertEquals("Café Raíz", item.placeName)
        assertEquals("https://img/p1.jpg", item.photoUrl)
        assertEquals("2026-11-20", item.day)
    }

    @Test
    fun `ya resenadas son los check-ins validos con resena`() = runTest {
        respond(
            "GET /checkins",
            200,
            """{"items":[
            {"id":"c1","negocio_id":"p1","valido":true,"dia":"2026-11-20","resena_id":"r1"},
            {"id":"c2","negocio_id":"p1","valido":true,"dia":"2026-11-21","resena_id":null},
            {"id":"c3","negocio_id":"p1","valido":false,"dia":"2026-11-22","resena_id":null}
            ],"siguiente_cursor":null,"limite":50}"""
        )

        val visits = (repository.reviewedVisits() as Result.Success).data

        assertEquals(listOf("c1"), visits.map { it.checkinId })
        assertEquals("Café Raíz", visits.single().placeName)
    }

    @Test
    fun `POST resenas manda lo que pide la API y guarda la version`() = runTest {
        respond("POST /resenas", 201, reviewJson(), mapOf("ETag" to "\"3\""))

        val review = (repository.create(draft()) as Result.Success).data

        val post = requests().first { it.method == "POST" }
        val body = ApiJson.decodeFromString(JsonObject.serializer(), post.body!!.utf8())
        assertEquals("c1", body["checkin_id"]!!.jsonPrimitive.content)
        assertEquals(4, body["calificacion"]!!.jsonPrimitive.int)
        assertEquals(Aspect.entries.map { it.apiKey }.toSet(), body["aspectos"]!!.jsonObject.keys)
        assertEquals("Muy rico", body["comentario"]!!.jsonPrimitive.content)
        assertEquals(3L, review.version)
        assertEquals("Gracias", review.businessReply)
        assertEquals(ReviewState.PUBLISHED, review.state)
        assertEquals(review, repository.cached("r1"))
    }

    @Test
    fun `422 de moderacion llega como Validation`() = runTest {
        respond("POST /resenas", 422, """{"codigo":"validacion","mensaje":"texto rechazado","campos":[]}""")

        val error = (repository.create(draft()) as Result.Failure).error

        assertTrue(error is AppError.Validation)
        assertEquals("texto rechazado", error.apiMessage)
    }

    @Test
    fun `PATCH manda If-Match y solo lo que cambia`() = runTest {
        respond("GET /resenas/mias", 200, """{"items":[${reviewJson(3)}],"siguiente_cursor":null,"limite":20}""")
        respond("PATCH /resenas/r1", 200, reviewJson(4))
        repository.mine()
        val review = repository.cached("r1")!!

        val updated = repository.update(
            review,
            draft().copy(rating = 2, comment = review.comment, photoIds = emptyList())
        )

        val patch = requests().first { it.method == "PATCH" }
        assertEquals("\"3\"", patch.headers["If-Match"])
        val body = ApiJson.decodeFromString(JsonObject.serializer(), patch.body!!.utf8())
        assertEquals(setOf("calificacion", "aspectos"), body.keys)
        assertEquals(4L, (updated as Result.Success).data.version)
        assertEquals("Café Raíz", updated.data.placeName)
    }

    @Test
    fun `PATCH con version vieja es Conflict`() = runTest {
        respond("GET /resenas/mias", 200, """{"items":[${reviewJson(3)}],"siguiente_cursor":null,"limite":20}""")
        respond("PATCH /resenas/r1", 409, """{"codigo":"conflicto","mensaje":"version vieja","campos":[]}""")
        repository.mine()

        val error = (repository.update(repository.cached("r1")!!, draft()) as Result.Failure).error

        assertTrue(error is AppError.Conflict)
    }

    @Test
    fun `DELETE 204 quita la resena de memoria`() = runTest {
        respond("GET /resenas/mias", 200, """{"items":[${reviewJson()}],"siguiente_cursor":"abc","limite":20}""")
        respond("DELETE /resenas/r1", 204)
        val page = (repository.mine() as Result.Success).data
        assertEquals("abc", page.nextCursor)

        assertTrue(repository.delete("r1") is Result.Success)
        assertNull(repository.cached("r1"))
    }

    @Test
    fun `GET resenas del lugar lleva los filtros en la query`() = runTest {
        respond(
            "GET /lugares/p1/resenas",
            200,
            """{"items":[{"id":"r9","autor":{"nombre_usuario":"ana","foto_url":null},"calificacion":5,"aspectos":{},
            "chips":[],"comentario":"Rico","fotos":[{"id":"f1","url":"https://img/f1.jpg"}],"util":12,
            "respuesta_negocio":null,"creado_en":"2026-11-19T18:00:00Z"}],"siguiente_cursor":"n2","limite":20}"""
        )

        val page = repository.placeReviews(
            "p1",
            PlaceReviewQuery(stars = 5, aspect = Aspect.TASTE, withPhotos = true, recent = true),
            cursor = "c0"
        ) as Result.Success

        val url = requests().first { it.url.encodedPath == "/lugares/p1/resenas" }.url
        assertEquals("5", url.queryParameter("estrellas"))
        assertEquals("sabor", url.queryParameter("aspecto"))
        assertEquals("true", url.queryParameter("con_fotos"))
        assertEquals("recientes", url.queryParameter("orden"))
        assertEquals("c0", url.queryParameter("cursor"))
        assertEquals("ana", page.data.items.single().author)
        assertEquals(listOf("https://img/f1.jpg"), page.data.items.single().photos)
        assertEquals("n2", page.data.nextCursor)
    }

    @Test
    fun `sin filtros no manda parametros vacios`() = runTest {
        respond("GET /lugares/p1/resenas", 200, """{"items":[],"siguiente_cursor":null,"limite":20}""")

        repository.placeReviews("p1", PlaceReviewQuery(), cursor = null)

        val url = requests().first { it.url.encodedPath == "/lugares/p1/resenas" }.url
        assertNull(url.queryParameter("estrellas"))
        assertNull(url.queryParameter("con_fotos"))
        assertEquals("utiles", url.queryParameter("orden"))
    }

    @Test
    fun `Util y Reportar`() = runTest {
        respond("PUT /resenas/r9/util", 200, """{"util":true,"total":13}""")
        respond("POST /resenas/r9/reportes", 201, """{"reportada":true}""")

        val useful = (repository.markUseful("r9", true) as Result.Success).data
        assertTrue(repository.report("r9", ReportReason.PERSONAL_DATA) is Result.Success)

        assertEquals(13, useful.total)
        val sent = requests()
        assertEquals("""{"util":true}""", sent.first { it.method == "PUT" }.body!!.utf8())
        assertEquals("""{"motivo":"datos_personales"}""", sent.first { it.method == "POST" }.body!!.utf8())
    }
}
