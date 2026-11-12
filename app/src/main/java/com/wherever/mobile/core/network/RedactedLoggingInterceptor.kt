package com.wherever.mobile.core.network

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Registra método, ruta sin parámetros, estado y `request_id`.
 * Nunca cabeceras, parámetros ni cuerpos: ahí viajan tokens, contraseñas y correos (constitución §9).
 * Si la petición falla sin respuesta, registra solo las clases de las excepciones (specs/nv-40):
 * es lo que distingue DNS, TLS, cleartext y timeout en Logcat.
 */
class RedactedLoggingInterceptor(private val log: (String) -> Unit) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val line = "${request.method} ${request.url.encodedPath} request_id=${request.header(HEADER_REQUEST_ID)}"
        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            val classes = e.failureChain().map { it.javaClass.name }.distinct()
            log("$line -> FAILED ${classes.joinToString(" + ")}")
            throw e
        }
        log("$line -> ${response.code} server_id=${response.header(HEADER_REQUEST_ID)}")
        return response
    }
}
