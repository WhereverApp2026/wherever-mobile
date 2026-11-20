package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.feature.status.data.AppStatusRepositoryImpl
import com.wherever.mobile.feature.status.data.StatusApi
import com.wherever.mobile.feature.status.domain.AppStatus
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

/** `GET /configuracion` (specs/nv-16): público, responde también en mantenimiento. */
class StatusIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private lateinit var repository: AppStatusRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val tokens = FakeTokenStore(SessionTokens("acceso", "refresh", "2026-11-04T15:15:00Z"))
        repository = AppStatusRepositoryImpl(RealNetwork(server, tokens).retrofit.create(StatusApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `lee version minima y mantenimiento sin mandar el token`() = runTest {
        server.reply(
            200,
            """{"version_minima_app":"0.8.0","version_actual_app":"1.0.0","version_terminos":"2026-10",""" +
                """"version_aviso_privacidad":"2026-11","mantenimiento":true}"""
        )

        assertEquals(Result.Success(AppStatus("0.8.0", maintenance = true)), repository.status())
        val request = server.takeRequest()
        assertEquals("/api/v1/configuracion", request.url.encodedPath)
        assertNull(request.headers["Authorization"])
    }

    @Test
    fun `503 es error de servidor, no falta de red`() = runTest {
        server.reply(503, """{"codigo":"mantenimiento","mensaje":"en mantenimiento","campos":[],"traza_id":"t"}""")

        val result = repository.status()

        assertTrue(result is Result.Failure && result.error is AppError.Server)
    }
}
