package com.wherever.mobile.feature.auth.data

import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.database.OnboardingDao
import com.wherever.mobile.core.database.OnboardingStateEntity
import com.wherever.mobile.core.network.ApiErrorParser
import com.wherever.mobile.feature.auth.domain.OnboardingPreferences
import com.wherever.mobile.feature.auth.domain.OnboardingStep
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var dao: FakeOnboardingDao
    private val json = Json { ignoreUnknownKeys = true }
    private val dispatchers = TestDispatcherProvider()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        dao = FakeOnboardingDao()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun connectMarksCompleteWhenServerReportsOnboardingDone() = runTest {
        server.enqueue(response(200, """{"data":{"onboarding_completo":true}}"""))

        val step = repository().connect()

        assertEquals(OnboardingStep.COMPLETE, step)
        assertEquals(OnboardingStep.COMPLETE.name, dao.entity?.step)
        assertEquals("/api/v1/me", server.takeRequest().path)
    }

    @Test
    fun connectFallsBackToPreferencesWhenRemoteFails() = runTest {
        server.enqueue(response(500, """{"codigo":"servicio_no_disponible"}"""))

        val step = repository().connect()

        assertEquals(OnboardingStep.PREFERENCES, step)
    }

    @Test
    fun savePreferencesSendsSpanishPayloadAndAdvances() = runTest {
        server.enqueue(response(200, "{}"))

        val result = repository().savePreferences(
            OnboardingPreferences(
                interests = listOf("cafe"),
                vibe = "social",
                budget = "medio",
                company = "amigos"
            )
        )

        assertTrue(result is Result.Success)
        val request = server.takeRequest()
        assertEquals("/api/v1/me/preferences", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"intereses\":[\"cafe\"]"))
        assertTrue(body.contains("\"ambiente\":\"social\""))
        assertTrue(body.contains("\"presupuesto\":\"medio\""))
        assertTrue(body.contains("\"compania\":\"amigos\""))
        assertEquals(OnboardingStep.LOCATION.name, dao.entity?.step)
    }

    @Test
    fun skipPreferencesAdvancesWithoutNetwork() = runTest {
        repository().skipPreferences()

        assertEquals(OnboardingStep.LOCATION.name, dao.entity?.step)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun suggestionsParseNestedList() = runTest {
        server.enqueue(
            response(
                200,
                """
                {"data":{"sugerencias":[
                  {"usuario_id":"u-2","nombre":"Luis","amigos_en_comun":3}
                ]}}
                """.trimIndent()
            )
        )

        val result = repository().suggestions()

        val suggestions = (result as Result.Success).value
        assertEquals("u-2", suggestions.single().id)
        assertEquals("Luis", suggestions.single().name)
        assertEquals(3, suggestions.single().mutualFriends)
        assertEquals("/api/v1/friend-suggestions?limit=20", server.takeRequest().path)
    }

    @Test
    fun sendFriendRequestPostsUserId() = runTest {
        server.enqueue(response(201, "{}"))

        val result = repository().sendFriendRequest("u-2")

        assertTrue(result is Result.Success)
        val request = server.takeRequest()
        assertEquals("/api/v1/friend-requests", request.path)
        assertTrue(request.body.readUtf8().contains("\"usuario_id\":\"u-2\""))
    }

    @Test
    fun completePersistsCompleteStep() = runTest {
        server.enqueue(response(200, "{}"))

        val result = repository().complete()

        assertTrue(result is Result.Success)
        assertEquals("/api/v1/me/onboarding/complete", server.takeRequest().path)
        assertEquals(OnboardingStep.COMPLETE.name, dao.entity?.step)
    }

    private fun repository(): OnboardingRepository {
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/").toString())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(500, TimeUnit.MILLISECONDS)
                    .readTimeout(500, TimeUnit.MILLISECONDS)
                    .callTimeout(2, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return OnboardingRepository(
            remote = OnboardingRemote(
                api = retrofit.create(OnboardingApi::class.java),
                errorParser = ApiErrorParser(json)
            ),
            dao = dao,
            dispatchers = dispatchers
        )
    }

    private fun response(code: Int, body: String): MockResponse = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private class FakeOnboardingDao : OnboardingDao {
        var entity: OnboardingStateEntity? = null

        override fun observe(): Flow<OnboardingStateEntity?> = flowOf(entity)

        override suspend fun get(): OnboardingStateEntity? = entity

        override suspend fun upsert(entity: OnboardingStateEntity) {
            this.entity = entity
        }

        override suspend fun clear() {
            entity = null
        }
    }

    private class TestDispatcherProvider : DispatcherProvider {
        private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }
}
