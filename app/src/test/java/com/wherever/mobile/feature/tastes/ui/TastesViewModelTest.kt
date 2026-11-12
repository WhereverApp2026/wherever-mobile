package com.wherever.mobile.feature.tastes.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.Tastes
import com.wherever.mobile.testing.FakeAuthRepository
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-43: consentimiento expreso de datos sensibles antes de guardar alergias. */
@OptIn(ExperimentalCoroutinesApi::class)
class TastesViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val profiles = FakeProfileRepository()
    private val auth = FakeAuthRepository()

    private fun TestScope.viewModel(): TastesViewModel {
        val viewModel = TastesViewModel(SavedStateHandle(), profiles, auth)
        runCurrent()
        listOf("tacos", "cafes", "museos").forEach(viewModel::toggleCategory)
        return viewModel
    }

    private fun TastesViewModel.withAllergies(text: String = "nueces") {
        onAllergiesEnabled(true)
        onAllergiesChange(text)
    }

    @Test
    fun `alergias sin la casilla no envian nada y piden marcarla`() = runTest {
        val viewModel = viewModel()
        viewModel.withAllergies()

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.tastes_allergies_consent_required), viewModel.state.value.consentError)
        assertTrue(profiles.calls.isEmpty())
        assertFalse(viewModel.state.value.saving)
    }

    @Test
    fun `marcar la casilla quita el error`() = runTest {
        val viewModel = viewModel()
        viewModel.withAllergies()
        viewModel.submit()

        viewModel.onAllergiesConsentChange(true)

        assertNull(viewModel.state.value.consentError)
    }

    @Test
    fun `con la casilla el permiso va antes que los gustos`() = runTest {
        val viewModel = viewModel()
        viewModel.withAllergies()
        viewModel.onAllergiesConsentChange(true)

        viewModel.finished.test {
            viewModel.submit()
            awaitItem()
        }

        assertEquals(listOf("privacidad", "gustos"), profiles.calls)
        assertEquals(listOf(PrivacyPatch(sensitiveData = true)), profiles.privacyPatches)
        assertEquals("nueces", profiles.savedTastes.single().allergies)
    }

    @Test
    fun `con el permiso ya guardado la casilla abre marcada y no se repite`() = runTest {
        profiles.profile.value = sampleProfile(
            tastes = Tastes(listOf("tacos", "cafes", "museos"), 100, 500, emptyList(), "nueces"),
            sensitiveData = true
        )
        val viewModel = TastesViewModel(SavedStateHandle(), profiles, auth)
        runCurrent()
        assertTrue(viewModel.state.value.allergiesConsent)

        viewModel.finished.test {
            viewModel.submit()
            awaitItem()
        }

        assertEquals(listOf("gustos"), profiles.calls)
    }

    @Test
    fun `si falla el permiso no se envian los gustos`() = runTest {
        profiles.privacyResult = Result.Failure(AppError.Network)
        val viewModel = viewModel()
        viewModel.withAllergies()
        viewModel.onAllergiesConsentChange(true)

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(listOf("privacidad"), profiles.calls)
        assertEquals(UiText.Res(R.string.error_network), viewModel.state.value.error)
    }

    @Test
    fun `reintentar tras fallar los gustos no repite el permiso`() = runTest {
        profiles.tastesResult = Result.Failure(AppError.Network)
        val viewModel = viewModel()
        viewModel.withAllergies()
        viewModel.onAllergiesConsentChange(true)
        viewModel.submit()
        advanceUntilIdle()
        profiles.tastesResult = Result.Success(Unit)

        viewModel.finished.test {
            viewModel.submit()
            awaitItem()
        }

        assertEquals(listOf("privacidad", "gustos", "gustos"), profiles.calls)
    }

    @Test
    fun `sin alergias no se toca el permiso aunque falte la casilla`() = runTest {
        val viewModel = viewModel()
        viewModel.withAllergies("   ")

        viewModel.finished.test {
            viewModel.submit()
            awaitItem()
        }

        assertEquals(listOf("gustos"), profiles.calls)
        assertEquals("", profiles.savedTastes.single().allergies)
    }

    @Test
    fun `chip de alergias apagado no envia el texto ni pide permiso`() = runTest {
        val viewModel = viewModel()
        viewModel.withAllergies()
        viewModel.onAllergiesEnabled(false)

        viewModel.finished.test {
            viewModel.submit()
            awaitItem()
        }

        assertEquals(listOf("gustos"), profiles.calls)
        assertEquals("", profiles.savedTastes.single().allergies)
    }
}
