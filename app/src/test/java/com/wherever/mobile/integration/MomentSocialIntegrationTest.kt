package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.feed.data.MomentSocialApi
import com.wherever.mobile.feature.feed.data.MomentSocialRepositoryImpl
import com.wherever.mobile.feature.feed.domain.CommentState
import com.wherever.mobile.feature.feed.domain.ReactionTab
import com.wherever.mobile.testing.ApiDoc
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.RealNetwork
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

/** Me gusta, comentarios, reacciones (backend nv-83) e historias (nv-84) con la cadena de red real. */
class MomentSocialIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private lateinit var repository: MomentSocialRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val tokens = FakeTokenStore(SessionTokens("acceso", "refresh", "2026-11-04T15:15:00Z"))
        repository =
            MomentSocialRepositoryImpl(RealNetwork(server, tokens).retrofit.create(MomentSocialApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `me gusta con PUT y DELETE y reacciones que me marcan con yo`() = runTest {
        server.reply(204)
        server.reply(204)
        server.reply(
            200,
            """{"pestana":"me_gusta","totales":{"todos":2,"me_gusta":2,"van":0},"personas":[""" +
                """{"id":"u1","nombre":"Prueba QA","nombre_usuario":"qa","foto_url":"","relacion":"yo",""" +
                """"amigos_en_comun":0,"me_gusta":true,"va":false},""" +
                """{"id":"u2","nombre":"Ana","relacion":"amigo","amigos_en_comun":3,"me_gusta":true,"va":false}]}"""
        )

        assertTrue(repository.setLiked("m1", liked = true) is Result.Success)
        assertTrue(repository.setLiked("m1", liked = false) is Result.Success)
        val reactions = (repository.reactions("m1", ReactionTab.LIKES) as Result.Success).data

        val like = server.takeRequest()
        assertEquals("PUT", like.method)
        assertEquals("/api/v1/momentos/m1/me-gusta", like.url.encodedPath)
        assertEquals("DELETE", server.takeRequest().method)
        val list = server.takeRequest()
        assertEquals("me_gusta", list.url.queryParameter("pestana"))
        assertTrue(reactions.likedByMe)
        assertEquals(2, reactions.likes)
        assertNull("foto vacía = sin foto", reactions.people.first().photoUrl)
    }

    @Test
    fun `comentarios con respuestas, comentar, responder y borrar`() = runTest {
        server.reply(
            200,
            """{"items":[{"id":"c1","autor":{"id":"u2","nombre":"Ana","nombre_usuario":"ana"},"padre_id":null,""" +
                """"texto":"¡Qué rico!","estado":"publicado","propio":false,"puede_borrar":false,""" +
                """"creado_en":"2026-11-23T10:00:00Z","respuestas":[{"id":"c2","autor":null,"padre_id":"c1",""" +
                """"texto":"","estado":"eliminado","propio":false,"puede_borrar":false,"creado_en":"2026-11-23T10:05:00Z"}]}],""" +
                """"siguiente_cursor":"cur2"}"""
        )
        server.reply(
            201,
            """{"id":"c3","autor":{"id":"u1","nombre":"Prueba QA"},"padre_id":"c1","texto":"Sí",""" +
                """"estado":"en_moderacion","propio":true,"puede_borrar":true,"creado_en":"2026-11-23T10:10:00Z"}"""
        )
        server.reply(204)

        val page = (repository.comments("m1") as Result.Success).data
        val reply = (repository.addComment("m1", "  Sí  ", parentId = "c1") as Result.Success).data
        assertTrue(repository.deleteComment("c3") is Result.Success)

        assertEquals("20", server.takeRequest().url.queryParameter("limite"))
        assertEquals("cur2", page.nextCursor)
        val first = page.items.single()
        assertEquals("Ana", first.author?.name)
        assertEquals(CommentState.DELETED, first.replies.single().state)
        assertNull(first.replies.single().author)
        val post = server.takeRequest()
        assertEquals("/api/v1/momentos/m1/comentarios", post.url.encodedPath)
        assertEquals("""{"texto":"Sí","padre_id":"c1"}""", post.body?.utf8())
        assertEquals(CommentState.IN_MODERATION, reply.state)
        assertTrue(reply.canDelete)
        val delete = server.takeRequest()
        assertEquals("DELETE", delete.method)
        assertEquals("/api/v1/comentarios/c3", delete.url.encodedPath)
    }

    @Test
    fun `comentario rechazado por moderacion llega como error de validacion`() = runTest {
        server.reply(422, ApiDoc.error(422, "el texto no cumple las normas"))
        val result = repository.addComment("m1", "texto feo")
        assertTrue((result as Result.Failure).error is AppError.Validation)
    }

    @Test
    fun `historias por persona sin las vacias`() = runTest {
        server.reply(
            200,
            """{"personas":[{"autor":{"id":"u2","nombre_usuario":"ana","foto_url":null},"momentos":[""" +
                """{"id":"m1","imagen_url":"https://cdn/m1.jpg","negocio":{"id":"n1","nombre":"Café Tacuba"},""" +
                """"verificado":true,"creado_en":"2026-11-23T09:00:00Z"},""" +
                """{"id":"m2","imagen_url":"","negocio":null,"verificado":false,"creado_en":"2026-11-23T09:30:00Z"}]},""" +
                """{"autor":{"id":"u3","nombre_usuario":"luis"},"momentos":[]}]}"""
        )

        val stories = (repository.stories() as Result.Success).data

        assertEquals("/api/v1/momentos/historias", server.takeRequest().url.encodedPath)
        val ana = stories.single()
        assertEquals("ana", ana.username)
        assertEquals("Café Tacuba", ana.moments.first().placeName)
        assertTrue(ana.moments.first().verified)
        assertNull(ana.moments.last().imageUrl)
        assertFalse(ana.moments.last().verified)
    }
}
