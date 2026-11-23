package com.wherever.mobile.security

import com.wherever.mobile.core.security.DeviceSignals
import com.wherever.mobile.core.security.IntegrityAction
import com.wherever.mobile.core.security.IntegrityPolicy
import com.wherever.mobile.core.security.IntegrityRisk
import org.junit.Assert.assertEquals
import org.junit.Test

/** MASVS-RESILIENCE: root, depurador y re-empaquetado; en debug avisa, en release bloquea. */
class IntegrityPolicyTest {
    private val clean = DeviceSignals(
        suPresent = false,
        testKeys = false,
        debuggerAttached = false,
        appDebuggable = false,
        signingCertSha256 = "AB:CD:EF"
    )

    @Test
    fun `telefono limpio no hace nada`() {
        val verdict = IntegrityPolicy.evaluate(clean, debugBuild = false, expectedCertSha256 = "abcdef")
        assertEquals(IntegrityAction.NONE, verdict.action)
        assertEquals(emptySet<IntegrityRisk>(), verdict.risks)
    }

    @Test
    fun `root y depurador avisan en debug y bloquean en release`() {
        val rooted = clean.copy(suPresent = true, debuggerAttached = true)
        assertEquals(
            IntegrityAction.WARN,
            IntegrityPolicy.evaluate(rooted, debugBuild = true, expectedCertSha256 = "").action
        )
        val release = IntegrityPolicy.evaluate(rooted, debugBuild = false, expectedCertSha256 = "")
        assertEquals(IntegrityAction.BLOCK, release.action)
        assertEquals(setOf(IntegrityRisk.ROOTED, IntegrityRisk.DEBUGGER), release.risks)
    }

    @Test
    fun `release depurable o con otra firma se toma por alterada`() {
        val repackaged = clean.copy(appDebuggable = true, signingCertSha256 = "00:11")
        val verdict = IntegrityPolicy.evaluate(repackaged, debugBuild = false, expectedCertSha256 = "AB:CD:EF")
        assertEquals(setOf(IntegrityRisk.REPACKAGED_DEBUGGABLE, IntegrityRisk.WRONG_SIGNATURE), verdict.risks)
        assertEquals(IntegrityAction.BLOCK, verdict.action)
    }

    @Test
    fun `debug es depurable por diseno y sin huella no se compara la firma`() {
        val debug = clean.copy(appDebuggable = true, signingCertSha256 = "00")
        assertEquals(
            IntegrityAction.NONE,
            IntegrityPolicy.evaluate(debug, debugBuild = true, expectedCertSha256 = "ab").action
        )
        assertEquals(
            IntegrityAction.NONE,
            IntegrityPolicy.evaluate(clean, debugBuild = false, expectedCertSha256 = "").action
        )
    }

    @Test
    fun `sistema con llaves de prueba cuenta como riesgo`() {
        val verdict = IntegrityPolicy.evaluate(clean.copy(testKeys = true), debugBuild = false, expectedCertSha256 = "")
        assertEquals(setOf(IntegrityRisk.TEST_KEYS), verdict.risks)
    }
}
