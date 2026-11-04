package com.wherever.mobile.config

import java.io.File
import java.util.Properties
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalPropertiesExampleTest {

    private val exampleFile = File(
        requireNotNull(System.getProperty("rootProjectDir")),
        "local.properties.example"
    )

    @Test
    fun exampleHasRequiredAndValidValues() {
        assertTrue("Falta local.properties.example", exampleFile.isFile)
        val properties = loadProperties()
        assertTrue(properties.getProperty("sdk.dir").orEmpty().isNotBlank())
        val baseUrl = properties.getProperty("API_BASE_URL").orEmpty()
        assertTrue(baseUrl.startsWith("https://"))
        assertTrue(baseUrl.endsWith("/"))
    }

    @Test
    fun exampleHasNoSecrets() {
        val values = loadProperties().stringPropertyNames()
            .joinToString(" ") { "$it=${loadProperties().getProperty(it)}" }
        val secretPattern = Regex(
            "(?i)(token|secret|password|api[_-]?key|private[_-]?key|figma|google-services)"
        )
        assertFalse(secretPattern.containsMatchIn(values))
    }

    @Test
    fun exampleHasNoFutureKeys() {
        val keys = loadProperties().stringPropertyNames()
        assertEquals(setOf("sdk.dir", "API_BASE_URL"), keys)
    }

    private fun loadProperties(): Properties = Properties().apply {
        exampleFile.inputStream().use { load(it) }
    }
}
