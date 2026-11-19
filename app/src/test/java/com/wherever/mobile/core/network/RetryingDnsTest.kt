package com.wherever.mobile.core.network

import java.net.InetAddress
import java.net.UnknownHostException
import okhttp3.Dns
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** nv-49: un fallo de DNS se reintenta una vez antes de llegar a la UI como "Sin conexión". */
class RetryingDnsTest {
    private val address = InetAddress.getByAddress("api", byteArrayOf(10, 0, 0, 1))

    private class FlakyDns(private val failures: Int, private val answer: List<InetAddress>) : Dns {
        var calls = 0

        override fun lookup(hostname: String): List<InetAddress> {
            calls++
            if (calls <= failures) throw UnknownHostException(hostname)
            return answer
        }
    }

    @Test
    fun `un fallo seguido de exito resuelve`() {
        val flaky = FlakyDns(failures = 1, answer = listOf(address))
        val pauses = mutableListOf<Long>()

        val result = RetryingDns(flaky) { pauses += it }.lookup("api")

        assertEquals(listOf(address), result)
        assertEquals(2, flaky.calls)
        assertEquals(1, pauses.size)
    }

    @Test
    fun `dos fallos llegan como UnknownHostException`() {
        val flaky = FlakyDns(failures = 5, answer = emptyList())

        assertThrows(UnknownHostException::class.java) { RetryingDns(flaky) { }.lookup("api") }
        assertEquals(2, flaky.calls)
    }

    @Test
    fun `sin fallos no espera`() {
        val pauses = mutableListOf<Long>()

        RetryingDns(FlakyDns(0, listOf(address))) { pauses += it }.lookup("api")

        assertEquals(emptyList<Long>(), pauses)
    }
}
