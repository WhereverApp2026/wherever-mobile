package com.wherever.mobile.feature.business.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.RedactedLoggingInterceptor
import com.wherever.mobile.feature.business.domain.CapacityConfig
import com.wherever.mobile.feature.business.domain.CouponType
import com.wherever.mobile.feature.business.domain.NewCoupon
import com.wherever.mobile.testing.sampleCoupon
import com.wherever.mobile.testing.testRetrofit
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** nv-37 (datos y seguridad): POST cupones, PATCH y PUT con If-Match y nada del cupón en el log. */
class BusinessCouponsRepositoryImplTest {
    private val server = MockWebServer()
    private val logs = mutableListOf<String>()
    private lateinit var repository: BusinessCouponsRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().addInterceptor(RedactedLoggingInterceptor { logs += it }).build()
        repository = BusinessCouponsRepositoryImpl(
            testRetrofit(server.url("/"), client).create(BusinessCouponsApi::class.java)
        )
    }

    @After
    fun tearDown() = server.close()

    private fun enqueue(code: Int, body: String) = server.enqueue(MockResponse.Builder().code(code).body(body).build())

    private val couponJson = """{"id":"c9","titulo":"Café secreto","tipo":"monto","valor":5000,"estado":"activo",
        "limite_usos":50,"usos_actuales":0,"version":1}"""

    private fun newCoupon(type: CouponType, value: Long?) = NewCoupon(
        title = " Café secreto ",
        type = type,
        value = value,
        conditions = null,
        start = OffsetDateTime.parse("2026-10-17T12:00:00-06:00"),
        end = OffsetDateTime.parse("2026-11-16T23:59:59-06:00"),
        days = listOf(4, 1),
        hoursFrom = LocalTime.of(12, 0),
        hoursTo = LocalTime.of(19, 0),
        usageLimit = 50
    )

    @Test
    fun `crear manda monto en centavos, dias ordenados y horas`() = runTest {
        enqueue(201, couponJson)

        val coupon = (repository.create("n1", newCoupon(CouponType.AMOUNT, 5_000)) as Result.Success).data

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        val body = request.body!!.utf8()
        assertEquals("/negocios/n1/cupones", request.url.encodedPath)
        assertTrue(body.contains("\"titulo\":\"Café secreto\""))
        assertTrue(body.contains("\"tipo\":\"monto\""))
        assertTrue(body.contains("\"valor\":5000"))
        assertTrue(body.contains("\"dias_validos\":[1,4]"))
        assertTrue(body.contains("\"horas_validas\":{\"desde\":\"12:00\",\"hasta\":\"19:00\"}"))
        assertTrue(body.contains("\"vigencia_fin\":\"2026-11-16T23:59:59-06:00\""))
        assertEquals(50, coupon.usageLimit)
    }

    @Test
    fun `2x1 no manda valor`() = runTest {
        enqueue(201, couponJson)

        repository.create("n1", newCoupon(CouponType.TWO_FOR_ONE, 5_000))

        assertFalse(server.takeRequest(5, TimeUnit.SECONDS)!!.body!!.utf8().contains("valor"))
    }

    @Test
    fun `pausar usa If-Match y 409 con canjes es conflicto`() = runTest {
        enqueue(200, couponJson.replace("activo", "pausado"))
        enqueue(409, """{"codigo":"con_canjes","mensaje":"con canjes","campos":[],"traza_id":"t"}""")

        repository.setActive("n1", sampleCoupon(version = 7), active = false)
        val again = repository.setActive("n1", sampleCoupon(version = 7), active = true)

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("PATCH", request.method)
        assertEquals("\"7\"", request.headers["If-Match"])
        assertEquals("""{"estado":"pausado"}""", request.body!!.utf8())
        assertTrue((again as Result.Failure).error is AppError.Conflict)
    }

    @Test
    fun `guardar el cupo conserva los demas campos y manda If-Match`() = runTest {
        enqueue(
            200,
            """{"items":[{"id":"n1","version":3,"reservas_config":{"acepta_reservas":true,"capacidad_por_horario":12,
            "duracion_turno_min":60,"anticipo":{"tipo":"fijo","valor":0},"politica_cancelacion":"flexible",
            "confirmacion":"automatica","permite_cambios":true}}]}"""
        )
        enqueue(
            200,
            """{"reservas_config":{"capacidad_por_horario":20,"duracion_turno_min":45},"version":4}"""
        )
        enqueue(422, """{"codigo":"cobros_no_activos","mensaje":"activa tus cobros","campos":[],"traza_id":"t"}""")

        val config = (repository.capacity("n1") as Result.Success).data
        val saved = (repository.saveCapacity("n1", CapacityConfig(20, 45, config.version)) as Result.Success).data
        val refused = repository.saveCapacity("n1", saved)

        server.takeRequest(5, TimeUnit.SECONDS)
        val put = server.takeRequest(5, TimeUnit.SECONDS)!!
        val body = put.body!!.utf8()
        assertEquals("PUT", put.method)
        assertEquals("\"3\"", put.headers["If-Match"])
        assertTrue(body.contains("\"capacidad_por_horario\":20"))
        assertTrue(body.contains("\"duracion_turno_min\":45"))
        assertTrue(body.contains("\"politica_cancelacion\":\"flexible\""))
        assertEquals(4L, saved.version)
        assertTrue((refused as Result.Failure).error is AppError.Validation)
    }

    @Test
    fun `sin reservas configuradas es NotFound`() = runTest {
        enqueue(200, """{"items":[{"id":"n1","version":3}]}""")

        assertTrue((repository.capacity("n1") as Result.Failure).error is AppError.NotFound)
    }

    @Test
    fun `el titulo y las condiciones del cupon no salen en el log`() = runTest {
        enqueue(201, couponJson)

        repository.create("n1", newCoupon(CouponType.AMOUNT, 5_000))

        server.takeRequest(5, TimeUnit.SECONDS)
        assertTrue(logs.isNotEmpty())
        logs.forEach { assertFalse(it.contains("Café secreto")) }
    }
}
