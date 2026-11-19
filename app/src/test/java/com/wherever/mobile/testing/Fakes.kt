package com.wherever.mobile.testing

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.ApiJson
import com.wherever.mobile.core.notifications.DeviceRegistration
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.core.security.TokenStore
import com.wherever.mobile.feature.auth.domain.ActiveSession
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.auth.domain.PostAuthDestination
import com.wherever.mobile.feature.auth.domain.Registration
import com.wherever.mobile.feature.profile.data.UserProfileDao
import com.wherever.mobile.feature.profile.data.UserProfileEntity
import com.wherever.mobile.feature.profile.domain.CitySuggestion
import com.wherever.mobile.feature.profile.domain.NotificationSettings
import com.wherever.mobile.feature.profile.domain.Preferences
import com.wherever.mobile.feature.profile.domain.PreferencesPatch
import com.wherever.mobile.feature.profile.domain.Privacy
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.profile.domain.Tastes
import com.wherever.mobile.feature.profile.domain.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Valores de prueba: no son tokens ni contraseñas reales. */
const val TEST_PASSWORD = "Prueba2026"

fun testRetrofit(baseUrl: okhttp3.HttpUrl, client: okhttp3.OkHttpClient = okhttp3.OkHttpClient()): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(ApiJson.asConverterFactory("application/json".toMediaType()))
        .build()

/** Anota cada llamada y si había sesión en ese momento (el DELETE necesita el JWT). */
class FakeDeviceRegistration(private val tokenStore: TokenStore? = null) : DeviceRegistration {
    val calls = mutableListOf<String>()
    var unregisteredWithSession: Boolean? = null

    override suspend fun register(token: String?): Result<Unit> {
        calls += "register"
        return Result.Success(Unit)
    }

    override fun registerInBackground(token: String?) {
        calls += "registerInBackground"
    }

    override suspend fun unregister(): Result<Unit> {
        calls += "unregister"
        unregisteredWithSession = tokenStore?.current() != null
        return Result.Success(Unit)
    }
}

class FakeTokenStore(initial: SessionTokens? = null) : TokenStore {
    var tokens: SessionTokens? = initial

    override suspend fun current(): SessionTokens? = tokens

    override suspend fun save(tokens: SessionTokens) {
        this.tokens = tokens
    }

    override suspend fun clear() {
        tokens = null
    }
}

class FakeUserProfileDao : UserProfileDao {
    val state = MutableStateFlow<UserProfileEntity?>(null)

    override fun observe(): Flow<UserProfileEntity?> = state

    override suspend fun get(): UserProfileEntity? = state.value

    override suspend fun upsert(profile: UserProfileEntity) {
        state.value = profile
    }

    override suspend fun clear() {
        state.value = null
    }

    override suspend fun replace(profile: UserProfileEntity) {
        state.value = profile
    }
}

fun sampleProfile(
    version: Long = 3,
    name: String = "Ana López",
    tastes: Tastes? = null,
    deleteAt: String? = null,
    sensitiveData: Boolean = false
) = UserProfile(
    id = "u1",
    name = name,
    email = "ana@ejemplo.com",
    emailVerified = false,
    mode = "explorar",
    city = null,
    tastes = tastes,
    preferences = Preferences(
        mobility = "a_pie",
        theme = "sistema",
        units = "km",
        quietFrom = null,
        quietTo = null,
        notifications = NotificationSettings(
            reservations = true,
            coupons = true,
            groups = true,
            reviews = true,
            promotions = false
        )
    ),
    privacy = Privacy(
        location = true,
        personalizedLearning = true,
        mood = false,
        usageData = true,
        searchableByPhone = false,
        sensitiveData = sensitiveData
    ),
    points = 50,
    level = "Explorador",
    weekStreak = 0,
    deleteAt = deleteAt,
    version = version
)

