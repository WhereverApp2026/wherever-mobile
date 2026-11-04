package com.wherever.mobile.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Registra método, ruta sin parámetros, estado y `request_id`.
 * Nunca cabeceras, parámetros ni cuerpos: ahí viajan tokens, contraseñas y correos (constitución §9).
 */
class RedactedLoggingInterceptor(private val log: (String) -> Unit) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val line = "${request.method} ${request.url.encodedPath} request_id=${request.header(HEADER_REQUEST_ID)}"
        val response = chain.proceed(request)
        log("$line -> ${response.code} server_id=${response.header(HEADER_REQUEST_ID)}")
        return response
    }
}
