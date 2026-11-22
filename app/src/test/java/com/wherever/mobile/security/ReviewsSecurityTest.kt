package com.wherever.mobile.security

import com.wherever.mobile.core.network.ApiJson
import com.wherever.mobile.feature.reviews.data.ReviewRepositoryImpl
import com.wherever.mobile.feature.reviews.data.ReviewsApi
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.ReviewDraft
import com.wherever.mobile.testing.testRetrofit
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** nv-32 (seguridad): la reseña no manda campos que la API no pide (sin ubicación ni ids ajenos). */
class ReviewsSecurityTest {
    private val server = MockWebServer()
    private lateinit var repository: ReviewRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        repository = ReviewRepositoryImpl(testRetrofit(server.url("/")).create(ReviewsApi::class.java))
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `POST resenas solo lleva checkin, calificacion, aspectos, comentario y fotos`() = runTest {
        server.enqueue(MockResponse.Builder().code(201).body("""{"id":"r1","negocio_id":""}""").build())
        server.enqueue(MockResponse.Builder().code(404).build())

        repository.create(ReviewDraft("c1", 5, Aspect.entries.associateWith { 5 }, "Rico", listOf("i1")))

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        val body = ApiJson.decodeFromString(JsonObject.serializer(), request.body!!.utf8())
        assertEquals(setOf("checkin_id", "calificacion", "aspectos", "comentario", "fotos"), body.keys)
        listOf("lat", "lng", "ubicacion", "usuario_id", "negocio_id", "autor").forEach { assertFalse(it in body) }
    }

    @Test
    fun `sin comentario ni fotos no manda campos vacios`() = runTest {
        server.enqueue(MockResponse.Builder().code(201).body("""{"id":"r1"}""").build())

        repository.create(ReviewDraft("c1", 3, Aspect.entries.associateWith { 3 }, "   ", emptyList()))

        val body = ApiJson.decodeFromString(
            JsonObject.serializer(),
            server.takeRequest(5, TimeUnit.SECONDS)!!.body!!.utf8()
        )
        assertEquals(setOf("checkin_id", "calificacion", "aspectos"), body.keys)
    }

    @Test
    fun `las resenas no leen ubicacion ni sesion`() {
        val sources = File("src/main/java/com/wherever/mobile/feature/reviews").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .map { it.readText() }
            .toList()

        assertTrue(sources.isNotEmpty())
        sources.forEach { source ->
            assertFalse(source.contains("LocationProvider"))
            assertFalse(source.contains("TokenStore"))
            assertFalse(source.contains("ACCESS_FINE_LOCATION"))
        }
    }
}
