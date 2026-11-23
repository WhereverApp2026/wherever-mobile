package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.groups.data.GroupMembersRepositoryImpl
import com.wherever.mobile.feature.groups.data.GroupMessagesCleaner
import com.wherever.mobile.feature.groups.data.GroupsApi
import com.wherever.mobile.feature.groups.data.GroupsRepositoryImpl
import com.wherever.mobile.feature.groups.data.VotesRepositoryImpl
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
    private lateinit var members: GroupMembersRepositoryImpl
    private lateinit var votes: VotesRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        network = RealNetwork(server, FakeTokenStore(SessionTokens("vigente", "r", "2026-11-19T15:00:00Z")))
        repository = GroupsRepositoryImpl(
            network.retrofit.create(GroupsApi::class.java),
            dao,
            Clock.fixed(Instant.parse("2026-11-20T18:30:00Z"), ZoneOffset.UTC)
        )
        val api = network.retrofit.create(GroupsApi::class.java)
        members = GroupMembersRepositoryImpl(api, dao)
        votes = VotesRepositoryImpl(api)
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

    private val vote = """{"id":"v1","grupo_id":"g1","parada_id":"s1","opciones":[""" +
        """{"negocio_id":"n1","lugar":{"id":"n1","nombre":"Café Tacuba"},"votos":2},""" +
        """{"negocio_id":"n2","votos":1}],"total_votos":3,"mi_voto":"n1",""" +
        """"cierra_en":"2026-11-23T20:00:00Z","estado":"abierta","creada_en":"2026-11-23T10:00:00Z"}"""

    @Test
    fun `crear grupo, invitar, renombrar con If-Match, salir y sacar`() = runTest {
        server.reply(201, group.replace("\"mi_rol\":\"integrante\"", "\"mi_rol\":\"organizador\",\"version\":3"))
        server.reply(200, group)
        server.reply(200, group.replace("\"plan_id\":\"p1\"", "\"plan_id\":\"p1\",\"nombre\":\"Tacos\""))
        server.reply(204, "")
        server.reply(204, "")

        val created = (members.create("p1", listOf("u2", "u2", "u3")) as Result.Success).data
        members.invite("g1", listOf("u4"))
        val renamed = (members.rename(created, "  Tacos ") as Result.Success).data
        assertTrue(members.leave("g1") is Result.Success)
        assertTrue(members.removeMember("g1", "u4") is Result.Success)

        val create = server.takeRequest()
        assertEquals("POST", create.method)
        assertEquals("/api/v1/grupos", create.url.encodedPath)
        assertEquals("""{"plan_id":"p1","invitados":["u2","u3"]}""", create.body?.utf8())
        assertEquals(3, created.version)
        assertTrue(created.iAmOrganizer)
        val invite = server.takeRequest()
        assertEquals("/api/v1/grupos/g1/integrantes", invite.url.encodedPath)
        assertEquals("""{"usuarios":["u4"]}""", invite.body?.utf8())
        val rename = server.takeRequest()
        assertEquals("PATCH", rename.method)
        assertEquals("\"3\"", rename.headers["If-Match"])
        assertEquals("""{"nombre":"Tacos"}""", rename.body?.utf8())
        assertEquals("Tacos", renamed.name)
        val leave = server.takeRequest()
        assertEquals("DELETE", leave.method)
        assertEquals("/api/v1/grupos/g1/integrantes/yo", leave.url.encodedPath)
        assertEquals("/api/v1/grupos/g1/integrantes/u4", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `invitar a un plan usa su grupo o lo crea`() = runTest {
        server.reply(200, """{"items":[$group],"siguiente_cursor":null,"limite":20}""")
        server.reply(200, group)
        server.reply(200, """{"items":[],"siguiente_cursor":null,"limite":20}""")
        server.reply(201, group.replace("\"plan_id\":\"p1\"", "\"plan_id\":\"p2\""))

        assertTrue(members.inviteToPlan("p1", listOf("u7")) is Result.Success)
        assertTrue(members.inviteToPlan("p2", listOf("u7")) is Result.Success)

        assertEquals("/api/v1/grupos", server.takeRequest().url.encodedPath)
        assertEquals("/api/v1/grupos/g1/integrantes", server.takeRequest().url.encodedPath)
        server.takeRequest()
        val create = server.takeRequest()
        assertEquals("POST", create.method)
        assertEquals("""{"plan_id":"p2","invitados":["u7"]}""", create.body?.utf8())
    }

    @Test
    fun `votaciones, votar, cerrar y cerrada 409`() = runTest {
        server.reply(200, """{"items":[$vote]}""")
        server.reply(200, vote.replace("\"mi_voto\":\"n1\"", "\"mi_voto\":\"n2\""))
        server.reply(
            200,
            vote.replace("\"estado\":\"abierta\"", "\"estado\":\"cerrada\",\"ganadora\":\"n1\",\"aplicada\":false")
        )
        server.reply(409, ApiDoc.error(409, "votacion cerrada"))

        val list = (votes.votes("g1") as Result.Success).data
        val voted = (votes.vote("v1", "n2") as Result.Success).data
        val closed = (votes.closeVote("v1") as Result.Success).data
        val late = votes.vote("v1", "n1")

        assertEquals("/api/v1/grupos/g1/votaciones", server.takeRequest().url.encodedPath)
        val first = list.single()
        assertTrue(first.open)
        assertEquals("Café Tacuba", first.options[0].placeName)
        assertNull("opción sin lugar = no disponible", first.options[1].placeName)
        val cast = server.takeRequest()
        assertEquals("PUT", cast.method)
        assertEquals("/api/v1/votaciones/v1/voto", cast.url.encodedPath)
        assertEquals("""{"negocio_id":"n2"}""", cast.body?.utf8())
        assertEquals("n2", voted.myVote)
        assertEquals("/api/v1/votaciones/v1/cerrar", server.takeRequest().url.encodedPath)
        assertFalse(closed.open)
        assertEquals("n1", closed.winner)
        assertEquals(false, closed.applied)
        assertTrue((late as Result.Failure).error is AppError.Conflict)
    }
}
