package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.groups.data.GroupMessagesCleaner
import com.wherever.mobile.feature.groups.data.GroupsApi
import com.wherever.mobile.feature.groups.data.GroupsRepositoryImpl
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.groups.domain.MessageStatus
import com.wherever.mobile.testing.ApiDoc
import com.wherever.mobile.testing.FakeGroupMessageDao
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.RealNetwork
import com.wherever.mobile.testing.reply
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

/** nv-25 (Data): grupos y chat con la cadena de red real (backend nv-37, nv-57) y la caché de Room. */
class GroupsIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private val dao = FakeGroupMessageDao()
    private lateinit var network: RealNetwork
    private lateinit var repository: GroupsRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        network = RealNetwork(server, FakeTokenStore(SessionTokens("vigente", "r", "2026-11-19T15:00:00Z")))
        repository = GroupsRepositoryImpl(
            network.retrofit.create(GroupsApi::class.java),
            dao,
            Clock.fixed(Instant.parse("2026-11-20T18:30:00Z"), ZoneOffset.UTC)
        )
    }

    @After
    fun tearDown() = server.close()

    private val group = """{"id":"g1","plan_id":"p1","organizador_id":"u1","mi_rol":"integrante",""" +
        """"mi_estado":"invitado","cupo":{"max":12,"ocupados":2},"resumen":{"confirmados":1,"invitados":1},""" +
        """"integrantes":[{"usuario_id":"u1","persona":{"id":"u1","nombre":"Ana","nombre_usuario":"ana",""" +
        """"foto_url":""},"rol":"organizador","estado":"confirmado","desde":"2026-11-20T10:00:00Z"}],""" +
        """"ultimo_mensaje":{"mensaje_id":"m9","texto":"¿Vamos?","autor_id":"u1","fecha":"2026-11-20T11:00:00Z"},""" +
        """"creado_en":"2026-11-20T10:00:00Z","actualizado_en":"2026-11-20T11:00:00Z"}"""

    private fun message(id: String, at: String, text: String = "hola", clientId: String? = null) =
        """{"id":"$id","autor":{"id":"u1","nombre":"Ana","foto_url":null},"tipo":"texto","texto":"$text",""" +
            """"reacciones":[],"eliminado":false,"creado_en":"$at"${clientId?.let {
                ",\"id_cliente\":\"$it\""
            } ?: ""}}"""

    @Test
    fun `lista con cursor y aceptar la invitacion`() = runTest {
        server.reply(200, """{"items":[$group],"siguiente_cursor":"c2","limite":20}""")
        server.reply(200, group.replace("\"mi_estado\":\"invitado\"", "\"mi_estado\":\"confirmado\""))

        val page = (repository.groups() as Result.Success).data
        val joined = (repository.respond("g1", accept = true) as Result.Success).data

        val list = server.takeRequest()
        assertEquals("/api/v1/grupos", list.url.encodedPath)
        assertEquals("20", list.url.queryParameter("limite"))
        assertNull(list.url.queryParameter("cursor"))
        assertEquals("c2", page.nextCursor)
        val first = page.items.single()
        assertEquals(MemberStatus.INVITED, first.myStatus)
        assertEquals("¿Vamos?", first.lastMessage?.text)
        assertNull("foto vacía = sin foto", first.members.single().photoUrl)
        val respond = server.takeRequest()
        assertEquals("PATCH", respond.method)
        assertEquals("/api/v1/grupos/g1/integrantes/yo", respond.url.encodedPath)
        assertEquals("""{"estado":"confirmado"}""", respond.body?.utf8())
        assertEquals(MemberStatus.CONFIRMED, joined.myStatus)
    }

    @Test
    fun `mensajes a Room, mas viejos con cursor y nuevos con despues_de`() = runTest {
        server.reply(
            200,
            """{"items":[${message(
                "m2",
                "2026-11-20T18:02:00Z"
            )},${message("m1", "2026-11-20T18:01:00Z")}],"siguiente_cursor":"old","limite":30}"""
        )
        server.reply(
            200,
            """{"items":[${message("m0", "2026-11-20T18:00:00Z")}],"siguiente_cursor":null,"limite":30}"""
        )
        server.reply(
            200,
            """{"items":[${message("m3", "2026-11-20T18:03:00Z")}],"siguiente_cursor":null,"limite":30}"""
        )

        assertEquals("old", (repository.refreshMessages("g1") as Result.Success).data)
        assertNull((repository.loadOlder("g1", "old") as Result.Success).data)
        assertTrue(repository.pollNew("g1") is Result.Success)

        assertNull(server.takeRequest().url.queryParameter("cursor"))
        assertEquals("old", server.takeRequest().url.queryParameter("cursor"))
        assertEquals("m2", server.takeRequest().url.queryParameter("despues_de"))
        assertEquals(listOf("m3", "m2", "m1", "m0"), repository.observeMessages("g1").first().map { it.id })
    }

    @Test
    fun `envio optimista con id_cliente, falla, reintento sin duplicar`() = runTest {
        server.reply(503, ApiDoc.error(503, "no disponible"))

        val failed = repository.send("g1", "  Llevo el coche  ")

        assertTrue(failed is Result.Failure)
        val first = server.takeRequest()
        val body = Json.parseToJsonElement(first.body!!.utf8()).jsonObject
        assertEquals("Llevo el coche", body["texto"]!!.jsonPrimitive.content)
        val clientId = body["id_cliente"]!!.jsonPrimitive.content
        assertTrue(Regex("^[0-9a-f-]{36}$").matches(clientId))
        val pending = repository.observeMessages("g1").first().single()
        assertEquals(MessageStatus.FAILED, pending.status)
        assertEquals("local:$clientId", pending.id)

        server.reply(200, message("m7", "2026-11-20T18:31:00Z", "Llevo el coche"))
        assertTrue(repository.retry("g1", clientId) is Result.Success)

        val retry = Json.parseToJsonElement(server.takeRequest().body!!.utf8()).jsonObject
        assertEquals(clientId, retry["id_cliente"]!!.jsonPrimitive.content)
        val stored = repository.observeMessages("g1").first().single()
        assertEquals("m7", stored.id)
        assertEquals(MessageStatus.SENT, stored.status)
    }

    @Test
    fun `grupo ajeno 404, limite del chat 429 y cerrar sesion borra los mensajes`() = runTest {
        server.reply(404, ApiDoc.error(404, "no encontrado"))
        server.reply(429, ApiDoc.error(429, "demasiadas"), "Retry-After" to "30")
        server.reply(
            200,
            """{"items":[${message("m1", "2026-11-20T18:01:00Z")}],"siguiente_cursor":null,"limite":30}"""
        )

        assertTrue((repository.group("g9") as Result.Failure).error is AppError.NotFound)
        assertTrue((repository.send("g1", "hola") as Result.Failure).error is AppError.RateLimited)
        repository.refreshMessages("g1")
        assertFalse(dao.rows.value.isEmpty())

        GroupMessagesCleaner(dao).clear()

        assertTrue(dao.rows.value.isEmpty())
        assertFalse(network.logs.any { it.contains("hola") })
    }
}
