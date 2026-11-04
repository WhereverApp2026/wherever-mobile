package com.wherever.mobile.feature.settings.ui

import app.cash.turbine.test
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.auth.domain.ActiveSession
import com.wherever.mobile.feature.profile.domain.PreferencesPatch
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.testing.FakeAuthRepository
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelsTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val profiles = FakeProfileRepository(sampleProfile())
    private val auth = FakeAuthRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC)

    // Ajustes

    @Test
    fun `cambiar tema manda el PATCH parcial`() = runTest {
        val viewModel = SettingsViewModel(profiles, auth)

        viewModel.setTheme("oscuro")
        advanceUntilIdle()

        assertEquals(listOf(PreferencesPatch(theme = "oscuro")), profiles.preferencePatches)
    }

    @Test
    fun `interruptor de votaciones usa grupos`() = runTest {
        val viewModel = SettingsViewModel(profiles, auth)

        viewModel.setGroups(false)
        advanceUntilIdle()

        assertEquals(listOf(PreferencesPatch(groups = false)), profiles.preferencePatches)
    }

    @Test
    fun `usar ubicacion va a privacidad`() = runTest {
        val viewModel = SettingsViewModel(profiles, auth)

        viewModel.setUseLocation(false)
        advanceUntilIdle()

        assertEquals(listOf(PrivacyPatch(location = false)), profiles.privacyPatches)
    }

    @Test
    fun `fallo al guardar muestra el mensaje de la API`() = runTest {
        val viewModel = SettingsViewModel(profiles, auth)
        profiles.preferencesResult = Result.Failure(AppError.Validation(apiMessage = "datos inválidos"))

        viewModel.setPromotions(true)
        advanceUntilIdle()

        assertEquals(UiText.Plain("Datos inválidos"), viewModel.state.value.error)
        assertFalse(viewModel.state.value.saving)
    }

    @Test
    fun `cerrar sesion llama al repositorio y avisa`() = runTest {
        val viewModel = SettingsViewModel(profiles, auth)

        viewModel.loggedOut.test {
            viewModel.logout()
            awaitItem()
        }

        assertEquals(1, auth.logoutCalls)
    }

    // Sesiones

    private val sessions = listOf(
        ActiveSession("s1", "Pixel 8", "Android 15", "2026-11-03T19:05:00Z", true),
        ActiveSession("s2", "iPhone 15", "iOS 19", "2026-11-01T02:10:00Z", false)
    )

    @Test
    fun `sesiones se muestran con fecha legible`() = runTest {
        auth.sessionsResult = Result.Success(sessions)

        val viewModel = SessionsViewModel(auth, clock)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Pixel 8 · Android 15", state.sessions.first().device)
        assertTrue(state.sessions.first().lastUsed.contains("2026"))
        assertTrue(state.sessions.first().isCurrent)
        assertFalse(state.loading)
    }

    @Test
    fun `sesiones con error permite reintentar`() = runTest {
        auth.sessionsResult = Result.Failure(AppError.Network)
        val viewModel = SessionsViewModel(auth, clock)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.loadFailed)

        auth.sessionsResult = Result.Success(sessions)
        viewModel.load()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.loadFailed)
        assertEquals(2, viewModel.state.value.sessions.size)
    }

    @Test
    fun `cerrar una sesion la quita de la lista`() = runTest {
        auth.sessionsResult = Result.Success(sessions)
        val viewModel = SessionsViewModel(auth, clock)
        advanceUntilIdle()

        viewModel.close("s2")
        advanceUntilIdle()

        assertEquals(listOf("s1"), viewModel.state.value.sessions.map { it.id })
    }

    @Test
    fun `404 al cerrar una sesion conserva la lista y avisa`() = runTest {
        auth.sessionsResult = Result.Success(sessions)
        auth.closeResult = Result.Failure(AppError.NotFound("no encontrada"))
        val viewModel = SessionsViewModel(auth, clock)
        advanceUntilIdle()

        viewModel.close("s2")
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.sessions.size)
        assertEquals(UiText.Plain("No encontrada"), viewModel.state.value.error)
    }

    @Test
    fun `cerrar todas avisa solo si el servidor confirma`() = runTest {
        val viewModel = SessionsViewModel(auth, clock)
        advanceUntilIdle()

        auth.logoutAllResult = Result.Failure(AppError.Network)
        viewModel.closeAll()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.error_network), viewModel.state.value.error)

        auth.logoutAllResult = Result.Success(Unit)
        viewModel.allClosed.test {
            viewModel.closeAll()
            awaitItem()
        }
    }

    // Privacidad

    @Test
    fun `privacidad manda solo el interruptor tocado`() = runTest {
        val viewModel = PrivacyViewModel(profiles)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.privacy!!.searchableByPhone)

        viewModel.update(PrivacyPatch(searchableByPhone = true))
        advanceUntilIdle()

        assertEquals(listOf(PrivacyPatch(searchableByPhone = true)), profiles.privacyPatches)
    }

    // Eliminar cuenta

    @Test
    fun `eliminar solo se habilita con ELIMINAR exacto`() = runTest {
        val viewModel = DeleteAccountViewModel(profiles, auth)

        viewModel.onConfirmationChange("eliminar")
        assertFalse(viewModel.state.value.canDelete)
        viewModel.delete()
        advanceUntilIdle()
        assertTrue(profiles.deleteConfirmations.isEmpty())

        viewModel.onConfirmationChange("ELIMINAR")
        assertTrue(viewModel.state.value.canDelete)
    }

    @Test
    fun `eliminar limpia la sesion local y avisa`() = runTest {
        val viewModel = DeleteAccountViewModel(profiles, auth).apply { onConfirmationChange("ELIMINAR") }

        viewModel.deleted.test {
            viewModel.delete()
            awaitItem()
        }

        assertEquals(listOf("ELIMINAR"), profiles.deleteConfirmations)
        assertEquals(1, auth.clearCalls)
    }

    @Test
    fun `eliminar con error conserva la sesion`() = runTest {
        val viewModel = DeleteAccountViewModel(profiles, auth).apply { onConfirmationChange("ELIMINAR") }
        profiles.deleteResult = Result.Failure(AppError.Network)

        viewModel.delete()
        advanceUntilIdle()

        assertEquals(0, auth.clearCalls)
        assertEquals(UiText.Res(R.string.error_network), viewModel.state.value.error)
    }

    @Test
    fun `eliminacion pendiente se puede cancelar`() = runTest {
        profiles.profile.value = sampleProfile(deleteAt = "2026-12-03T19:10:00Z")
        val viewModel = DeletionPendingViewModel(profiles, auth, clock)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.deleteDate.contains("2026"))

        viewModel.events.test {
            viewModel.cancelDeletion()
            assertEquals(DeletionPendingEvent.CANCELLED, awaitItem())
        }
    }

    @Test
    fun `cancelar fuera de plazo muestra el error`() = runTest {
        val viewModel = DeletionPendingViewModel(profiles, auth, clock)
        profiles.cancelResult = Result.Failure(AppError.Conflict("fuera de plazo"))

        viewModel.cancelDeletion()
        advanceUntilIdle()

        assertEquals(UiText.Plain("Fuera de plazo"), viewModel.state.value.error)
        assertFalse(viewModel.state.value.working)
    }
}
