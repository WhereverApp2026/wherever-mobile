package com.wherever.mobile.core.network

import java.net.InetAddress
import java.net.UnknownHostException
import okhttp3.Dns

private const val DNS_RETRY_DELAY_MILLIS = 400L

/**
 * En el teléfono de pruebas la primera resolución a veces falla con `UnknownHostException` aunque haya
 * red (specs/nv-49). Se reintenta una vez: la petición todavía no salió, así que es seguro también
 * para `POST /auth/login`. Corre en el hilo de OkHttp, nunca en el principal.
 */
class RetryingDns(
    private val delegate: Dns = Dns.SYSTEM,
    private val attempts: Int = 2,
    private val pause: (Long) -> Unit = { Thread.sleep(it) }
) : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        var last: UnknownHostException? = null
        repeat(attempts) { attempt ->
            try {
                return delegate.lookup(hostname)
            } catch (e: UnknownHostException) {
                last = e
                if (attempt < attempts - 1) pause(DNS_RETRY_DELAY_MILLIS)
            }
        }
        throw last ?: UnknownHostException(hostname)
    }
}
