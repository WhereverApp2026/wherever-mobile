package com.wherever.mobile.feature.profile.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.testing.testRetrofit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** nv-14 (datos): 83_Insignias con `GET /usuarios/yo/insignias` real (antes, catálogo local). */
class BadgesRepositoryImplTest {
    private val server = MockWebServer()
    private lateinit var repository: BadgesRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        repository = BadgesRepositoryImpl(testRetrofit(server.url("/")).create(BadgesApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `lee nombre, descripcion, ganada y avance`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(200).body(
                """{"insignias":[
                  {"id":"catador_cafe","nombre":"Catador de café","descripcion":"Haz check-in en 5 cafeterías",
                   "ganada":false,"ganada_en":null,"progreso":{"actual":2,"meta":5}},
                  {"id":"resenista","nombre":"Reseñista","descripcion":"Publica tu primera reseña",
                   "ganada":true,"ganada_en":"2026-11-22T18:00:00Z","progreso":{"actual":1,"meta":1}}
                ]}"""
            ).build()
        )

        val badges = (repository.badges() as Result.Success).data

        assertEquals("/usuarios/yo/insignias", server.takeRequest(5, TimeUnit.SECONDS)!!.url.encodedPath)
        assertEquals(listOf("catador_cafe", "resenista"), badges.map { it.id })
        assertEquals("Catador de café", badges[0].name)
        assertEquals(2, badges[0].current)
        assertEquals(5, badges[0].goal)
        assertTrue(badges[1].earned)
    }

    @Test
    fun `un error de la API llega como AppError`() = runTest {
        server.enqueue(MockResponse.Builder().code(503).body("{}").build())
        val result = repository.badges()
        assertTrue((result as Result.Failure).error is AppError.Server)
    }
}
