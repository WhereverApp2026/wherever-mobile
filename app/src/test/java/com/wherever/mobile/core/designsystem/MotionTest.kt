package com.wherever.mobile.core.designsystem

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** nv-45: tiempos centralizados y respeto a "animaciones apagadas". */
class MotionTest {
    @Test
    fun `animator scale 0 turns motion off`() {
        assertFalse(motionEnabled(0f))
    }

    @Test
    fun `any positive animator scale keeps motion on`() {
        assertTrue(motionEnabled(0.5f))
        assertTrue(motionEnabled(1f))
        assertTrue(motionEnabled(10f))
    }

    @Test
    fun `navigation transitions are none when motion is off`() {
        assertEquals(EnterTransition.None, NavMotion.forwardEnter(false))
        assertEquals(ExitTransition.None, NavMotion.forwardExit(false))
        assertEquals(EnterTransition.None, NavMotion.backEnter(false))
        assertEquals(ExitTransition.None, NavMotion.backExit(false))
        assertEquals(EnterTransition.None, NavMotion.resultEnter(false))
        assertEquals(ExitTransition.None, NavMotion.resultExit(false))
    }

    @Test
    fun `navigation transitions animate when motion is on`() {
        assertNotEquals(EnterTransition.None, NavMotion.forwardEnter(true))
        assertNotEquals(ExitTransition.None, NavMotion.forwardExit(true))
        assertNotEquals(EnterTransition.None, NavMotion.backEnter(true))
        assertNotEquals(ExitTransition.None, NavMotion.backExit(true))
        assertNotEquals(EnterTransition.None, NavMotion.resultEnter(true))
        assertNotEquals(ExitTransition.None, NavMotion.resultExit(true))
    }

    @Test
    fun `durations follow the spec`() {
        assertEquals(300, WhereverMotion.NAV_MS)
        assertTrue(WhereverMotion.CONTENT_MS < WhereverMotion.NAV_MS)
        assertTrue(WhereverMotion.SHORT_MS < WhereverMotion.CONTENT_MS)
        assertTrue(WhereverMotion.RESULT_SCALE in 0.9f..0.95f)
    }

    @Test
    fun `phase changes never animate size, so the old phase leaves at once`() {
        // `togetherWith` trae un resorte de tamaño por defecto; con él, LOADING seguía a la vista (CI del PR #21).
        assertNull(crossfade(enabled = false, durationMs = WhereverMotion.CONTENT_MS).sizeTransform)
        assertNull(crossfade(enabled = true, durationMs = WhereverMotion.CONTENT_MS).sizeTransform)
        assertNull(noMotion().sizeTransform)
    }
}
