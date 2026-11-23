package com.wherever.mobile.core.security

/** Motivo por el que el entorno no es confiable (MASVS-RESILIENCE-1, -2 y -4). */
enum class IntegrityRisk {
    /** `su`, Magisk o Superuser en el sistema. */
    ROOTED,

    /** Sistema firmado con llaves de prueba (`test-keys`): ROM modificada. */
    TEST_KEYS,

    /** Hay un depurador conectado o esperando. */
    DEBUGGER,

    /** Una build de release que el sistema marca como depurable: la re-empaquetaron. */
    REPACKAGED_DEBUGGABLE,

    /** La firma del APK no es la nuestra: la re-firmaron. */
    WRONG_SIGNATURE
}

/** Lo que se lee del teléfono; aparte para probar la decisión sin Android. */
data class DeviceSignals(
    val suPresent: Boolean,
    val testKeys: Boolean,
    val debuggerAttached: Boolean,
    val appDebuggable: Boolean,
    /** Huella SHA-256 del certificado con el que se firmó el APK instalado, o `null` si no se pudo leer. */
    val signingCertSha256: String?
)

/** Qué hace la app con los riesgos: en debug solo avisa; en release bloquea (decisión de Jorge, 2026-11-23). */
enum class IntegrityAction { NONE, WARN, BLOCK }

data class IntegrityVerdict(val risks: Set<IntegrityRisk>, val action: IntegrityAction)

object IntegrityPolicy {
    /**
     * @param debugBuild `BuildConfig.DEBUG`.
     * @param expectedCertSha256 huella esperada de la firma de release; vacía = no se comprueba.
     */
    fun evaluate(signals: DeviceSignals, debugBuild: Boolean, expectedCertSha256: String): IntegrityVerdict {
        val risks = buildSet {
            if (signals.suPresent) add(IntegrityRisk.ROOTED)
            if (signals.testKeys) add(IntegrityRisk.TEST_KEYS)
            if (signals.debuggerAttached) add(IntegrityRisk.DEBUGGER)
            if (!debugBuild && signals.appDebuggable) add(IntegrityRisk.REPACKAGED_DEBUGGABLE)
            val expected = expectedCertSha256.normalizedHex()
            val actual = signals.signingCertSha256?.normalizedHex()
            if (!debugBuild && expected.isNotEmpty() && actual != expected) add(IntegrityRisk.WRONG_SIGNATURE)
        }
        val action = when {
            risks.isEmpty() -> IntegrityAction.NONE
            debugBuild -> IntegrityAction.WARN
            else -> IntegrityAction.BLOCK
        }
        return IntegrityVerdict(risks, action)
    }

    private fun String.normalizedHex(): String = replace(":", "").trim().lowercase()
}

/** Lee las señales del teléfono. */
interface DeviceSignalsReader {
    fun read(): DeviceSignals
}
