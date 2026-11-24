package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.friends.data.FriendsApi
import com.wherever.mobile.feature.friends.data.FriendsRepositoryImpl
import com.wherever.mobile.feature.friends.domain.FriendQr
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.Relation
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

/** nv-53 (Data): personas y amistades con la cadena de red real (backend nv-35, nv-36, nv-53, nv-64, nv-70). */
class FriendsIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private lateinit var repository: FriendsRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val network = RealNetwork(server, FakeTokenStore(SessionTokens("vigente", "r", "2026-11-19T15:00:00Z")))
        repository = FriendsRepositoryImpl(network.retrofit.create(FriendsApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    private val ana = """{"id":"u1","nombre":"Ana","nombre_usuario":"ana_l","foto_url":null}"""

    @Test
    fun `lista recibidas con pendientes y sin q vacio`() = runTest {
        server.reply(
            200,
            """{"resultados":[{"id":"f1","estado":"pendiente","direccion":"recibida","persona":$ana,""" +
                """"silenciado":false,"creada_en":"2026-11-19T08:00:00Z","actualizada_en":"2026-11-19T08:00:00Z"}],""" +
                """"pendientes":1}"""
        )

        val page = (repository.list(FriendshipList.RECEIVED, "  ") as Result.Success).data

        val request = server.takeRequest()
        assertEquals("/api/v1/amistades", request.url.encodedPath)
        assertEquals("recibidas", request.url.queryParameter("estado"))
        assertNull(request.url.queryParameter("q"))
        assertEquals(1, page.pending)
        assertTrue(page.items.single().received)
        assertFalse(page.items.single().accepted)
        assertEquals("ana_l", page.items.single().person.username)
    }

    @Test
    fun `buscar, sugerencias y codigo en mayusculas`() = runTest {
        server.reply(200, """{"resultados":[$ana]}""")
        server.reply(200, """{"resultados":[{"persona":$ana,"amigos_en_comun":2}],"degradado":false}""")
        server.reply(
            200,
            """{"id":"u1","nombre":"Ana","nombre_usuario":"ana_l","foto_url":null,"relacion":"ninguna"}"""
        )

        assertEquals("u1", (repository.search(" ana ") as Result.Success).data.single().id)
        assertEquals(2, (repository.suggestions() as Result.Success).data.single().mutualFriends)
        val found = (repository.byCode("k7m2p9qx") as Result.Success).data

        assertEquals("ana", server.takeRequest().url.queryParameter("q"))
        assertEquals("/api/v1/amistades/sugerencias", server.takeRequest().url.encodedPath)
        assertEquals("/api/v1/personas/codigo/K7M2P9QX", server.takeRequest().url.encodedPath)
        assertEquals(Relation.NONE, found.relation)
    }

    @Test
    fun `perfil de amigo con datos de amigo y degradado sin amigos en comun`() = runTest {
        server.reply(
            200,
            """{"id":"u1","nombre":"Ana","nombre_usuario":"ana_l","foto_url":"","relacion":"amigo",""" +
                """"nivel":"Explorador","amigos_en_comun":null,"grupos_en_comun":2,""" +
                """"silenciado":false,"degradado":true}"""
        )

        val detail = (repository.person("u1") as Result.Success).data

        assertEquals(Relation.FRIEND, detail.relation)
        assertEquals("Explorador", detail.level)
        assertNull(detail.mutualFriends)
        assertEquals(2, detail.mutualGroups)
        assertNull(detail.person.photoUrl)
    }

    @Test
    fun `perfil de amigo con nivel como objeto, como lo manda staging hoy`() = runTest {
        server.reply(
            200,
            """{"id":"u1","nombre":"QA B","nombre_usuario":"qa_b","foto_url":"",""" +
                """"descripcion":"","relacion":"amigo","nivel":{"numero":2,"nombre":"Explorador"},""" +
                """"contadores":{"planes":0,"amigos":1,"resenas":1},"insignias_total":1,""" +
                """"insignias_destacadas":[{"id":"resenista","nombre":"Reseñista"}],""" +
                """"amigos_en_comun":0,"grupos_en_comun":3,"silenciado":false,"degradado":false}"""
        )

        val detail = (repository.person("u1") as Result.Success).data

        assertEquals("Explorador", detail.level)
        assertEquals(3, detail.mutualGroups)
    }

    @Test
    fun `silenciar manda PUT con la marca y devuelve como quedo`() = runTest {
        server.reply(200, """{"id":"f1","silenciado":true}""")

        assertEquals(true, (repository.setMuted("f1", muted = true) as Result.Success).data)

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/amistades/f1/silencio", request.url.encodedPath)
        assertEquals("""{"silenciado":true}""", request.body?.utf8())
    }

    @Test
    fun `solicitar, aceptar, eliminar y bloquear`() = runTest {
        server.reply(
            201,
            """{"id":"f1","estado":"aceptada","direccion":"enviada","persona":$ana,""" +
                """"creada_en":"2026-11-19T08:00:00Z","actualizada_en":"2026-11-19T08:00:00Z"}"""
        )
        server.reply(
            200,
            """{"id":"f2","estado":"aceptada","direccion":"recibida","persona":$ana,""" +
                """"creada_en":"2026-11-19T08:00:00Z","actualizada_en":"2026-11-19T08:00:00Z"}"""
        )
        server.reply(204)
        server.reply(204)

        // Si la otra persona ya me había mandado una, quedan como amigos al instante.
        assertTrue((repository.request("u1") as Result.Success).data.accepted)
        assertTrue(repository.respond("f2", accept = true) is Result.Success)
        assertTrue(repository.remove("f1") is Result.Success)
        assertTrue(repository.block("f1") is Result.Success)

        assertEquals("""{"usuario_id":"u1"}""", server.takeRequest().body?.utf8())
        val respond = server.takeRequest()
        assertEquals("PATCH", respond.method)
        assertEquals("""{"accion":"aceptar"}""", respond.body?.utf8())
        assertEquals("DELETE", server.takeRequest().method)
        assertEquals("/api/v1/amistades/f1/bloquear", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `solicitud repetida da 409 y codigo inexistente 404`() = runTest {
        server.reply(409, ApiDoc.error(409, "ya existe"))
        server.reply(404, ApiDoc.error(404, "no encontrado"))

        assertTrue((repository.request("u1") as Result.Failure).error is AppError.Conflict)
        assertTrue((repository.byCode("K7M2P9QX") as Result.Failure).error is AppError.NotFound)
    }

    @Test
    fun `QR escaneado viaja solo como codigo a personas codigo`() = runTest {
        server.reply(
            200,
            """{"id":"u1","nombre":"Ana","nombre_usuario":"ana_l","foto_url":null,"relacion":"ninguna"}"""
        )

        val code = checkNotNull(FriendQr.parse(" wherever:amigo:k7m2p9qx "))
        val found = (repository.byCode(code) as Result.Success).data

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/v1/personas/codigo/K7M2P9QX", request.url.encodedPath)
        assertNull(request.url.query)
        assertEquals("u1", found.person.id)
        assertEquals(1, server.requestCount)
    }
}
