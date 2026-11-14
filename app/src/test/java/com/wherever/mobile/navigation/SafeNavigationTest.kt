package com.wherever.mobile.navigation

import androidx.lifecycle.Lifecycle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** nv-45: solo la pantalla al frente y quieta navega; así un doble toque no apila dos veces. */
class SafeNavigationTest {
    @Test
    fun `resumed screen can navigate`() {
        assertTrue(canNavigateFrom(Lifecycle.State.RESUMED))
    }

    @Test
    fun `screen in transition or behind cannot navigate`() {
        listOf(
            Lifecycle.State.STARTED,
            Lifecycle.State.CREATED,
            Lifecycle.State.INITIALIZED,
            Lifecycle.State.DESTROYED
        ).forEach { assertFalse(it.name, canNavigateFrom(it)) }
    }

    @Test
    fun `result navigates while its screen is still on top, even in background`() {
        assertTrue(canDeliverResult(fromEntryId = "login", currentEntryId = "login"))
    }

    @Test
    fun `result is ignored once the screen already navigated or the stack is empty`() {
        assertFalse(canDeliverResult(fromEntryId = "login", currentEntryId = "home"))
        assertFalse(canDeliverResult(fromEntryId = "login", currentEntryId = null))
    }
}
