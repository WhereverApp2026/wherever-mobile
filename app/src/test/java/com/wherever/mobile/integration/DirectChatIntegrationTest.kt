package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.directchat.data.DirectChatApi
import com.wherever.mobile.feature.directchat.data.DirectChatRepositoryImpl
import com.wherever.mobile.feature.directchat.domain.PlanInvite
import com.wherever.mobile.testing.ApiDoc
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

/** ChatAmigo con la cadena de red real (backend nv-87, `/conversaciones`). */
class DirectChatIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private lateinit var repository: DirectChatRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val tokens = FakeTokenStore(SessionTokens("acceso", "refresh", "2026-11-04T15:15:00Z"))
        repository = DirectChatRepositoryImpl(RealNetwork(server, tokens).retrofit.create(DirectChatApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `abre la conversacion con el amigo y lee la pagina con el plan y su invitacion`() = runTest {
        server.reply(
            201,
            """{"id":"c1","otro":{"id":"u2","nombre":"QA B","nombre_usuario":"qa_b","foto_url":""},""" +
                """"ultimo_mensaje":null,"no_leidos":0,"creada_en":"2026-11-24T14:54:34.912Z"}"""
        )
        server.reply(
            200,
            """{"items":[{"id":"m2","autor_id":"u1","texto":"¡Te invito a este plan!",""" +
                """"adjunto":{"tipo":"plan","id":"p1","nombre":"Viernes de tacos","invitacion":"aceptada"},""" +
                """"reacciones":[],"eliminado":false,"creado_en":"2026-11-24T14:55:00Z"},""" +
                """{"id":"m1","autor_id":"u2","texto":"","adjunto":null,"reacciones":[],"eliminado":true,""" +
                """"creado_en":"2026-11-24T14:54:35.353Z"}],"siguiente_cursor":"","limite":30}"""
        )

        val conversation = (repository.open("u2") as Result.Success).data
        val page = (repository.messages("c1") as Result.Success).data

        val open = server.takeRequest()
        assertEquals("/api/v1/conversaciones", open.url.encodedPath)
        assertEquals("""{"usuario_id":"u2"}""", open.body?.utf8())
        assertEquals("u2", conversation.otherId)
        assertNull("foto vacía = sin foto", conversation.otherPhoto)
        assertEquals("30", server.takeRequest().url.queryParameter("limite"))
        assertNull("cursor vacío = no hay más", page.nextCursor)
        val plan = page.items.first().attachment!!
        assertTrue(plan.isPlan)
        assertEquals(PlanInvite.ACCEPTED, plan.invite)
        assertTrue(page.items.last().deleted)
    }

    @Test
    fun `enviar con id_cliente y el plan adjunto, y lo nuevo con despues_de`() = runTest {
        server.reply(
            201,
            """{"id":"m3","autor_id":"u1","texto":"Mira","adjunto":{"tipo":"plan","id":"p1","nombre":"Tacos",""" +
                """"invitacion":"enviada"},"reacciones":[],"eliminado":false,"creado_en":"2026-11-24T15:00:00Z"}"""
        )
        server.reply(200, """{"items":[],"siguiente_cursor":"","limite":30}""")
        server.reply(204)

        val sent = (repository.send("c1", "Mira", "0f8fad5b-d9cb-469f-a165-70867728950e", "p1") as Result.Success).data
        assertTrue(repository.newer("c1", "m3") is Result.Success)
        assertTrue(repository.markRead("c1") is Result.Success)

        val post = server.takeRequest()
        assertEquals("/api/v1/conversaciones/c1/mensajes", post.url.encodedPath)
        assertEquals(
            """{"texto":"Mira","id_cliente":"0f8fad5b-d9cb-469f-a165-70867728950e",""" +
                """"adjunto":{"tipo":"plan","id":"p1"}}""",
            post.body?.utf8()
        )
        assertEquals(PlanInvite.SENT, sent.attachment?.invite)
        assertEquals("m3", server.takeRequest().url.queryParameter("despues_de"))
        assertEquals("/api/v1/conversaciones/c1/leido", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `sin amistad aceptada la API da 404`() = runTest {
        server.reply(404, ApiDoc.error(404, "persona no encontrada"))
        val result = repository.open("u9")
        assertTrue((result as Result.Failure).error is AppError.NotFound)
    }
}
