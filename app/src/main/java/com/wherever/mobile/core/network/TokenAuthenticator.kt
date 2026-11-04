package com.wherever.mobile.core.network

import com.wherever.mobile.core.security.TokenStore
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val tokenStore: TokenStore, private val refresher: TokenRefresher?) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") == null || response.priorResponse != null) {
            return null
        }

        val refreshToken = tokenStore.refreshToken() ?: return null
        return when (val result = refresher?.refresh(refreshToken)) {
            is RefreshResult.Refreshed -> {
                tokenStore.save(result.tokens)
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${result.tokens.accessToken}")
                    .build()
            }

            RefreshResult.Rejected -> {
                tokenStore.clear()
                null
            }

            RefreshResult.Unavailable, null -> null
        }
    }
}
