package com.wherever.mobile.security

import com.wherever.mobile.navigation.CheckEmailRoute
import com.wherever.mobile.navigation.TastesRoute
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Código fuente sin secretos ni fugas (constitución §6, `docs/masvs-checklist.md` STORAGE, CODE y NETWORK).
 * Complementa a `scripts/critic-guardrails.sh` y gitleaks en CI.
 */
class SourceSecurityTest {
    private val sources: List<File> by lazy {
        File("src/main").walkTopDown().filter { it.isFile && it.extension in setOf("kt", "xml") }.toList()
    }

    private fun offenders(pattern: Regex, files: List<File> = sources, codeOnly: Boolean = false): List<String> =
        files.flatMap { file ->
            file.readLines().mapIndexedNotNull { index, line ->
                val comment = line.trimStart().let { it.startsWith("//") || it.startsWith("*") || it.startsWith("/*") }
                "${file.path}:${index + 1}".takeIf { pattern.containsMatchIn(line) && !(codeOnly && comment) }
            }
        }

    @Test
    fun `sin llaves ni credenciales en el codigo`() {
        val secret = Regex(
            """AIza[0-9A-Za-z_\-]{30,}|ghp_[0-9A-Za-z]{30,}|AKIA[0-9A-Z]{16}|-----BEGIN [A-Z ]*PRIVATE KEY|""" +
                """(?i)(password|api_?key|secret)\s*=\s*"[^"]{6,}""""
        )
        assertEquals(emptyList<String>(), offenders(secret))
    }

    @Test
    fun `sin URLs http en claro ni URL base fija`() {
        assertEquals(emptyList<String>(), offenders(Regex("""http://(?!schemas\.android\.com)""")))
        assertEquals(emptyList<String>(), offenders(Regex("""sslip\.io|https://[\w.-]+/api/v1""")))
    }

    @Test
    fun `los tokens solo se guardan cifrados en core security`() {
        val prefsUsers = sources.filter { it.readText().contains("getSharedPreferences") }
        assertEquals(listOf("SharedOnboardingPreferences.kt"), prefsUsers.map { it.name })
        assertEquals(
            "SharedPreferences solo guarda marcas booleanas",
            emptyList<String>(),
            offenders(Regex("""putString|putStringSet|(?i)token"""), prefsUsers, codeOnly = true)
        )
        val wiring = File("src/main/java/com/wherever/mobile/core/di/SecurityModule.kt").readText()
        assertTrue(
            "El TokenStore cifra con Keystore",
            wiring.contains("EncryptedTokensSerializer(KeystoreTokenCipher())")
        )
    }

    @Test
    fun `no se escribe en logs fuera del interceptor redactado`() {
        val logCalls = offenders(Regex("""\bLog\.[vdiwe]\(|println\(|printStackTrace\("""))
        assertEquals(
            listOf("NetworkModule.kt"),
            logCalls.map { File(it.substringBeforeLast(':')).name }.distinct()
        )
    }

    @Test
    fun `las rutas de navegacion no llevan tokens ni contrasenas`() {
        val routeFields = listOf(CheckEmailRoute::class.java, TastesRoute::class.java)
            .flatMap { route -> route.declaredFields.filterNot { it.isSynthetic }.map { it.name.lowercase() } }
        val sensitive = Regex("token|password|contrasena|refresh|secret")
        assertTrue(routeFields.isNotEmpty())
        assertEquals(emptyList<String>(), routeFields.filter { sensitive.containsMatchIn(it) && it != "companion" })
        val routes = File("src/main/java/com/wherever/mobile/navigation/Routes.kt").readText()
        assertTrue("Ninguna ruta declara un token", !Regex("(?i)val \\w*(token|password)").containsMatchIn(routes))
    }
}
