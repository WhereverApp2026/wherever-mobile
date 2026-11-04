package com.wherever.mobile.core.network

import java.util.UUID
import okhttp3.Interceptor
import okhttp3.Response

const val HEADER_REQUEST_ID = "X-Request-Id"

class RequestIdInterceptor(private val newId: () -> String = { UUID.randomUUID().toString() }) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
        chain.request().newBuilder().header(HEADER_REQUEST_ID, newId()).build()
    )
}
