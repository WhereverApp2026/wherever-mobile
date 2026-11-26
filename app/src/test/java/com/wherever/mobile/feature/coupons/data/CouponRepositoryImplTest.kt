package com.wherever.mobile.feature.coupons.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.coupons.domain.CouponKind
import com.wherever.mobile.feature.coupons.domain.CouponsTab
import com.wherever.mobile.testing.testRetrofit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val WALLET = """{"items":[
{"id":"w1","cupon_id":"c1","negocio_id":"p1","titulo":"2x1 en café de olla","tipo":"dos_por_uno","valor":null,
"negocio_nombre":"Café Raíz","estado":"guardado","vence_en":"2026-11-28T05:59:59Z","creado_en":"x","nuevo":1},
{"id":"w2","cupon_id":"c2","negocio_id":"p2","titulo":"$50 en tu consumo","tipo":"monto","valor":5000,
"negocio_nombre":"Fonda Doña Meche","estado":"guardado","vence_en":"2026-11-25T23:00:00Z","creado_en":"x"}],
"siguiente_cursor":"abc","limite":20}"""

/** nv-29 (datos): MockWebServer real con las respuestas del contrato de `docs/endpoints.md` (nv-61). */
class CouponRepositoryImplTest {
    private val server = MockWebServer()
    private lateinit var repository: CouponRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        repository = CouponRepositoryImpl(testRetrofit(server.url("/")).create(CouponsApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    private fun enqueue(code: Int, body: String = "") =
        server.enqueue(MockResponse.Builder().code(code).body(body).build())

    @Test
    fun `la cartera manda el filtro de la pestana y lee tipo, valor y cursor`() = runTest {
        enqueue(200, WALLET)

        val page = (repository.wallet(CouponsTab.AVAILABLE) as Result.Success).data

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("/mis-cupones", request.url.encodedPath)
        assertEquals("vigentes", request.url.queryParameter("filtro"))
        assertEquals("abc", page.nextCursor)
        assertEquals(listOf(CouponKind.TWO_FOR_ONE, CouponKind.AMOUNT), page.items.map { it.kind })
        assertEquals("$50", page.items[1].badge)
        assertEquals("2x1", page.items[0].badge)
        assertEquals("Café Raíz", repository.cached("w1")?.placeName)
    }

    @Test
    fun `usados pide filtro usados y manda el cursor`() = runTest {
        enqueue(200, """{"items":[],"siguiente_cursor":""}""")

        val page = (repository.wallet(CouponsTab.USED, "xyz") as Result.Success).data

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("usados", request.url.queryParameter("filtro"))
        assertEquals("xyz", request.url.queryParameter("cursor"))
        assertNull(page.nextCursor)
    }

    @Test
    fun `condiciones del cupon publico y 404 sin condiciones`() = runTest {
        enqueue(200, """{"id":"c1","titulo":"2x1","condiciones":"Lunes a jueves."}""")
        enqueue(404, """{"codigo":"no_encontrado","mensaje":"no existe"}""")

        assertEquals("Lunes a jueves.", (repository.conditions("c1") as Result.Success).data)
        assertNull((repository.conditions("c9") as Result.Success).data)
        assertEquals("/cupones/c1", server.takeRequest(5, TimeUnit.SECONDS)!!.url.encodedPath)
    }

    @Test
    fun `canjear manda POST con el id de la cartera y lee codigo y vencimiento`() = runTest {
        enqueue(200, """{"codigo":"K7Q2MX","qr":"wherever://canje/K7Q2MX","expira_en":"2026-11-25T18:05:00Z"}""")

        val code = (repository.redeem("w1") as Result.Success).data

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("POST", request.method)
        assertEquals("/mis-cupones/w1/canjear", request.url.encodedPath)
        assertEquals("K7Q2MX", code.code)
        assertEquals("2026-11-25T18:05:00Z", code.expiresAt)
    }

    @Test
    fun `canjear un cupon vencido da Gone y uno agotado Conflict, con el mensaje de la API`() = runTest {
        enqueue(409, """{"codigo":"vencido","mensaje":"el cupón ya venció","campos":[]}""")

        val error = (repository.redeem("w1") as Result.Failure).error

        assertTrue(error is AppError.Gone)
        assertEquals("el cupón ya venció", error.apiMessage)

        enqueue(409, """{"codigo":"agotado","mensaje":"ya no quedan","campos":[]}""")
        val soldOut = (repository.redeem("w1") as Result.Failure).error
        assertTrue(soldOut is AppError.Conflict)
        assertEquals("ya no quedan", soldOut.apiMessage)
    }
}
