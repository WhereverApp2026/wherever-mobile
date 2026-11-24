package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.home.data.HomeApi
import com.wherever.mobile.feature.home.data.HomeRepositoryImpl
import com.wherever.mobile.feature.home.domain.Company
import com.wherever.mobile.feature.places.data.FavoritesApi
import com.wherever.mobile.feature.places.data.FavoritesRepositoryImpl
import com.wherever.mobile.feature.places.data.PlacesApi
import com.wherever.mobile.feature.places.data.PlacesRepositoryImpl
import com.wherever.mobile.feature.places.data.ReviewsRepositoryImpl
import com.wherever.mobile.feature.places.domain.PlaceFilters
import com.wherever.mobile.feature.places.domain.ReportReason
import com.wherever.mobile.feature.places.domain.ReviewQuery
import com.wherever.mobile.feature.places.domain.SortOrder
import com.wherever.mobile.testing.ApiDoc
import com.wherever.mobile.testing.TEST_POINT
import com.wherever.mobile.testing.reply
import com.wherever.mobile.testing.testRetrofit
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

/**
 * nv-47 (Data): repositorios de Inicio y lugares contra MockWebServer con los contratos de
 * wherever-backend docs/endpoints.md (nv-21, nv-22, nv-24, nv-25, nv-54, nv-62, nv-65, nv-66, nv-67).
 */
class PlacesIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private lateinit var places: PlacesRepositoryImpl
    private lateinit var reviews: ReviewsRepositoryImpl
    private lateinit var favorites: FavoritesRepositoryImpl
    private lateinit var home: HomeRepositoryImpl

    private val card = """{"id":"p1","slug":"cafe-andrade","nombre":"Café Andrade","categoria":"cafeteria",
        "nivel_precio":2,"precio_promedio":150,"calificacion":{"promedio":4.5,"total_resenas":12},
        "foto_portada":"","ubicacion":{"lat":20.67,"lng":-103.34},"distancia_m":820.5,"abierto_ahora":true}"""

    @Before
    fun setUp() {
        server.start()
        val retrofit = testRetrofit(server.url("/api/v1/"))
        places = PlacesRepositoryImpl(retrofit.create(PlacesApi::class.java))
        reviews = ReviewsRepositoryImpl(retrofit.create(PlacesApi::class.java))
        favorites = FavoritesRepositoryImpl(retrofit.create(FavoritesApi::class.java))
        home = HomeRepositoryImpl(retrofit.create(HomeApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `inicio manda ubicacion y compania y lee clima, chips y por que`() = runTest {
        val extra = ""","puntaje":0.8,"razones":["gusto"],"por_que":"Te gustan los cafés"}"""
        val recommendation = card.dropLast(1) + extra
        server.reply(
            200,
            """{"saludo":"Buenos días","franja":"manana","momento":"2026-11-18T17:51:04Z",
            "clima":{"disponible":true,"temperatura_c":20.6,"estado":"nublado","adverso":false},
            "chips":[{"id":"cafe","nombre":"Un café","categoria":"cafeteria"}],
            "recomendaciones":[$recommendation]}"""
        )

        val feed = (home.feed(TEST_POINT, Company.FRIENDS) as Result.Success).data

        val url = server.takeRequest().url
        assertEquals("/api/v1/inicio", url.encodedPath)
        assertEquals("amigos", url.queryParameter("compania"))
        assertEquals(TEST_POINT.lat.toString(), url.queryParameter("lat"))
        assertEquals(21, feed.weather?.temperatureC)
        assertEquals("Te gustan los cafés", feed.recommendations.single().why)
        assertNull(feed.recommendations.single().coverUrl)
    }

    @Test
    fun `inicio sin ubicacion no manda lat ni lng y sin clima queda null`() = runTest {
        server.reply(200, """{"saludo":"Hola","clima":{"disponible":false},"chips":[],"recomendaciones":[]}""")

        val feed = (home.feed(null, null) as Result.Success).data

        val url = server.takeRequest().url
        assertNull(url.queryParameter("lat"))
        assertNull(url.queryParameter("compania"))
        assertNull(feed.weather)
        assertTrue(feed.recommendations.isEmpty())
    }

    @Test
    fun `no me interesa usa POST y deshacer DELETE sobre el mismo negocio`() = runTest {
        server.reply(204)
        server.reply(204)

        home.notInterested("p1")
        home.undoNotInterested("p1")

        val post = server.takeRequest()
        val delete = server.takeRequest()
        assertEquals("POST", post.method)
        assertEquals("/api/v1/recomendaciones/p1/no-me-interesa", post.url.encodedPath)
        assertEquals("DELETE", delete.method)
    }

    @Test
    fun `lista manda solo los filtros puestos y lee el cursor`() = runTest {
        server.reply(200, """{"items":[$card],"siguiente_cursor":"abc","limite":20}""")

        val page = (
            places.list(
                PlaceFilters(
                    category = "cafeteria",
                    maxPriceLevel = 2,
                    openNow = true,
                    radiusMeters = 2000,
                    order = SortOrder.RATING
                ),
                TEST_POINT,
                cursor = null
            ) as Result.Success
            ).data

        val url = server.takeRequest().url
        assertEquals("cafeteria", url.queryParameter("categoria"))
        assertEquals("2", url.queryParameter("precio_max"))
        assertEquals("true", url.queryParameter("abierto_ahora"))
        assertEquals("2000", url.queryParameter("radio_m"))
        assertEquals("calificacion", url.queryParameter("orden"))
        assertEquals("abc", page.nextCursor)
        assertEquals(820.5, page.items.single().distanceMeters ?: 0.0, 0.0)
    }

    @Test
    fun `sin ubicacion el radio no se manda y el cursor vacio es fin`() = runTest {
        server.reply(200, """{"items":[],"siguiente_cursor":"","limite":20}""")

        val page = (places.list(PlaceFilters(radiusMeters = 2000), near = null, cursor = null) as Result.Success).data

        val url = server.takeRequest().url
        assertNull(url.queryParameter("radio_m"))
        assertNull(url.queryParameter("abierto_ahora"))
        assertNull(url.queryParameter("con_sello"))
        assertNull(page.nextCursor)
    }

    @Test
    fun `con Sello manda con_sello y la tarjeta trae sello_excelencia`() = runTest {
        val sealed = card.replace("\"abierto_ahora\":true", "\"abierto_ahora\":true,\"sello_excelencia\":true")
        server.reply(200, """{"items":[$sealed],"siguiente_cursor":"","limite":20}""")

        val page = (places.list(PlaceFilters(seal = true), TEST_POINT, cursor = null) as Result.Success).data

        assertEquals("true", server.takeRequest().url.queryParameter("con_sello"))
        assertTrue(page.items.single().seal)
    }

    @Test
    fun `la ficha arma la direccion, ordena fotos y lee aspectos`() = runTest {
        server.reply(
            200,
            """{"id":"p1","nombre":"Café Andrade","categoria":"cafeteria","etiquetas":["cafe"],"descripcion":"Rico",
            "nivel_precio":2,"precio_promedio":150,"ubicacion":{"lat":20.67,"lng":-103.34},
            "direccion":{"calle":"Av. Juárez","numero":"10","colonia":"Centro","municipio":"Guadalajara","estado":"Jalisco","cp":"44100"},
            "horarios":[{"dia":1,"abre":"08:00","cierra":"20:00","cerrado":false}],
            "fotos":[{"url":"https://img/2.jpg","tipo":"local","orden":2},{"url":"https://img/1.jpg","tipo":"portada","orden":1}],
            "servicios":["wifi"],"accesibilidad":{"silla_ruedas":true,"apto_ninos":false,"pet_friendly":true},
            "opciones_dieta":["vegano"],"alergenos":[],"acepta_reservas":true,
            "calificacion":{"promedio":4.4,"total_resenas":20,"aspectos":{"sabor":4.8,"atencion":4.1,"ambiente":4.0,"espera":3.5,"precio_calidad":4.2}},
            "verificado":true,"sello_excelencia":false,"abierto_ahora":true}"""
        )

        val detail = (places.detail("p1") as Result.Success).data

        assertEquals("Av. Juárez 10, Centro, Guadalajara, Jalisco", detail.address)
        assertEquals(listOf("https://img/1.jpg", "https://img/2.jpg"), detail.photos)
        assertEquals(listOf("silla_ruedas", "pet_friendly"), detail.accessibility)
        assertEquals(4.8, detail.aspects.taste, 0.0)
        assertNull(detail.phone)
    }

    @Test
    fun `lugar no visible da 404 como NotFound`() = runTest {
        server.reply(404, ApiDoc.error(404, "no encontrado"))

        val result = places.detail("x") as Result.Failure

        assertTrue(result.error is AppError.NotFound)
    }

    @Test
    fun `resenas por estrellas y recientes con cursor`() = runTest {
        server.reply(
            200,
            """{"items":[{"id":"r1","autor":{"nombre_usuario":"ana"},"calificacion":5,"aspectos":{},"chips":["cafe"],
            "comentario":"Muy rico","fotos":[],"util":3,"respuesta_negocio":{"texto":"Gracias","fecha":"2026-11-01T00:00:00Z"},
            "creado_en":"2026-11-01T00:00:00Z"}],"siguiente_cursor":"n2","limite":10}"""
        )

        val page = (reviews.reviews("p1", ReviewQuery(stars = 5, recent = true, cursor = "c1")) as Result.Success).data

        val url = server.takeRequest().url
        assertEquals("/api/v1/lugares/p1/resenas", url.encodedPath)
        assertEquals("5", url.queryParameter("estrellas"))
        assertEquals("recientes", url.queryParameter("orden"))
        assertEquals("c1", url.queryParameter("cursor"))
        assertEquals("Gracias", page.items.single().businessReply)
        assertEquals("n2", page.nextCursor)
    }

    @Test
    fun `favorito usa PUT y DELETE con tipo negocio`() = runTest {
        server.reply(200, """{"tipo":"negocio","objetivo_id":"p1","resumen":{"nombre":"Café Andrade"}}""")
        server.reply(204)

        favorites.setFavorite("p1", true)
        favorites.setFavorite("p1", false)

        val put = server.takeRequest()
        val delete = server.takeRequest()
        assertEquals("PUT", put.method)
        assertEquals("/api/v1/favoritos/negocio/p1", put.url.encodedPath)
        assertEquals("DELETE", delete.method)
    }

    @Test
    fun `reportar manda motivo y detalle recortado`() = runTest {
        server.reply(201, """{"id":"rep1","estado":"abierto"}""")

        places.report("p1", ReportReason.OTHER, "  ya cerró  ")

        val request = server.takeRequest()
        assertEquals("/api/v1/lugares/p1/reportes", request.url.encodedPath)
        assertEquals("""{"motivo":"otro","detalle":"ya cerró"}""", request.body?.utf8())
    }

    @Test
    fun `afluencia sin datos queda como insuficiente`() = runTest {
        server.reply(200, """{"dia":3,"horas":[],"hora_actual":null,"suficiente":true}""")

        val traffic = (places.traffic("p1") as Result.Success).data

        assertEquals(false, traffic.enough)
    }

    @Test
    fun `buscar recorta la frase y la vista manda su origen`() = runTest {
        server.reply(200, """{"items":[$card],"aproximada":true}""")
        server.reply(204)

        places.search("  tacos ")
        places.registerView("p1", "busqueda")

        assertEquals("tacos", server.takeRequest().url.queryParameter("q"))
        assertEquals("""{"origen":"busqueda"}""", server.takeRequest().body?.utf8())
    }
}
