package com.wherever.mobile.feature.auth.ui

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.testing.FakeLocationPermissionPreferences
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** 08_Permisos (specs/nv-11): flujo de decisión por permiso. */
@OptIn(ExperimentalCoroutinesApi::class)
class PermissionsViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val profiles = FakeProfileRepository()
    private val prefs = FakeLocationPermissionPreferences()
    private val viewModel = PermissionsViewModel(profiles, prefs)

    @Test
    fun `permitir todo guarda ubicacion true y termina`() = runTest {
        viewModel.onSystemResult(location = true, notifications = true)
        advanceUntilIdle()

        assertEquals(listOf(PrivacyPatch(location = true)), profiles.privacyPatches)
        assertTrue(viewModel.state.value.done)
        assertEquals(0, prefs.deniedCount)
        assertFalse(prefs.notificationsDecided)
    }

    @Test
    fun `rechazar ubicacion la guarda false y cuenta el rechazo para Inicio`() = runTest {
        viewModel.onSystemResult(location = false, notifications = true)
        advanceUntilIdle()

        assertEquals(listOf(PrivacyPatch(location = false)), profiles.privacyPatches)
        assertEquals(1, prefs.deniedCount)
        assertTrue(viewModel.state.value.done)
    }

    @Test
    fun `rechazar avisos evita que Inicio los vuelva a pedir`() = runTest {
        viewModel.onSystemResult(location = true, notifications = false)
        advanceUntilIdle()

        assertTrue(prefs.notificationsDecided)
    }

    @Test
    fun `ahora no termina sin llamar a la API`() = runTest {
        viewModel.onNotNow()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.done)
        assertTrue(profiles.privacyPatches.isEmpty())
        assertTrue(prefs.dismissed)
        assertTrue(prefs.notificationsDecided)
    }

    @Test
    fun `sin red muestra el error y reintentar vuelve a mandar la misma decision`() = runTest {
        profiles.privacyResult = Result.Failure(AppError.Network)
        viewModel.onSystemResult(location = true, notifications = true)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.done)

        profiles.privacyResult = Result.Success(Unit)
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(List(2) { PrivacyPatch(location = true) }, profiles.privacyPatches)
        assertTrue(viewModel.state.value.done)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `continuar sin guardar deja seguir tras el error`() = runTest {
        profiles.privacyResult = Result.Failure(AppError.Network)
        viewModel.onSystemResult(location = true, notifications = true)
        advanceUntilIdle()

        viewModel.continueWithoutSaving()

        assertTrue(viewModel.state.value.done)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `doble toque no manda dos veces`() = runTest {
        viewModel.onSystemResult(location = true, notifications = true)
        viewModel.onSystemResult(location = true, notifications = true)
        advanceUntilIdle()

        assertEquals(1, profiles.privacyPatches.size)
    }
}