/** Repositorio programable para probar ViewModels con respuestas falsas. */
class FakeProfileRepository(initial: UserProfile? = null) : ProfileRepository {
    val profile = MutableStateFlow(initial)
    var refreshResult: Result<UserProfile>? = null
    var updateProfileResult: Result<UserProfile>? = null
    var tastesResult: Result<Unit> = Result.Success(Unit)
    var preferencesResult: Result<Unit> = Result.Success(Unit)
    var privacyResult: Result<Unit> = Result.Success(Unit)
    var citiesResult: Result<List<CitySuggestion>> = Result.Success(emptyList())
    var deleteResult: Result<String> = Result.Success("2026-12-03T19:10:00Z")
    var cancelResult: Result<UserProfile> = Result.Success(sampleProfile())

    val savedTastes = mutableListOf<Tastes>()
    val preferencePatches = mutableListOf<PreferencesPatch>()
    val privacyPatches = mutableListOf<PrivacyPatch>()
    val profileUpdates = mutableListOf<Pair<String?, String?>>()
    val cityQueries = mutableListOf<String>()
    val deleteConfirmations = mutableListOf<String>()

    /** Orden de las llamadas de escritura, para probar qué ruta va primero. */
    val calls = mutableListOf<String>()

    override fun observeProfile(): Flow<UserProfile?> = profile

    override suspend fun refresh(): Result<UserProfile> {
        val result = refreshResult ?: Result.Success(profile.value ?: sampleProfile())
        if (result is Result.Success) profile.value = result.data
        return result
    }

    override suspend fun updateProfile(name: String?, cityId: String?): Result<UserProfile> {
        profileUpdates += name to cityId
        val result = updateProfileResult ?: Result.Success(sampleProfile())
        if (result is Result.Success) profile.value = result.data
        return result
    }

    override suspend fun updateTastes(tastes: Tastes): Result<Unit> {
        savedTastes += tastes
        calls += "gustos"
        return tastesResult
    }

    override suspend fun updatePreferences(patch: PreferencesPatch): Result<Unit> {
        preferencePatches += patch
        return preferencesResult
    }

    override suspend fun updatePrivacy(patch: PrivacyPatch): Result<Unit> {
        privacyPatches += patch
        calls += "privacidad"
        return privacyResult
    }

    override suspend fun searchCities(query: String): Result<List<CitySuggestion>> {
        cityQueries += query
        return citiesResult
    }

    override suspend fun deleteAccount(confirmation: String): Result<String> {
        deleteConfirmations += confirmation
        return deleteResult
    }

    override suspend fun cancelDeletion(): Result<UserProfile> = cancelResult
}

class FakeAuthRepository : AuthRepository {
    var session = false
    var loginResult: Result<PostAuthDestination> = Result.Success(PostAuthDestination.HOME)
    var emailAvailable: Result<Boolean> = Result.Success(true)
    var registerResult: Result<Unit> = Result.Success(Unit)
    var recoverResult: Result<Unit> = Result.Success(Unit)
    var resendResult: Result<Unit> = Result.Success(Unit)
    var sessionsResult: Result<List<ActiveSession>> = Result.Success(emptyList())
    var closeResult: Result<Unit> = Result.Success(Unit)
    var logoutAllResult: Result<Unit> = Result.Success(Unit)

    val recoverCalls = mutableListOf<String>()
    var resendCalls = 0
    var logoutCalls = 0
    var clearCalls = 0
    var skipCalls = 0
    val registrations = mutableListOf<Registration>()
    val emailChecks = mutableListOf<String>()

    override suspend fun hasSession(): Boolean = session

    override suspend fun login(email: String, password: String) = loginResult

    override suspend fun isEmailAvailable(email: String): Result<Boolean> {
        emailChecks += email
        return emailAvailable
    }

    override suspend fun register(registration: Registration): Result<Unit> {
        registrations += registration
        return registerResult
    }

    override suspend fun recoverPassword(email: String): Result<Unit> {
        recoverCalls += email
        return recoverResult
    }

    override suspend fun resendVerification(): Result<Unit> {
        resendCalls++
        return resendResult
    }

    override suspend fun sessions() = sessionsResult

    override suspend fun closeSession(id: String) = closeResult

    override suspend fun logout() {
        logoutCalls++
    }

    override suspend fun logoutAll() = logoutAllResult

    override suspend fun clearLocalSession() {
        clearCalls++
    }

    override suspend fun skipTastes() {
        skipCalls++
    }
}
