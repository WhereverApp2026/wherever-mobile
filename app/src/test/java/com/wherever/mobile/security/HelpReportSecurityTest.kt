package com.wherever.mobile.security

import android.content.Context
import com.wherever.mobile.core.network.ApiJson
import com.wherever.mobile.feature.help.data.HelpApi
import com.wherever.mobile.feature.help.data.HelpRepositoryImpl
import com.wherever.mobile.feature.help.domain.REPORT_MAX_LENGTH
import com.wherever.mobile.testing.testRetrofit
import io.mockk.mockk
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/** nv-33 (seguridad): el reporte se recorta a 500 y no lleva datos del perfil. */
class HelpReportSecurityTest {
    private val server = MockWebServer()
    private lateinit var repository: HelpRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        repository = HelpRepositoryImpl(
            testRetrofit(server.url("/")).create(HelpApi::class.java),
            mockk<Context>(),
            "0.7.0"
        )
        server.enqueue(MockResponse.Builder().code(202).build())
    }

    @After
    fun tearDown() = server.close()

    private fun sentBody(): JsonObject {
        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        return ApiJson.decodeFromString(JsonObject.serializer(), request.body!!.utf8())
    }

    @Test
    fun `el mensaje se recorta a 500 caracteres`() = runTest {
        repository.reportProblem("x".repeat(REPORT_MAX_LENGTH * 2))

        assertEquals(REPORT_MAX_LENGTH, sentBody()["mensaje"]!!.jsonPrimitive.content.length)
    }

    @Test
    fun `el cuerpo solo lleva modulo, mensaje y version`() = runTest {
        repository.reportProblem("La app se cerró al abrir un plan")

        assertEquals(setOf("modulo", "mensaje", "version"), sentBody().keys)
    }

    @Test
    fun `la ayuda no lee el perfil ni la sesion`() {
        val sources = File("src/main/java/com/wherever/mobile/feature/help").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .map { it.readText() }
            .toList()

        assertFalse(sources.isEmpty())
        sources.forEach { source ->
            assertFalse(source.contains("ProfileRepository"))
            assertFalse(source.contains("TokenStore"))
            assertFalse(source.contains("UserProfile"))
        }
    }
}
