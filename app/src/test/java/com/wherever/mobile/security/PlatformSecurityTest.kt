package com.wherever.mobile.security

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

/**
 * Seguridad de plataforma (constitución §6, `docs/masvs-checklist.md` NETWORK y PLATFORM):
 * solo HTTPS, sin respaldo de datos sensibles y componentes no exportados salvo el launcher.
 */
class PlatformSecurityTest {
    private fun xml(path: String): Document = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()
        .parse(File(path))

    private val manifest by lazy { xml("src/main/AndroidManifest.xml") }
    private val application by lazy { manifest.getElementsByTagName("application").item(0) as Element }

    private fun Element.android(name: String): String = getAttributeNS(ANDROID_NS, name)

    @Test
    fun `sin respaldo en la nube ni copia por adb`() {
        assertEquals("false", application.android("allowBackup"))
        assertEquals("false", application.android("fullBackupContent"))
        assertEquals("@xml/data_extraction_rules", application.android("dataExtractionRules"))
    }

    @Test
    fun `las reglas de extraccion excluyen todos los dominios en nube y transferencia`() {
        val rules = xml("src/main/res/xml/data_extraction_rules.xml")
        listOf("cloud-backup", "device-transfer").forEach { section ->
            val node = rules.getElementsByTagName(section).item(0) as Element
            val excluded = (0 until node.getElementsByTagName("exclude").length)
                .map { (node.getElementsByTagName("exclude").item(it) as Element).getAttribute("domain") }
                .toSet()
            assertEquals(setOf("root", "file", "database", "sharedpref", "external"), excluded)
            assertEquals(0, node.getElementsByTagName("include").length)
        }
    }

    @Test
    fun `solo HTTPS y solo CA del sistema`() {
        assertEquals("@xml/network_security_config", application.android("networkSecurityConfig"))
        assertFalse(application.android("usesCleartextTraffic") == "true")

        val config = xml("src/main/res/xml/network_security_config.xml")
        val base = config.getElementsByTagName("base-config").item(0) as Element
        assertEquals("false", base.getAttribute("cleartextTrafficPermitted"))
        val sources = (0 until config.getElementsByTagName("certificates").length)
            .map { (config.getElementsByTagName("certificates").item(it) as Element).getAttribute("src") }
        assertEquals(listOf("system"), sources)
        assertEquals("Sin excepciones por dominio", 0, config.getElementsByTagName("domain-config").length)
        assertEquals("Sin depuración de certificados", 0, config.getElementsByTagName("debug-overrides").length)
    }

    @Test
    fun `solo la actividad launcher esta exportada`() {
        val components = listOf("activity", "service", "receiver", "provider").flatMap { tag ->
            val nodes = manifest.getElementsByTagName(tag)
            (0 until nodes.length).map { nodes.item(it) as Element }
        }
        val exported = components.filter { it.android("exported") == "true" }
        assertEquals(listOf(".MainActivity"), exported.map { it.android("name") })
        assertTrue(
            exported.single().getElementsByTagName("category").item(0).let {
                (it as Element).android("name") == "android.intent.category.LAUNCHER"
            }
        )
    }

    @Test
    fun `permisos minimos`() {
        val nodes = manifest.getElementsByTagName("uses-permission")
        val permissions = (0 until nodes.length).map { (nodes.item(it) as Element).android("name") }
        // POST_NOTIFICATIONS: push de specs/nv-46, pedido en contexto (docs/permissions-matrix.md).
        assertEquals(listOf("android.permission.INTERNET", "android.permission.POST_NOTIFICATIONS"), permissions)
    }
}
