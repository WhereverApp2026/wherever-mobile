package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.places.data.PlacesApi
import com.wherever.mobile.feature.plans.data.PlanAssistantRepositoryImpl
import com.wherever.mobile.feature.plans.data.PlansApi
import com.wherever.mobile.feature.plans.data.PlansRepositoryImpl
import com.wherever.mobile.feature.plans.domain.ManualPlan
import com.wherever.mobile.feature.plans.domain.ManualStop
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.PlanRequest
import com.wherever.mobile.feature.plans.domain.PlansTab
import com.wherever.mobile.testing.ApiDoc
import com.wherever.mobile.testing.TEST_POINT
import com.wherever.mobile.testing.reply
import com.wherever.mobile.testing.sampleDraft
import com.wherever.mobile.testing.samplePlan
import com.wherever.mobile.testing.testRetrofit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

/** nv-48 y nv-24 (Data): asistente y planes contra MockWebServer (backend nv-30 a nv-33, nv-38, nv-68). */
class PlansIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private lateinit var assistant: PlanAssistantRepositoryImpl
    private lateinit var plans: PlansRepositoryImpl

    private val stops = """[{"id":"s1","negocio_id":"n1","llega":"2026-11-18T22:00:00Z","minutos":60,"precio":150},
        {"id":"s2","negocio_id":"n2","llega":"2026-11-18T23:15:00Z","minutos":45,"precio":100}]"""
    private val transfers = """[{"desde":"s1","hasta":"s2","minutos":12,"km":0.9}]"""
    private val planJson = """{"id":"plan1","nombre":"Tarde de café","fecha":"2026-11-18T00:00:00Z","personas":2,
        "presupuesto":600,"movilidad":"auto","paradas":$stops,"traslados":$transfers,"costo_total":500,
        "costo_por_persona":250,"origen":"ia","version":4,"visibilidad":"privado"}"""

    @Before
    fun setUp() {
        server.start()
        val retrofit = testRetrofit(server.url("/api/v1/"))
        assistant = PlanAssistantRepositoryImpl(retrofit.create(PlansApi::class.java))
        plans = PlansRepositoryImpl(retrofit.create(PlansApi::class.java), retrofit.create(PlacesApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    private fun place(id: String, name: String) = """{"id":"$id","nombre":"$name","categoria":"cafeteria"}"""

    @Test
    fun `generar manda frase y ubicacion y lee fuente, parametros y paradas con nombre`() = runTest {
        server.reply(
            200,
            """{"fuente":"reglas","aviso":"la IA no respondió","parametros":{"compania":"pareja","con_ninos":false,
            "con_mascota":false,"presupuesto_max":400,"hora":"20:00","palabras_clave":["cena"]},
            "plan":{"nombre":"Cena","fecha":"2026-11-18T00:00:00Z","personas":2,"presupuesto":800,"movilidad":"a_pie",
            "paradas":[{"id":"s1","negocio_id":"n1","llega":"2026-11-19T02:00:00Z","minutos":90,"precio":400,
            "nombre":"La Cocina","categoria":"restaurante","por_que":"Tranquilo"}],"traslados":[],"costo_total":800,
            "costo_por_persona":400,"origen":"reglas"}}"""
        )

        val draft = (
            assistant.generate(
                PlanRequest(" cena ", TEST_POINT, people = 2, mobility = Mobility.WALK)
            ) as Result.Success
            ).data

        val body = server.takeRequest().body?.utf8().orEmpty()
        assertTrue(body.contains("\"frase\":\"cena\""))
        assertTrue(body.contains("\"movilidad\":\"a_pie\""))
        assertTrue(!body.contains("excluir"))
        assertEquals("reglas", draft.source)
        assertEquals("2026-11-18", draft.date)
        assertEquals("La Cocina", draft.stops.single().name)
        assertEquals(400, draft.parameters?.maxBudget)
    }

    @Test
    fun `otra idea manda excluir`() = runTest {
        server.reply(200, """{"fuente":"ia","plan":{"paradas":[],"traslados":[]}}""")

        assistant.otherIdea(PlanRequest("cena", TEST_POINT), listOf("n1", "n2"))

        val request = server.takeRequest()
        assertEquals("/api/v1/planes/generar/otra-idea", request.url.encodedPath)
        assertTrue(request.body?.utf8().orEmpty().contains("\"excluir\":[\"n1\",\"n2\"]"))
    }

    @Test
    fun `guardar manda las paradas y usa los nombres del borrador sin pedir fichas`() = runTest {
        server.reply(201, planJson)

        val plan = (plans.save(sampleDraft()) as Result.Success).data

        val body = server.takeRequest().body?.utf8().orEmpty()
        assertTrue(body.contains("\"fecha\":\"2026-11-18\""))
        assertTrue(body.contains("\"origen\":\"ia\""))
        assertEquals("Lugar n1", plan.stops.first().name)
        assertEquals(Mobility.CAR, plan.mobility)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `leer un plan pide la ficha de cada parada una sola vez`() = runTest {
        server.reply(200, planJson)
        server.reply(200, place("n1", "Café Andrade"))
        server.reply(200, place("n2", "Tacos Don Gus"))
        server.reply(200, planJson)

        plans.get("plan1")
        val second = (plans.get("plan1") as Result.Success).data

        assertEquals(listOf("Café Andrade", "Tacos Don Gus"), second.stops.map { it.name })
        assertEquals(4, server.requestCount)
        assertEquals(12, second.transfers.single().minutes)
    }

    @Test
    fun `las ediciones mandan If-Match con la version`() = runTest {
        server.reply(200, planJson)
        server.reply(200, place("n1", "A"))
        server.reply(200, place("n2", "B"))
        val plan = (plans.get("plan1") as Result.Success).data
        repeat(3) { server.reply(200, planJson) }

        plans.reorder(plan, listOf("s2", "s1"))
        plans.moveStop(plan, "s1", "2026-11-18T22:15:00Z")
        plans.removeStop(plan, "s2")

        repeat(3) { server.takeRequest() }
        val reorder = server.takeRequest()
        val move = server.takeRequest()
        val remove = server.takeRequest()
        assertEquals("PUT", reorder.method)
        assertEquals("\"4\"", reorder.headers["If-Match"])
        assertEquals("""{"orden":["s2","s1"]}""", reorder.body?.utf8())
        assertEquals("/api/v1/planes/plan1/paradas/s1", move.url.encodedPath)
        assertEquals("DELETE", remove.method)
    }

    @Test
    fun `version vieja da 409 como Conflict`() = runTest {
        server.reply(200, planJson)
        server.reply(200, place("n1", "A"))
        server.reply(200, place("n2", "B"))
        val plan = (plans.get("plan1") as Result.Success).data
        server.reply(409, ApiDoc.error(409, "el plan cambió"))

        val result = plans.rename(plan, "Otro") as Result.Failure

        assertTrue(result.error is AppError.Conflict)
    }

    @Test
    fun `lista por pestana, compartir y populares`() = runTest {
        server.reply(200, """{"items":[$planJson],"siguiente_cursor":""}""")
        server.reply(
            201,
            """{"codigo":"abc","enlace":"enlace/plan#codigo=abc","grupo_id":"g1","vence_en":"2026-11-25T00:00:00Z"}"""
        )
        server.reply(
            200,
            """{"items":[{"id":"pop1","nombre":"Ruta","personas":3,"costo_por_persona":200,"veces_clonado":5,
            "paradas":[{"lugar":${place("n1", "Tacos")},"llega":"2026-11-18T20:00:00Z","minutos":30}]}]}"""
        )

        val list = (plans.list(PlansTab.PAST) as Result.Success).data
        val share = (plans.share(samplePlan()) as Result.Success).data
        val popular = (assistant.popular(TEST_POINT) as Result.Success).data

        assertEquals("pasados", server.takeRequest().url.queryParameter("pestana"))
        assertEquals(2, list.single().stops)
        assertEquals("enlace/plan#codigo=abc", share.link)
        assertEquals(listOf("Tacos"), popular.single().stops)
    }

    @Test
    fun `crear a mano manda paradas sin origen y marca publico con If-Match`() = runTest {
        server.reply(201, planJson)
        server.reply(200, planJson.replace("privado", "publico"))
        val manual = ManualPlan(
            name = "Sábado de cafés",
            date = "2026-11-19",
            people = 3,
            budget = 500,
            mobility = Mobility.WALK,
            stops = listOf(
                ManualStop("n1", "Lugar n1", "cafeteria", null, "2026-11-19T15:00:00Z", 60, 150),
                ManualStop("n2", "Lugar n2", "cafeteria", null, "2026-11-19T16:15:00Z", 60, 100)
            ),
            isPublic = true
        )

        val plan = (plans.create(manual) as Result.Success).data

        val create = server.takeRequest()
        val body = create.body!!.utf8()
        assertEquals("POST", create.method)
        assertTrue(body.contains("\"nombre\":\"Sábado de cafés\""))
        assertTrue(!body.contains("origen"))
        assertTrue(body.contains("\"llega\":\"2026-11-19T16:15:00Z\""))
        val patch = server.takeRequest()
        assertEquals("PATCH", patch.method)
        assertEquals("\"4\"", patch.headers["If-Match"])
        assertEquals("{\"visibilidad\":\"publico\"}", patch.body!!.utf8())
        assertTrue(plan.isPublic)
        assertEquals("Lugar n1", plan.stops.first().name)
    }

    @Test
    fun `alternativas con q buscan por nombre y traen la foto`() = runTest {
        server.reply(
            200,
            """{"items":[{"lugar":{"id":"n7","nombre":"Mirador","categoria":"parques","nivel_precio":0,
            "foto_portada":"https://img/mirador.jpg"},"distancia_m":650,"minutos_extra":10}]}"""
        )

        val items = (plans.alternatives(samplePlan(), "s2", " mirador ") as Result.Success).data

        assertEquals("mirador", server.takeRequest().url.queryParameter("q"))
        assertEquals("https://img/mirador.jpg", items.single().coverUrl)
        assertEquals(10, items.single().extraMinutes)
    }
}
