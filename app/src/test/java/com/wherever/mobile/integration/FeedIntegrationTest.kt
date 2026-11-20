package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.feed.data.FeedApi
import com.wherever.mobile.feature.feed.data.FeedRepositoryImpl
import com.wherever.mobile.testing.FakeTokenStore
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

/** Feed de amigos (specs/nv-17, backend nv-63 y nv-69) contra MockWebServer con respuestas como las de staging. */
class FeedIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private lateinit var repository: FeedRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val tokens = FakeTokenStore(SessionTokens("acceso", "refresh", "2026-11-04T15:15:00Z"))
        repository = FeedRepositoryImpl(RealNetwork(server, tokens).retrofit.create(FeedApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `lee el feed y convierte los vacios de la API en nulos`() = runTest {
        server.reply(
            200,
            """{"items":[{"id":"m1","autor":{"id":"u1","nombre_usuario":"qa_b"},""" +
                """"postal":{"id":"p1","imagen_url":"https://cdn/i/1080.jpg","estilo":"clasica","texto":"Tacos"},""" +
                """"negocio_id":null,"texto":"","visibilidad":"amigos","estado":"publicado","etiquetados":[],""" +
                """"plan_id":"","reacciones_total":0,"propio":false,"version":1,"creado_en":"2026-11-20T18:21:20.453Z"}],""" +
                """"siguiente_cursor":"","limite":20}"""
        )

        val page = (repository.feed(null) as Result.Success).data

        val moment = page.items.single()
        assertEquals("qa_b", moment.author.username)
        assertEquals("https://cdn/i/1080.jpg", moment.imageUrl)
        assertEquals("Tacos", moment.text)
        assertNull(moment.placeId)
        assertNull(moment.planId)
        assertNull(page.nextCursor)
        val request = server.takeRequest()
        assertEquals("/api/v1/momentos/feed", request.url.encodedPath)
        assertEquals("20", request.url.queryParameter("limite"))
        assertNull(request.url.queryParameter("cursor"))
        assertEquals("Bearer acceso", request.headers["Authorization"])
    }

    @Test
    fun `manda el cursor de la siguiente pagina`() = runTest {
        server.reply(200, """{"items":[],"siguiente_cursor":"","limite":20}""")

        repository.feed("abc")

        assertEquals("abc", server.takeRequest().url.queryParameter("cursor"))
    }

    @Test
    fun `momento ajeno o borrado es NotFound`() = runTest {
        server.reply(404, """{"codigo":"no_encontrado","mensaje":"recurso no encontrado","campos":[],"traza_id":"t"}""")

        val result = repository.moment("m9")

        assertTrue((result as Result.Failure).error is AppError.NotFound)
        assertEquals("/api/v1/momentos/m9", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `me apunto hace POST y plan lleno es Conflict`() = runTest {
        server.reply(200, """{"id":"g1","plan_id":"pl1","mi_rol":"invitado","mi_estado":"confirmado"}""")
        server.reply(409, """{"codigo":"grupo_lleno","mensaje":"el grupo está lleno","campos":[],"traza_id":"t"}""")

        assertEquals(Result.Success(Unit), repository.joinPlan("pl1"))
        val first = server.takeRequest()
        assertEquals("POST", first.method)
        assertEquals("/api/v1/planes/pl1/me-apunto", first.url.encodedPath)
        assertTrue((repository.joinPlan("pl1") as Result.Failure).error is AppError.Conflict)
    }
}
