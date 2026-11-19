package com.wherever.mobile.navigation

import app.cash.turbine.test
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.SessionEvents
import com.wherever.mobile.core.notifications.PushDestination
import com.wherever.mobile.testing.FakeAuthRepository
import com.wherever.mobile.testing.FakeDeviceRegistration
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val profiles = FakeProfileRepository()
    private val sessionEvents = SessionEvents()
    private val devices = FakeDeviceRegistration()

    private fun viewModel() = MainViewModel(auth, profiles, sessionEvents, devices)

    private val votePush = mapOf("tipo" to "votacion_abierta", "grupo_id" to "g1", "votacion_id" to "v1")

    @Test
    fun `con sesion vuelve a mandar el token de push al arrancar`() = runTest {
        auth.session = true
        viewModel()
        advanceUntilIdle()
        assertEquals(listOf("registerInBackground"), devices.calls)
    }

    @Test
    fun `sin sesion no registra el telefono para push`() = runTest {
        viewModel()
        advanceUntilIdle()
        assertTrue(devices.calls.isEmpty())
    }

    @Test
    fun `un push tocado con sesion llega a la raiz con su destino`() = runTest {
        auth.session = true
        val viewModel = viewModel()
        viewModel.pushes.test {
            viewModel.onPushOpened(votePush)
            assertEquals(PushDestination.VoteOpened("g1", "v1"), awaitItem())
        }
    }

    @Test
    fun `un push tocado sin sesion o con la cuenta por eliminarse no navega`() = runTest {
        val viewModel = viewModel()
        viewModel.pushes.test {
            viewModel.onPushOpened(votePush)
            advanceUntilIdle()
            expectNoEvents()

            auth.session = true
            profiles.profile.value = sampleProfile(deleteAt = "2026-12-03T19:10:00Z")
            viewModel.onPushOpened(votePush)
            advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `abrir la app sin datos de push no navega`() = runTest {
        auth.session = true
        val viewModel = viewModel()
        viewModel.pushes.test {
            viewModel.onPushOpened(mapOf("tipo" to null))
            advanceUntilIdle()
            expectNoEvents()
        }
    }

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
