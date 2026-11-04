package com.wherever.mobile.core.health

import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.network.AppError
import java.time.Clock
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class HealthRepositoryTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun reportsHealthyWhenBothEndpointsSucceed() = runTest {
        server.enqueue(MockResponse().setResponseCode(200))
        server.enqueue(MockResponse().setResponseCode(200))

        val result = repository(server.url("/").toString()).check()

        assertNull(result.error)
        assertEquals(200, result.healthCode)
        assertEquals(200, result.readyCode)
    }

    @Test
    fun mapsUnavailableEndpointToServerError() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(200))

        val result = repository(server.url("/").toString()).check()

        assertEquals(AppError.Server, result.error)
        assertEquals(503, result.healthCode)
    }

    @Test
    fun mapsSocketTimeout() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val result = repository(server.url("/").toString()).check()

        assertEquals(AppError.Timeout, result.error)
    }

    @Test
    fun mapsUnreachableHostToNetworkError() = runTest {
        val result = repository("http://127.0.0.1:1/").check()

        assertEquals(AppError.Network, result.error)
        assertTrue(result.durationMillis >= 0)
    }

    private fun repository(baseUrl: String): HealthRepository {
        val client = OkHttpClient.Builder()
            .connectTimeout(200, TimeUnit.MILLISECONDS)
            .readTimeout(200, TimeUnit.MILLISECONDS)
            .callTimeout(500, TimeUnit.MILLISECONDS)
            .build()
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
        return HealthRepository(
            api = retrofit.create(HealthApi::class.java),
            dispatchers = TestDispatcherProvider(),
            clock = Clock.systemUTC()
        )
    }

    private class TestDispatcherProvider : DispatcherProvider {
        private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }
}
