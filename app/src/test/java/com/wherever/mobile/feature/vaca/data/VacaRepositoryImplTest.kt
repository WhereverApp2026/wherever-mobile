package com.wherever.mobile.feature.vaca.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.RedactedLoggingInterceptor
import com.wherever.mobile.feature.vaca.domain.DebtStatus
import com.wherever.mobile.feature.vaca.domain.ExpenseConcept
import com.wherever.mobile.feature.vaca.domain.NewExpense
import com.wherever.mobile.feature.vaca.domain.SplitMode
import com.wherever.mobile.testing.testRetrofit
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

/** nv-26 (datos y seguridad): corte, gasto personalizado, marcar pagada y nada del gasto en el log. */
class VacaRepositoryImplTest {
    private val server = MockWebServer()
    private val logs = mutableListOf<String>()
    private lateinit var repository: VacaRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().addInterceptor(RedactedLoggingInterceptor { logs += it }).build()
        repository = VacaRepositoryImpl(testRetrofit(server.url("/"), client).create(VacaApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    private fun enqueue(code: Int, body: String) = server.enqueue(MockResponse.Builder().code(code).body(body).build())

    private fun expense(mode: SplitMode) = NewExpense(
        amountCents = 48_000,
        concept = ExpenseConcept.FOOD,
        description = " Tacos del Güero ",
        paidBy = "u1",
        splitBetween = listOf("u1", "u2"),
        mode = mode,
        customCents = mapOf("u1" to 30_000L, "u2" to 18_000L),
        planId = "p1",
        clientId = "c-123"
    )

    @Test
    fun `el corte lee deudas con id o con objeto y los gastos`() = runTest {
        enqueue(
            200,
            """{"total_centavos":76000,"por_persona":[{"usuario_id":"u1","nombre":"Ana","puso":64000,"le_toca":19000,
            "saldo":45000}],"deudas":[{"id":"d1","deudor":"u2","acreedor":{"id":"u1","nombre":"Ana"},
            "monto_centavos":7000,"estado":"pagada","version_corte":3}],"version_corte":3}"""
        )
        enqueue(
            200,
            """{"items":[{"id":"e1","monto_centavos":64000,"concepto":"comida","descripcion":"la comida",
            "pagado_por":"u1"}],"siguiente":null,"total_centavos":64000}"""
        )

        val summary = (repository.summary("g1") as Result.Success).data

        val debt = summary.debts.single()
        assertEquals("u2", debt.debtorId)
        assertEquals("u1", debt.creditorId)
        assertEquals(DebtStatus.PAID, debt.status)
        assertEquals(64_000L, summary.expenses.single().amountCents)
        assertEquals("u1", summary.expenses.single().paidBy)
        assertEquals("/grupos/g1/vaca", server.takeRequest(5, TimeUnit.SECONDS)!!.url.encodedPath)
    }

    @Test
    fun `gasto personalizado manda montos por persona en centavos e id_cliente`() = runTest {
        enqueue(201, """{"gasto":{"id":"e9"},"corte":{}}""")

        repository.addExpense("g1", expense(SplitMode.CUSTOM))

        val body = server.takeRequest(5, TimeUnit.SECONDS)!!.body!!.utf8()
        assertTrue(body.contains("\"monto_centavos\":48000"))
        assertTrue(body.contains("\"division\":\"personalizada\""))
        assertTrue(body.contains("{\"usuario_id\":\"u1\",\"monto_centavos\":30000}"))
        assertTrue(body.contains("\"descripcion\":\"Tacos del Güero\""))
        assertTrue(body.contains("\"id_cliente\":\"c-123\""))
    }

    @Test
    fun `partes iguales no manda montos personalizados`() = runTest {
        enqueue(201, """{"gasto":{"id":"e9"},"corte":{}}""")

        repository.addExpense("g1", expense(SplitMode.EQUAL))

        val body = server.takeRequest(5, TimeUnit.SECONDS)!!.body!!.utf8()
        assertFalse(body.contains("montos_personalizados"))
        assertTrue(body.contains("\"division\":\"igual\""))
    }

    @Test
    fun `marcar pagada manda el estado y 409 es conflicto`() = runTest {
        enqueue(200, """{"id":"d1","estado":"pagada"}""")
        enqueue(409, """{"codigo":"ya_pagada","mensaje":"ya pagada","campos":[],"traza_id":"t"}""")

        repository.markPaid("d1")
        val again = repository.markPaid("d1")

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("PATCH", request.method)
        assertEquals("/vaca/deudas/d1", request.url.encodedPath)
        assertEquals("""{"estado":"pagada"}""", request.body!!.utf8())
        assertTrue((again as Result.Failure).error is AppError.Conflict)
    }

    @Test
    fun `el gasto no lleva datos bancarios y su cuerpo no sale en el log`() = runTest {
        enqueue(201, """{"gasto":{"id":"e9"},"corte":{}}""")

        repository.addExpense("g1", expense(SplitMode.EQUAL))

        val body = server.takeRequest(5, TimeUnit.SECONDS)!!.body!!.utf8()
        listOf("clabe", "tarjeta", "cuenta", "banco").forEach { assertFalse(body.contains(it)) }
        assertTrue(logs.isNotEmpty())
        logs.forEach { line ->
            assertFalse(line.contains("Güero"))
            assertFalse(line.contains("48000"))
        }
    }
}
