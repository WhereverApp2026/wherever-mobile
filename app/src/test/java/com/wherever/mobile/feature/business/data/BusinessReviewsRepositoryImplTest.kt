package com.wherever.mobile.feature.business.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.business.domain.ReplyStatus
import com.wherever.mobile.feature.business.domain.ReviewPeriod
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.testing.testRetrofit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** nv-36 (datos y seguridad): resumen por periodo, PUT respuesta, POST sugerir-respuesta y PATCH momentos. */
class BusinessReviewsRepositoryImplTest {
    private val server = MockWebServer()
    private lateinit var repository: BusinessReviewsRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        repository = BusinessReviewsRepositoryImpl(
            testRetrofit(server.url("/"), OkHttpClient()).create(BusinessReviewsApi::class.java)
        )
    }

    @After
    fun tearDown() = server.close()

    private fun enqueue(code: Int, body: String) = server.enqueue(MockResponse.Builder().code(code).body(body).build())

    private fun takeRequest() = server.takeRequest(5, TimeUnit.SECONDS)!!

    @Test
    fun `resumen manda el periodo y descarta aspectos desconocidos o nulos`() = runTest {
        enqueue(
            200,
            """{"actual":{"total":126,"promedio":4.8,"aspectos":{"sabor":9.4,"atencion":null,"otro":3.0}},
            "anterior":{"total":100,"promedio":4.6},"distribucion":{"5":100,"4":20,"1":6,"9":1}}"""
        )

        val summary = (repository.summary("n1", ReviewPeriod.QUARTER) as Result.Success).data

        val request = takeRequest()
        assertEquals("/negocios/n1/resenas/resumen", request.url.encodedPath)
        assertEquals("trimestre", request.url.queryParameter("periodo"))
        assertEquals(mapOf(Aspect.TASTE to 9.4), summary.aspects)
        assertEquals(4.6, summary.previousAverage)
        assertEquals(mapOf(5 to 100, 4 to 20, 1 to 6), summary.distribution)
    }

    @Test
    fun `responder manda solo el texto recortado con PUT`() = runTest {
        enqueue(200, """{"id":"r1","respuesta_negocio":{"texto":"Gracias","estado":"publicada"}}""")

        val status = (repository.reply("n1", "r1", "  Gracias  ") as Result.Success).data

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/negocios/n1/resenas/r1/respuesta", request.url.encodedPath)
        assertEquals("""{"texto":"Gracias"}""", request.body!!.utf8())
        assertEquals(ReplyStatus.PUBLISHED, status)
    }

    @Test
    fun `respuesta en moderacion y 422 con motivo`() = runTest {
        enqueue(200, """{"respuesta_negocio":{"texto":"x","estado":"en_moderacion"}}""")
        assertEquals(ReplyStatus.IN_MODERATION, (repository.reply("n1", "r1", "x") as Result.Success).data)

        enqueue(422, """{"codigo":"moderacion","mensaje":"lenguaje no permitido","campos":[],"traza_id":"t"}""")
        val error = (repository.reply("n1", "r1", "x") as Result.Failure).error
        assertTrue(error is AppError.Validation)
    }

    @Test
    fun `sugerir con IA solo manda el id de la resena, sin datos del autor`() = runTest {
        enqueue(200, """{"texto":"¡Gracias por venir!"}""")

        val text = (repository.suggestReply("n1", "r1") as Result.Success).data

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/negocios/n1/resenas/r1/sugerir-respuesta", request.url.encodedPath)
        val body = request.body?.utf8().orEmpty()
        assertFalse(body.contains("autor"))
        assertFalse(body.contains("nombre"))
        assertFalse(body.contains("comentario"))
        assertEquals("¡Gracias por venir!", text)
    }

    @Test
    fun `sin IA el 503 llega como error del servidor`() = runTest {
        enqueue(503, """{"codigo":"ia_no_disponible","mensaje":"sin IA","campos":[],"traza_id":"t"}""")

        assertTrue((repository.suggestReply("n1", "r1") as Result.Failure).error is AppError.Server)
    }

    @Test
    fun `momentos destacados y marcar manda solo el campo cambiado`() = runTest {
        enqueue(
            200,
            """{"items":[{"id":"m1","autor":{"id":"u1","nombre_usuario":"sofi"},"postal":{"imagen_url":"https://x/1.jpg"},
            "creado_en":"2026-11-20T19:30:00Z","agradecido_por_negocio":false,"destacado_por_negocio":true}]}"""
        )
        val moments = (repository.moments("n1", featured = true) as Result.Success).data
        val list = takeRequest()
        assertEquals("true", list.url.queryParameter("destacados"))
        assertEquals("sofi", moments.single().author)
        assertEquals("https://x/1.jpg", moments.single().imageUrl)
        assertTrue(moments.single().featured)

        enqueue(200, """{"id":"m1","autor":{"id":"u1"},"agradecido_por_negocio":true,"destacado_por_negocio":true}""")
        val marked = (repository.markMoment("n1", "m1", thanked = true, featured = null) as Result.Success).data
        val patch = takeRequest()
        assertEquals("PATCH", patch.method)
        assertEquals("/negocios/n1/momentos/m1", patch.url.encodedPath)
        assertEquals("""{"agradecido":true}""", patch.body!!.utf8())
        assertTrue(marked.thanked)
    }
}
