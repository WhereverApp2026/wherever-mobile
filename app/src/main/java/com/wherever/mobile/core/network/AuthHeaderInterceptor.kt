package com.wherever.mobile.core.network

import com.wherever.mobile.core.security.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = tokenStore.accessToken()
        val request = if (accessToken.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request()
                .newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }
        return chain.proceed(request)
    }
}
