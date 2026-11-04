package com.wherever.mobile.core.network

import java.util.UUID
import okhttp3.Interceptor
import okhttp3.Response

class RequestIdInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestWithId = if (request.header(REQUEST_ID_HEADER).isNullOrBlank()) {
            request.newBuilder()
                .header(REQUEST_ID_HEADER, UUID.randomUUID().toString())
                .build()
        } else {
            request
        }
        return chain.proceed(requestWithId)
    }

    companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"
    }
}
