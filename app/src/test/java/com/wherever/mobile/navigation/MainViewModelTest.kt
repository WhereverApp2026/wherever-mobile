package com.wherever.mobile.navigation

import app.cash.turbine.test
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.SessionEvents
import com.wherever.mobile.testing.FakeAuthRepository
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val profiles = FakeProfileRepository()
    private val sessionEvents = SessionEvents()

    private fun viewModel() = MainViewModel(auth, profiles, sessionEvents)

    @Test
    fun `sin sesion va a Bienvenida`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(StartDestination.WELCOME, viewModel.start.value)
    }

    @Test
    fun `con sesion entra directo a Inicio`() = runTest {
        auth.session = true
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(StartDestination.HOME, viewModel.start.value)
    }

    @Test
    fun `con eliminacion pendiente en cache va a su pantalla`() = runTest {
        auth.session = true
        profiles.profile.value = sampleProfile(deleteAt = "2026-12-03T19:10:00Z")
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(StartDestination.DELETION_PENDING, viewModel.start.value)
    }

    @Test
    fun `si el servidor revela eliminacion pendiente avisa a la raiz`() = runTest {
        auth.session = true
        profiles.profile.value = sampleProfile()
        profiles.refreshResult = Result.Success(sampleProfile(deleteAt = "2026-12-03T19:10:00Z"))

        val viewModel = viewModel()
        viewModel.events.test {
            assertEquals(RootEvent.DELETION_PENDING, awaitItem())
        }
    }

    @Test
    fun `sesion rechazada limpia y lleva a Login`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.events.test {
            sessionEvents.notifySessionExpired()
            assertEquals(RootEvent.SESSION_EXPIRED, awaitItem())
        }
        assertEquals(1, auth.clearCalls)
    }
}
