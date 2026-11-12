package com.wherever.mobile.feature.settings.ui

import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.Tastes
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-43: interruptor de datos sensibles en Privacidad. */
@OptIn(ExperimentalCoroutinesApi::class)
class PrivacyViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val profiles = FakeProfileRepository()

    private fun TestScope.viewModel(allergies: String): PrivacyViewModel {
        profiles.profile.value = sampleProfile(
            tastes = Tastes(listOf("tacos", "cafes", "museos"), 100, 500, emptyList(), allergies),
            sensitiveData = true
        )
        val viewModel = PrivacyViewModel(profiles)
        runCurrent()
        return viewModel
    }

    @Test
    fun `encender manda datos_sensibles true`() = runTest {
        profiles.profile.value = sampleProfile()
        val viewModel = PrivacyViewModel(profiles)
        runCurrent()

        viewModel.onSensitiveDataChange(true)
        advanceUntilIdle()

        assertEquals(listOf(PrivacyPatch(sensitiveData = true)), profiles.privacyPatches)
    }

    @Test
    fun `apagar con alergias pide confirmacion y no envia todavia`() = runTest {
        val viewModel = viewModel(allergies = "nueces")
        assertTrue(viewModel.state.value.hasAllergies)

        viewModel.onSensitiveDataChange(false)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.confirmSensitiveOff)
        assertTrue(profiles.privacyPatches.isEmpty())
    }

    @Test
    fun `confirmar retira el permiso`() = runTest {
        val viewModel = viewModel(allergies = "nueces")
        viewModel.onSensitiveDataChange(false)

        viewModel.confirmSensitiveOff()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.confirmSensitiveOff)
        assertEquals(listOf(PrivacyPatch(sensitiveData = false)), profiles.privacyPatches)
    }

    @Test
    fun `cancelar no envia nada`() = runTest {
        val viewModel = viewModel(allergies = "nueces")
        viewModel.onSensitiveDataChange(false)

        viewModel.dismissSensitiveOff()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.confirmSensitiveOff)
        assertTrue(profiles.privacyPatches.isEmpty())
    }

    @Test
    fun `apagar sin alergias guardadas envia directo`() = runTest {
        val viewModel = viewModel(allergies = "")

        viewModel.onSensitiveDataChange(false)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.confirmSensitiveOff)
        assertEquals(listOf(PrivacyPatch(sensitiveData = false)), profiles.privacyPatches)
    }
}
