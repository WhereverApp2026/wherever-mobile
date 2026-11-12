package com.wherever.mobile.feature.tastes.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
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

    // Presupuesto (nv-44)

    @Test
    fun `con un error de presupuesto no se puede continuar`() = runTest {
        val viewModel = viewModel()
        assertTrue(viewModel.state.value.canSubmit)

        viewModel.onBudgetMinChange("")
        assertFalse(viewModel.state.value.canSubmit)

        viewModel.onBudgetMinChange("500")
        viewModel.onBudgetMaxChange("300")
        assertFalse(viewModel.state.value.canSubmit)

        viewModel.onBudgetMaxChange("60000")
        assertFalse(viewModel.state.value.canSubmit)

        viewModel.submit()
        advanceUntilIdle()
        assertTrue(profiles.calls.isEmpty())
    }

    @Test
    fun `desde Ajustes carga el presupuesto guardado`() = runTest {
        profiles.profile.value = sampleProfile(
            tastes = Tastes(listOf("tacos", "cafes", "museos"), 250, 2500, emptyList(), "")
        )
        val viewModel = TastesViewModel(SavedStateHandle(mapOf("fromSettings" to true)), profiles, auth)
        runCurrent()

        assertEquals("250", viewModel.state.value.budget.minText)
        assertEquals("2500", viewModel.state.value.budget.maxText)
        assertEquals(1000f, viewModel.state.value.budget.sliderEnd)
    }

    @Test
    fun `se guardan los enteros escritos, aunque pasen de 1000`() = runTest {
        val viewModel = viewModel()
        viewModel.onBudgetMinChange(" 0120 ")
        viewModel.onBudgetMaxChange("2,500")

        viewModel.finished.test {
            viewModel.submit()
            awaitItem()
        }

        assertEquals(120, profiles.savedTastes.single().budgetMin)
        assertEquals(2500, profiles.savedTastes.single().budgetMax)
    }

    @Test
    fun `barra y soltar dejan pasos de 50 en el estado`() = runTest {
        val viewModel = viewModel()
        viewModel.onBudgetSlide(150f, 712f)
        assertEquals("712", viewModel.state.value.budget.maxText)

        viewModel.onBudgetSlideFinished()

        assertEquals("700", viewModel.state.value.budget.maxText)
    }

    @Test
    fun `422 en presupuesto va bajo el campo y se borra al editarlo`() = runTest {
        profiles.tastesResult = Result.Failure(
            AppError.Validation(
                listOf(FieldError("presupuesto_max", "debe ser mayor o igual que presupuesto_min")),
                apiMessage = "datos inválidos"
            )
        )
        val viewModel = viewModel()

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(
            UiText.Plain("Debe ser mayor o igual que presupuesto_min"),
            viewModel.state.value.budgetMaxError
        )
        assertNull(viewModel.state.value.error)

        viewModel.onBudgetMaxChange("400")
        assertNull(viewModel.state.value.budgetMaxError)
    }

    // Alergias (nv-43)

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
