package com.wherever.mobile.security

import com.wherever.mobile.BuildConfig
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Identificador y build de release (specs/nv-41): el paquete que fija Play Console vive en Gradle,
 * y la firma solo llega por variables de entorno, nunca desde el repositorio.
 */
class ReleaseConfigTest {
    private val buildScript by lazy { File("build.gradle.kts").readText() }
    private val manifest by lazy { File("src/main/AndroidManifest.xml").readText() }
    private val gitignore by lazy { File("../.gitignore").readText() }

    @Test
    fun `applicationId y namespace son com wherever mobile`() {
        assertEquals("com.wherever.mobile", BuildConfig.APPLICATION_ID)
        assertTrue(buildScript.contains("""namespace = "com.wherever.mobile""""))
        assertTrue(buildScript.contains("""applicationId = "com.wherever.mobile""""))
        assertFalse("Sin applicationIdSuffix: Play Console solo ve un id", buildScript.contains("applicationIdSuffix"))
    }

    @Test
    fun `el manifiesto no declara package`() {
        assertFalse(Regex("""<manifest[^>]*\spackage=""").containsMatchIn(manifest))
    }

    @Test
    fun `versionCode positivo y versionName semantico`() {
        assertTrue(BuildConfig.VERSION_CODE >= 1)
        assertTrue(BuildConfig.VERSION_NAME, Regex("""^\d+\.\d+\.\d+$""").matches(BuildConfig.VERSION_NAME))
    }

    @Test
    fun `la firma de release solo sale de variables de entorno`() {
        listOf("WHEREVER_KEYSTORE_PATH", "WHEREVER_KEYSTORE_PASSWORD", "WHEREVER_KEY_ALIAS", "WHEREVER_KEY_PASSWORD")
            .forEach { assertTrue(it, buildScript.contains("System.getenv(\"$it\")")) }
        assertFalse(
            "Sin contraseñas literales en el build",
            Regex("""(?i)(storePassword|keyPassword)\s*=\s*"""").containsMatchIn(buildScript)
        )
    }

    @Test
    fun `ningun keystore versionado ni versionable`() {
        assertTrue(gitignore.lines().any { it.trim() == "*.jks" })
        assertTrue(gitignore.lines().any { it.trim() == "*.keystore" })
        val keystores = File("..").walkTopDown()
            .onEnter { it.name !in setOf(".git", "build", ".gradle", ".idea", "node_modules") }
            .filter { it.isFile && it.extension in setOf("jks", "keystore", "p12") }
            .toList()
        assertEquals(emptyList<File>(), keystores)
    }

    @Test
    fun `release se minifica`() {
        val release = buildScript.substringAfter("release {").substringBefore("compileOptions")
        assertTrue(release.contains("isMinifyEnabled = true"))
        assertTrue(release.contains("isShrinkResources = true"))
    }
}
