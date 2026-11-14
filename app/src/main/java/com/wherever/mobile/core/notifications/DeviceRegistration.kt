package com.wherever.mobile.core.notifications

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.di.ApplicationScope
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.core.notifications.data.DevicesApi
import com.wherever.mobile.core.notifications.data.PLATFORM_ANDROID
import com.wherever.mobile.core.notifications.data.RegisterDeviceDto
import com.wherever.mobile.core.notifications.data.UnregisterDeviceDto
import com.wherever.mobile.core.security.TokenStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Liga y desliga el teléfono de la cuenta para recibir push (specs/nv-46). */
interface DeviceRegistration {
    /** `PUT /dispositivos` con el token dado o el actual. Sin sesión o sin token no hace nada. */
    suspend fun register(token: String? = null): Result<Unit>

    /** Igual que [register], sin esperar: tras abrir sesión, al arrancar con sesión y en `onNewToken`. */
    fun registerInBackground(token: String? = null)

    /** `DELETE /dispositivos`. Va antes de cerrar sesión, porque necesita el JWT. */
    suspend fun unregister(): Result<Unit>
}

@Singleton
class DeviceRegistrationImpl @Inject constructor(
    private val api: DevicesApi,
    private val tokenStore: TokenStore,
    private val tokenSource: PushTokenSource,
    @ApplicationScope private val scope: CoroutineScope
) : DeviceRegistration {
    override suspend fun register(token: String?): Result<Unit> {
        if (tokenStore.current() == null) return Result.Success(Unit)
        val pushToken = token?.takeIf { it.isNotBlank() } ?: tokenSource.currentToken() ?: return Result.Success(Unit)
        return safeApiCall { api.register(RegisterDeviceDto(pushToken, PLATFORM_ANDROID)) }
    }

    override fun registerInBackground(token: String?) {
        scope.launch { register(token) }
    }

    override suspend fun unregister(): Result<Unit> {
        if (tokenStore.current() == null) return Result.Success(Unit)
        val pushToken = tokenSource.currentToken() ?: return Result.Success(Unit)
        return safeApiCall { api.unregister(UnregisterDeviceDto(pushToken)) }
    }
}
