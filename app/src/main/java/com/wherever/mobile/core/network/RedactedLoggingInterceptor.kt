package com.wherever.mobile.core.network

import okhttp3.Interceptor
import okhttp3.Response

class RedactedLoggingInterceptor(private val logger: HttpLogger) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestId = request.header(RequestIdInterceptor.REQUEST_ID_HEADER).orEmpty()
        logger.log(
            "request ${request.method} ${HttpLogSanitizer.sanitize(request.url)} " +
                "requestId=$requestId"
        )
        val response = chain.proceed(request)
        logger.log(
            "response ${response.code} ${HttpLogSanitizer.sanitize(request.url)} " +
                "requestId=$requestId"
        )
        return response
    }
}
