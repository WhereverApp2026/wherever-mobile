package com.wherever.mobile.core.network

import com.wherever.mobile.core.security.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation

const val HEADER_AUTHORIZATION = "Authorization"
const val BEARER_PREFIX = "Bearer "

/** Agrega el Bearer salvo en métodos con [PublicEndpoint]. Corre en un hilo de OkHttp. */
class AuthHeaderInterceptor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isPublic = request.tag(Invocation::class.java)?.method()
            ?.isAnnotationPresent(PublicEndpoint::class.java) == true
        if (isPublic) return chain.proceed(request)
        val token = runBlocking { tokenStore.current() }?.accessToken ?: return chain.proceed(request)
        return chain.proceed(request.newBuilder().header(HEADER_AUTHORIZATION, BEARER_PREFIX + token).build())
    }
}
