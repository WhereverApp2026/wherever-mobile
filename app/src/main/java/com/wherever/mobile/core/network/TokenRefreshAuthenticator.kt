package com.wherever.mobile.core.network

import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.core.security.TokenStore
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_UNPROCESSABLE = 422

/**
 * Ante un 401 renueva una sola vez y repite la petición.
 *
 * La API rota el refresh y reusar uno viejo cierra todas las sesiones, así que el lock es obligatorio:
 * quien espera compara su token con el actual y reutiliza el nuevo. Ver specs/000-arquitectura §3.2.
 */
class TokenRefreshAuthenticator(
    private val tokenStore: TokenStore,
    private val refreshApi: RefreshApi,
    private val sessionEvents: SessionEvents
) : Authenticator {
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponse != null) return null
        val sent = response.request.header(HEADER_AUTHORIZATION)?.removePrefix(BEARER_PREFIX)
        val fresh = sent?.let { synchronized(lock) { freshAccessToken(it) } }
        return fresh?.let {
            response.request.newBuilder().header(HEADER_AUTHORIZATION, BEARER_PREFIX + it).build()
        }
    }

    private fun freshAccessToken(sent: String): String? {
        val current = runBlocking { tokenStore.current() } ?: return null
        return if (current.accessToken != sent) current.accessToken else refresh(current)
    }

    private fun refresh(current: SessionTokens): String? {
        val result = try {
            refreshApi.refresh(RefreshRequestDto(current.refreshToken)).execute()
        } catch (_: IOException) {
            return null
        }
        val body = result.body()
        return when {
            result.isSuccessful && body != null -> {
                val rotated = SessionTokens(body.accessToken, body.refreshToken, body.expiresAt)
                runBlocking { tokenStore.save(rotated) }
                rotated.accessToken
            }
            result.code() == HTTP_UNAUTHORIZED || result.code() == HTTP_UNPROCESSABLE -> {
                runBlocking { tokenStore.clear() }
                sessionEvents.notifySessionExpired()
                null
            }
            // 429, 503 o similar: fallo transitorio, se conserva la sesión (duda D16).
            else -> null
        }
    }
}
