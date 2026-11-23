package com.wherever.mobile.security

import com.wherever.mobile.core.security.AppLockPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Constitución E-05: el bloqueo vuelve tras más de un minuto en segundo plano, solo si está encendido. */
class AppLockPolicyTest {
    @Test
    fun `apagado nunca bloquea`() {
        assertFalse(AppLockPolicy.lockOnReturn(enabled = false, backgroundSinceMillis = 0, nowMillis = 10_000_000))
    }

    @Test
    fun `menos de un minuto fuera no vuelve a pedir la huella`() {
        assertFalse(
            AppLockPolicy.lockOnReturn(
                true,
                backgroundSinceMillis = 1_000,
                nowMillis =
                1_000 + AppLockPolicy.GRACE_MILLIS
            )
        )
    }

    @Test
    fun `mas de un minuto fuera bloquea`() {
        assertTrue(
            AppLockPolicy.lockOnReturn(
                true,
                backgroundSinceMillis = 1_000,
                nowMillis =
                2_000 + AppLockPolicy.GRACE_MILLIS
            )
        )
    }

    @Test
    fun `sin haber salido no bloquea`() {
        assertFalse(AppLockPolicy.lockOnReturn(true, backgroundSinceMillis = null, nowMillis = 5_000_000))
    }
}
