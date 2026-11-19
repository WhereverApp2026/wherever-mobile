package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.notifications.data.NotificationsApi
import com.wherever.mobile.feature.notifications.data.NotificationsRepositoryImpl
import com.wherever.mobile.feature.notifications.domain.NotificationType
import com.wherever.mobile.feature.profile.data.toDto
import com.wherever.mobile.feature.profile.domain.PreferencesPatch
import com.wherever.mobile.feature.profile.domain.QuietHours
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

/** nv-52 (Data): avisos con la cadena de red real (wherever-backend `docs/endpoints.md`, nv-40). */
class NotificationsIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private lateinit var repository: NotificationsRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val network = RealNetwork(server, FakeTokenStore(SessionTokens("vigente", "r", "2026-11-19T15:00:00Z")))
        repository = NotificationsRepositoryImpl(network.retrofit.create(NotificationsApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `lista con cursor, tipos conocidos y desconocidos, y solo ids de texto`() = runTest {
        server.reply(
            200,
            """{"items":[{"id":"n1","tipo":"invitacion_plan","datos":{"plan_id":"p1","grupo_id":"g1","n":3},""" +
                """"leida":false,"creada_en":"2026-11-19T08:00:00Z"},{"id":"n2","tipo":"tipo_nuevo","datos":{},""" +
                """"leida":true,"creada_en":"2026-11-18T08:00:00Z"}],"siguiente_cursor":"abc","limite":20}"""
        )

        val page = (repository.page() as Result.Success).data

        val request = server.takeRequest()
        assertEquals("/api/v1/notificaciones", request.url.encodedPath)
        assertEquals("20", request.url.queryParameter("limite"))
        assertNull(request.url.queryParameter("cursor"))
        assertEquals("abc", page.nextCursor)
        assertEquals(NotificationType.PLAN_INVITATION, page.items[0].type)
        assertEquals(mapOf("plan_id" to "p1", "grupo_id" to "g1"), page.items[0].ids)
        assertEquals(NotificationType.OTHER, page.items[1].type)
        assertTrue(page.items[1].read)
    }

    @Test
    fun `siguiente pagina manda el cursor y sin siguiente queda nulo`() = runTest {
        server.reply(200, """{"items":[],"siguiente_cursor":null,"limite":20}""")

        val page = (repository.page("abc") as Result.Success).data

        assertEquals("abc", server.takeRequest().url.queryParameter("cursor"))
        assertNull(page.nextCursor)
    }

    @Test
    fun `no leidas, marcar una, marcar todas y borrar`() = runTest {
        server.reply(200, """{"total":7}""")
        server.reply(200, """{"id":"n1","tipo":"amistad_aceptada","datos":{},"leida":true,"creada_en":"2026-11-19T08:00:00Z"}""")
        server.reply(204)
        server.reply(204)

        assertEquals(7, (repository.unreadCount() as Result.Success).data)
        assertTrue(repository.markRead("n1") is Result.Success)
        assertTrue(repository.markAllRead() is Result.Success)
        assertTrue(repository.delete("n1") is Result.Success)

        assertEquals("/api/v1/notificaciones/no-leidas", server.takeRequest().url.encodedPath)
        val patch = server.takeRequest()
        assertEquals("PATCH", patch.method)
        assertEquals("/api/v1/notificaciones/n1", patch.url.encodedPath)
        assertEquals("""{"leida":true}""", patch.body?.utf8())
        val all = server.takeRequest()
        assertEquals("POST", all.method)
        assertEquals("/api/v1/notificaciones/leer-todas", all.url.encodedPath)
        assertEquals("DELETE", server.takeRequest().method)
    }

    @Test
    fun `aviso ajeno da 404`() = runTest {
        server.reply(404, ApiDoc.error(404, "no encontrado"))

        assertTrue((repository.delete("ajeno") as Result.Failure).error is AppError.NotFound)
    }

    @Test
    fun `horario silencioso viaja como HH MM y apagado es desde igual a hasta`() {
        val on = PreferencesPatch(quietHours = QuietHours("22:00", "08:00")).toDto()
        val off = PreferencesPatch(quietHours = QuietHours.OFF).toDto()

        assertEquals("22:00", on.quietHours?.from)
        assertEquals("08:00", on.quietHours?.to)
        assertEquals(off.quietHours?.from, off.quietHours?.to)
        assertNull(on.notifications)
    }
}
