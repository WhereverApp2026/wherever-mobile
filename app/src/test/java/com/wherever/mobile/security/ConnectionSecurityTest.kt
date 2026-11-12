package com.wherever.mobile.security

import com.wherever.mobile.BuildConfig
import com.wherever.mobile.core.di.CONNECT_TIMEOUT_SECONDS
import com.wherever.mobile.core.di.IO_TIMEOUT_SECONDS
import com.wherever.mobile.core.di.NetworkModule
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Seguridad y configuración de la conexión (specs/nv-40, `docs/masvs-checklist.md` NETWORK):
 * URL base HTTPS en debug y release, sin hosts viejos, y tiempos de espera para datos móviles.
 */
class ConnectionSecurityTest {
    private val buildScript by lazy { File("build.gradle.kts").readText() }
    private val gradleProperties by lazy { File("../gradle.properties").readText() }

    @Test
    fun `la URL base es HTTPS y termina en api v1`() {
        assertTrue(BuildConfig.API_BASE_URL, BuildConfig.API_BASE_URL.startsWith("https://"))
        assertTrue(BuildConfig.API_BASE_URL, BuildConfig.API_BASE_URL.endsWith("/api/v1/"))
    }

    @Test
    fun `debug y release usan la URL de staging, no una vacia ni la vieja`() {
        val staging = Regex("""wherever\.stagingApiBaseUrl=(\S+)""").find(gradleProperties)!!.groupValues[1]
        assertEquals("https://d302bvc0018nfp.cloudfront.net/api/v1/", staging)
        assertEquals(
            "debug y release caen en la de staging si local.properties no la define",
            2,
            Regex("""configuredApiBaseUrl \?: stagingApiBaseUrl""").findAll(buildScript).count()
        )
        listOf("example.invalid", "sslip.io", "http://").forEach { old ->
            assertFalse(old, buildScript.contains(old) || gradleProperties.contains(old))
        }
    }

    @Test
    fun `tiempos de espera aptos para datos moviles`() {
        assertEquals(15L, CONNECT_TIMEOUT_SECONDS)
        assertEquals(30L, IO_TIMEOUT_SECONDS)
        val client = NetworkModule.provideRefreshClient()
        assertEquals(15_000, client.connectTimeoutMillis)
        assertEquals(30_000, client.readTimeoutMillis)
        assertEquals(30_000, client.writeTimeoutMillis)
    }

    @Test
    fun `no hay chequeo de conectividad que bloquee la peticion antes de intentarla`() {
        val offenders = File("src/main/java").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { file ->
                val text = file.readText()
                text.contains("ConnectivityManager") || text.contains("activeNetworkInfo")
            }
            .map { it.name }
            .toList()
        assertEquals(emptyList<String>(), offenders)
    }
}
