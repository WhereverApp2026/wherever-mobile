package com.wherever.mobile.security

import com.wherever.mobile.core.network.RedactedLoggingInterceptor
import com.wherever.mobile.feature.business.data.BusinessHomeApi
import com.wherever.mobile.feature.business.data.ContactDto
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

private const val CLIENT = "Mariana Gutiérrez"
private const val MESSAGE = "Tu mesa en terraza ya está lista"

/** nv-35 (seguridad): el nombre del cliente y el texto de Contactar nunca salen en el log. */
class BusinessClientsSecurityTest {
    private val server = MockWebServer()
    private val logs = mutableListOf<String>()
    private lateinit var api: BusinessHomeApi

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().addInterceptor(RedactedLoggingInterceptor { logs += it }).build()
        api = testRetrofit(server.url("/"), client).create(BusinessHomeApi::class.java)
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `el nombre del cliente de la lista de reservas no sale en el log`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(200).body(
                """{"fecha":"2026-10-17","items":[{"id":"r1","codigo":"WH-1","fecha_hora":"2026-10-17T23:00:00Z",
                "cliente":"$CLIENT","personas":2,"anticipo":0,"estado":"confirmada","version":1}]}"""
            ).build()
        )

        api.reservations("n1", null, null, CLIENT)

        server.takeRequest(5, TimeUnit.SECONDS)
        assertTrue(logs.isNotEmpty())
        logs.forEach { line ->
            assertFalse(line.contains("Mariana"))
            assertFalse(line.contains("Guti"))
        }
    }

    @Test
    fun `el texto de Contactar no sale en el log`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(201).body("""{"id":"m1","reserva_id":"r1","texto":"$MESSAGE"}""").build()
        )

        api.contact("n1", "r1", ContactDto(MESSAGE))

        server.takeRequest(5, TimeUnit.SECONDS)
        logs.forEach { line -> assertFalse(line.contains("terraza")) }
    }
}
