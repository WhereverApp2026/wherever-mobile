package com.wherever.mobile.security

import com.wherever.mobile.core.network.RedactedLoggingInterceptor
import com.wherever.mobile.feature.plans.data.GenerateRequestDto
import com.wherever.mobile.feature.plans.data.PlansApi
import com.wherever.mobile.testing.testRetrofit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val PHRASE = "tacos, en pareja, me siento romántico"
private const val LAT = 19.432607
private const val LNG = -99.133209

/** nv-23 (seguridad): la frase, la ubicación y los gustos que usó la IA no salen en el log. */
class PlanAiSecurityTest {
    private val server = MockWebServer()
    private val logs = mutableListOf<String>()
    private lateinit var api: PlansApi

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().addInterceptor(RedactedLoggingInterceptor { logs += it }).build()
        api = testRetrofit(server.url("/"), client).create(PlansApi::class.java)
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `generar no registra la frase, la ubicacion ni los gustos`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(200).body(
                """{"fuente":"ia","aviso":null,"parametros":{"compania":"pareja","con_ninos":false,
                "con_mascota":false,"presupuesto_max":500,"hora":"09:00","palabras_clave":["tacos","cafes"]},
                "plan":{"nombre":"Viernes de tacos","fecha":"2026-10-18","personas":2,"presupuesto":500,
                "movilidad":"a_pie","paradas":[],"traslados":[],"costo_total":0,"costo_por_persona":0}}"""
            ).build()
        )

        api.generate(GenerateRequestDto(PHRASE, LAT, LNG, people = 2, mobility = "a_pie"))

        server.takeRequest(5, TimeUnit.SECONDS)
        assertTrue(logs.isNotEmpty())
        logs.forEach { line ->
            assertFalse(line.contains("tacos"))
            assertFalse(line.contains("romántico"))
            assertFalse(line.contains("cafes"))
            assertFalse(line.contains("19.43"))
            assertFalse(line.contains("99.13"))
        }
    }
}
