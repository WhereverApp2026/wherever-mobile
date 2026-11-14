package com.wherever.mobile.feature.auth.data

import com.wherever.mobile.BuildConfig
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.network.AuthTokensDto
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.core.notifications.DeviceRegistration
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.core.security.TokenStore
import com.wherever.mobile.feature.auth.domain.ActiveSession
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.auth.domain.DeviceInfoProvider
import com.wherever.mobile.feature.auth.domain.OnboardingPreferences
import com.wherever.mobile.feature.auth.domain.PostAuthDestination
import com.wherever.mobile.feature.auth.domain.Registration
import com.wherever.mobile.feature.profile.data.UserProfileDao
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import javax.inject.Inject

@Suppress("TooManyFunctions") // Una función por endpoint de la API (docs/contexto_api_sprint1.md).
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val profileRepository: ProfileRepository,
    private val profileDao: UserProfileDao,
    private val onboarding: OnboardingPreferences,
    private val deviceInfo: DeviceInfoProvider,
    private val deviceRegistration: DeviceRegistration
) : AuthRepository {
    override suspend fun hasSession(): Boolean = tokenStore.current() != null

    override suspend fun login(email: String, password: String): Result<PostAuthDestination> = safeApiCall {
        api.login(LoginRequestDto(email.trim(), password, device()))
    }.map { tokens ->
        save(tokens)
        destinationFor(tokens)
    }

    override suspend fun isEmailAvailable(email: String): Result<Boolean> = safeApiCall {
        api.emailAvailability(email.trim()).available
    }

    override suspend fun register(registration: Registration): Result<Unit> = safeApiCall {
        api.register(
            RegisterRequestDto(
                name = registration.name.trim(),
                birthDate = registration.birthDate,
                email = registration.email.trim(),
                password = registration.password,
                acceptsTerms = registration.acceptsTerms,
                termsVersion = BuildConfig.TERMS_VERSION,
                emailPromotions = false,
                device = device()
            )
        )
    }.map { tokens ->
        save(tokens)
        profileRepository.refresh()
        Unit
    }

    override suspend fun recoverPassword(email: String): Result<Unit> = safeApiCall {
        api.recover(RecoverRequestDto(email.trim()))
    }

    override suspend fun resendVerification(): Result<Unit> = safeApiCall { api.resendVerification() }

    override suspend fun sessions(): Result<List<ActiveSession>> = safeApiCall {
        api.sessions().map {
            ActiveSession(it.id, it.device.model, it.device.system, it.lastUsed, it.isCurrent)
        }
    }

    override suspend fun closeSession(id: String): Result<Unit> = safeApiCall { api.closeSession(id) }

    override suspend fun logout() {
        // Antes del logout: quitar el teléfono necesita el JWT. Si falla, igual se cierra la sesión.
        deviceRegistration.unregister()
        safeApiCall { api.logout() }
        clearLocalSession()
    }

    override suspend fun logoutAll(): Result<Unit> {
        deviceRegistration.unregister()
        return safeApiCall { api.logoutAll() }.map { clearLocalSession() }
    }

    override suspend fun clearLocalSession() {
        tokenStore.clear()
        profileDao.clear()
        onboarding.clear()
    }

    override suspend fun skipTastes() {
        profileDao.get()?.let { onboarding.markTastesSkipped(it.id) }
    }

    /** Guarda la sesión y, sin esperar, liga el teléfono para recibir push (specs/nv-46). */
    private suspend fun save(tokens: AuthTokensDto) {
        tokenStore.save(SessionTokens(tokens.accessToken, tokens.refreshToken, tokens.expiresAt))
        deviceRegistration.registerInBackground()
    }

    /** Si el perfil no llega (sin red), entra a Inicio y lo pide después. */
    private suspend fun destinationFor(tokens: AuthTokensDto): PostAuthDestination {
        val profile = (profileRepository.refresh() as? Result.Success)?.data ?: return PostAuthDestination.HOME
        return when {
            profile.deleteAt != null -> PostAuthDestination.DELETION_PENDING
            profile.tastes == null && !onboarding.isTastesSkipped(tokens.user.id) -> PostAuthDestination.TASTES
            else -> PostAuthDestination.HOME
        }
    }

    private fun device() = deviceInfo.deviceInfo().let { DeviceDto(it.model, it.system, it.appVersion) }
}
