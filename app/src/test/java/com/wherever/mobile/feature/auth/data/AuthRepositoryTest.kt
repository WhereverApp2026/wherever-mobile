package com.wherever.mobile.feature.auth.data

import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.database.OnboardingDao
import com.wherever.mobile.core.database.OnboardingStateEntity
import com.wherever.mobile.core.network.ApiErrorParser
import com.wherever.mobile.core.network.AppError
import com.wherever.mobile.core.network.RefreshResult
import com.wherever.mobile.core.security.TokenStore
import com.wherever.mobile.core.security.Tokens
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
class AuthRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var tokenStore: FakeTokenStore
    private lateinit var onboardingDao: FakeOnboardingDao
    private val json = Json { ignoreUnknownKeys = true }
    private val clave = "Clave123"
    private val dispatchers = TestDispatcherProvider()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        tokenStore = FakeTokenStore()
        onboardingDao = FakeOnboardingDao()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun loginStoresTokensAndSendsDevice() = runTest {
        server.enqueue(
            response(200, """{"data":{"access_token":"access-1","refresh_token":"refresh-1"}}""")
        )

        val result = repository().login("ana@example.com", "Clave123")

        assertTrue(result is Result.Success)
        assertEquals("access-1", tokenStore.access)
        assertEquals("refresh-1", tokenStore.refresh)
        val request = server.takeRequest()
        assertEquals("/api/v1/auth/login", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"correo\":\"ana@example.com\""))
        assertTrue(body.contains("\"dispositivo\""))
        assertTrue(body.contains("\"id\":\"device-1\""))
    }

    @Test
    fun loginMapsRateLimitAndRetryAfter() = runTest {
        server.enqueue(
            response(
                429,
                """{"codigo":"demasiadas_peticiones","mensaje":"Espera antes de continuar"}""",
                "Retry-After" to "45"
            )
        )

        val result = repository().login("ana@example.com", "Clave123")

        assertEquals(Result.Failure(AppError.RateLimited(45)), result)
    }

    @Test
    fun loginWithoutTokensReturnsUnknownError() = runTest {
        server.enqueue(response(200, """{"usuario":{"id":"u-1"}}"""))

        val result = repository().login("ana@example.com", "Clave123")

        assertTrue((result as Result.Failure).error is AppError.Unknown)
        assertEquals(null, tokenStore.access)
    }

    @Test
    fun registerUsesUsersEndpointAndRequestsVerificationWithoutTokens() = runTest {
        server.enqueue(response(201, """{"usuario":{"id":"u-1"}}"""))

        val result = repository().register(
            name = "Ana",
            birthDate = "1994-05-06",
            email = "ana@example.com",
            password = clave
        )

        assertEquals(Result.Success(RegistrationOutcome.VERIFICATION_REQUIRED), result)
        val request = server.takeRequest()
        assertEquals("/api/v1/usuarios", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"nombre\":\"Ana\""))
        assertTrue(body.contains("\"fecha_nacimiento\":\"1994-05-06\""))
        assertTrue(body.contains("\"version_terminos\""))
    }

    @Test
    fun registerSavesSessionWhenTokensAreReturned() = runTest {
        server.enqueue(
            response(201, """{"access_token":"access-2","refresh_token":"refresh-2"}""")
        )

        val result = repository().register(
            name = "Ana",
            birthDate = "1994-05-06",
            email = "ana@example.com",
            password = clave
        )

        assertEquals(Result.Success(RegistrationOutcome.SESSION_CREATED), result)
        assertEquals("access-2", tokenStore.access)
        assertEquals("refresh-2", tokenStore.refresh)
    }

    @Test
    fun sessionsParsesNestedSessionList() = runTest {
        server.enqueue(
            response(
                200,
                """
                {
                  "data": {
                    "sesiones": [
                      {
                        "id": "s-1",
                        "dispositivo": {"nombre": "Pixel 8"},
                        "creado_en": "2026-11-01T10:00:00Z",
                        "ultimo_uso": "2026-11-03T10:00:00Z",
                        "actual": true,
                        "ip": "192.0.2.1"
                      }
                    ]
                  }
                }
                """.trimIndent()
            )
        )

        val result = repository().sessions()

        val sessions = (result as Result.Success).value
        assertEquals(1, sessions.size)
        assertEquals("s-1", sessions.single().id)
        assertEquals("Pixel 8", sessions.single().deviceName)
        assertTrue(sessions.single().isCurrent)
    }

    @Test
    fun logoutAllClearsSessionEvenWhenServerRejects() = runTest {
        tokenStore.save(Tokens("access", "refresh"))
        server.enqueue(response(401, """{"codigo":"no_autenticado","mensaje":"Sesion invalida"}"""))

        val result = repository().logoutAll()

        assertTrue(result is Result.Failure)
        assertEquals(null, tokenStore.access)
    }

    @Test
    fun refreshMapsRejectedInvalidSession() {
        server.enqueue(response(401, """{"codigo":"no_autenticado","mensaje":"Sesion invalida"}"""))

        val result = RemoteTokenRefresher(api().refreshApi).refresh("refresh")

        assertEquals(RefreshResult.Rejected, result)
    }

    @Test
    fun refreshMapsServerFailureToUnavailable() {
        server.enqueue(response(503, """{"codigo":"servicio_no_disponible"}"""))

        val result = RemoteTokenRefresher(api().refreshApi).refresh("refresh")

        assertEquals(RefreshResult.Unavailable, result)
    }

    @Test
    fun refreshReturnsRotatedTokens() {
        server.enqueue(
            response(200, """{"access_token":"new-access","refresh_token":"new-refresh"}""")
        )

        val result = RemoteTokenRefresher(api().refreshApi).refresh("refresh")

        assertEquals(
            RefreshResult.Refreshed(Tokens("new-access", "new-refresh")),
            result
        )
    }

    private fun repository(): AuthRepository = AuthRepository(
        api = api().authApi,
        errorParser = ApiErrorParser(json),
        tokenStore = tokenStore,
        deviceInfoProvider = FakeDeviceInfoProvider,
        onboardingDao = onboardingDao,
        dispatchers = dispatchers
    )

    private fun api(): TestApi {
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
        return TestApi(
            authApi = retrofit.create(AuthApi::class.java),
            refreshApi = retrofit.create(RefreshApi::class.java)
        )
    }

    private fun response(code: Int, body: String, vararg headers: Pair<String, String>): MockResponse = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody(body)
        .apply { headers.forEach { (name, value) -> setHeader(name, value) } }

    private data class TestApi(val authApi: AuthApi, val refreshApi: RefreshApi)

    private class FakeOnboardingDao : OnboardingDao {
        private var entity: OnboardingStateEntity? = null

        override fun observe(): Flow<OnboardingStateEntity?> = flowOf(entity)

        override suspend fun get(): OnboardingStateEntity? = entity

        override suspend fun upsert(entity: OnboardingStateEntity) {
            this.entity = entity
        }

        override suspend fun clear() {
            entity = null
        }
    }

    private class FakeTokenStore : TokenStore {
        var access: String? = null
        var refresh: String? = null

        override fun accessToken(): String? = access
        override fun refreshToken(): String? = refresh
        override fun save(tokens: Tokens) {
            access = tokens.accessToken
            refresh = tokens.refreshToken
        }

        override fun clear() {
            access = null
            refresh = null
        }

        override fun reset() = clear()
    }

    private object FakeDeviceInfoProvider : DeviceInfoProvider {
        override val device = DeviceDto(
            id = "device-1",
            name = "Test device",
            platform = "android",
            appVersion = "1.0"
        )
    }

    private class TestDispatcherProvider : DispatcherProvider {
        private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }
}
