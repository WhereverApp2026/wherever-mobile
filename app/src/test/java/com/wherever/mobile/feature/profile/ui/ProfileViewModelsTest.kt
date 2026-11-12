package com.wherever.mobile.feature.profile.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.profile.domain.CitySuggestion
import com.wherever.mobile.feature.profile.domain.PreferencesPatch
import com.wherever.mobile.feature.tastes.ui.TastesViewModel
import com.wherever.mobile.testing.FakeAuthRepository
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelsTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val profiles = FakeProfileRepository()
    private val auth = FakeAuthRepository()

    // Perfil

    @Test
    fun `perfil muestra lo que llega y deja de cargar`() = runTest {
        val viewModel = ProfileViewModel(profiles)
        assertTrue(viewModel.state.value.loading)

        advanceUntilIdle()

        assertEquals("Ana López", viewModel.state.value.profile?.name)
        assertFalse(viewModel.state.value.loading)
    }

    @Test
    fun `perfil sin red conserva la cache y muestra el error`() = runTest {
        profiles.profile.value = sampleProfile()
        profiles.refreshResult = Result.Failure(AppError.Network)

        val viewModel = ProfileViewModel(profiles)
        advanceUntilIdle()

        assertEquals("Ana López", viewModel.state.value.profile?.name)
        assertEquals(UiText.Res(R.string.error_network), viewModel.state.value.error)
    }

    // Editar perfil

    private fun loadedEditor(): EditProfileViewModel {
        profiles.profile.value = sampleProfile()
        return EditProfileViewModel(profiles)
    }

    @Test
    fun `editar manda solo el nombre cambiado`() = runTest {
        val viewModel = loadedEditor()
        runCurrent()
        viewModel.onNameChange("Ana L.")

        viewModel.saved.test {
            viewModel.save()
            awaitItem()
        }

        assertEquals(listOf("Ana L." to null), profiles.profileUpdates)
    }

    @Test
    fun `sin cambios no llama a la API`() = runTest {
        val viewModel = loadedEditor()
        runCurrent()

        viewModel.saved.test {
            viewModel.save()
            awaitItem()
        }

        assertTrue(profiles.profileUpdates.isEmpty())
    }

    @Test
    fun `409 avisa y recarga el perfil del servidor`() = runTest {
        val viewModel = loadedEditor()
        runCurrent()
        viewModel.onNameChange("Mi cambio")
        profiles.updateProfileResult = Result.Failure(AppError.Conflict("versión vieja"))
        profiles.refreshResult = Result.Success(sampleProfile(version = 9, name = "Nombre del otro dispositivo"))

        viewModel.save()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(UiText.Res(R.string.edit_profile_conflict), state.error)
        assertEquals("Nombre del otro dispositivo", state.name)
        assertEquals(9L, profiles.profile.value?.version)
        assertFalse(state.saving)
    }

    @Test
    fun `nombre invalido no se puede guardar`() = runTest {
        val viewModel = loadedEditor()
        runCurrent()
        viewModel.onNameChange("A")
        assertFalse(viewModel.state.value.canSave)
        assertEquals(UiText.Res(R.string.validation_name), viewModel.state.value.nameError)
    }

    @Test
    fun `422 del nombre va bajo el campo`() = runTest {
        val viewModel = loadedEditor()
        runCurrent()
        viewModel.onNameChange("Otro nombre")
        profiles.updateProfileResult = Result.Failure(
            AppError.Validation(listOf(FieldError("nombre", "demasiado largo")))
        )

        viewModel.save()
        advanceUntilIdle()

        assertEquals(UiText.Plain("Demasiado largo"), viewModel.state.value.nameError)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `ciudad elegida se manda por id`() = runTest {
        val viewModel = loadedEditor()
        runCurrent()
        viewModel.onCitySelect(CitySuggestion("c9", "Tula", "Hidalgo"))

        viewModel.saved.test {
            viewModel.save()
            awaitItem()
        }

        assertEquals(listOf(null to "c9"), profiles.profileUpdates)
    }

    // Gustos

    private fun tastes(fromSettings: Boolean = false) = TastesViewModel(
        SavedStateHandle(mapOf("fromSettings" to fromSettings)),
        profiles,
        auth
    )

    @Test
    fun `gustos exige minimo 3 y limita a 10`() = runTest {
        val viewModel = tastes()
        runCurrent()
        viewModel.toggleCategory("a")
        viewModel.toggleCategory("b")
        assertFalse(viewModel.state.value.canSubmit)
        viewModel.toggleCategory("c")
        assertTrue(viewModel.state.value.canSubmit)

        ('d'..'z').forEach { viewModel.toggleCategory(it.toString()) }
        assertEquals(10, viewModel.state.value.categories.size)
    }

    @Test
    fun `gustos arranca con presupuesto de 150 a 300`() = runTest {
        val viewModel = tastes()
        runCurrent()
        assertEquals(150, viewModel.state.value.budget.min)
        assertEquals(300, viewModel.state.value.budget.max)
    }

    @Test
    fun `guardar manda gustos, movilidad y ciudad`() = runTest {
        val viewModel = tastes()
        runCurrent()
        listOf("tacos", "cafes", "museos").forEach(viewModel::toggleCategory)
        viewModel.onBudgetMinChange("100")
        viewModel.onBudgetMaxChange("500")
        viewModel.toggleRestriction("vegetariano")
        viewModel.onAllergiesEnabled(true)
        viewModel.onAllergiesChange(" nueces ")
        viewModel.onAllergiesConsentChange(true)
        viewModel.onMobilityChange("auto")
        viewModel.onCitySelect(CitySuggestion("c1", "Guadalajara", "Jalisco"))

        viewModel.finished.test {
            viewModel.submit()
            awaitItem()
        }

        val saved = profiles.savedTastes.single()
        assertEquals(listOf("tacos", "cafes", "museos"), saved.categories)
        assertEquals(100, saved.budgetMin)
        assertEquals(500, saved.budgetMax)
        assertEquals(listOf("vegetariano"), saved.restrictions)
        assertEquals("nueces", saved.allergies)
        assertEquals(listOf(PreferencesPatch(mobility = "auto")), profiles.preferencePatches)
        assertEquals(listOf(null to "c1"), profiles.profileUpdates)
    }

    @Test
    fun `alergias se limita a 200 caracteres`() = runTest {
        val viewModel = tastes()
        viewModel.onAllergiesChange("x".repeat(300))
        assertEquals(200, viewModel.state.value.allergies.length)
    }

    @Test
    fun `error al guardar muestra el mensaje de la API y no avanza`() = runTest {
        val viewModel = tastes()
        runCurrent()
        listOf("a", "b", "c").forEach(viewModel::toggleCategory)
        profiles.tastesResult = Result.Failure(AppError.Forbidden("rol no permitido"))

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(UiText.Plain("Rol no permitido"), viewModel.state.value.error)
        assertTrue(profiles.preferencePatches.isEmpty())
    }

    @Test
    fun `omitir recuerda la decision`() = runTest {
        val viewModel = tastes()

        viewModel.finished.test {
            viewModel.skip()
            awaitItem()
        }

        assertEquals(1, auth.skipCalls)
        assertTrue(profiles.savedTastes.isEmpty())
    }

    @Test
    fun `autocompletar espera tras escribir y exige 2 letras`() = runTest {
        val viewModel = tastes()
        profiles.citiesResult = Result.Success(listOf(CitySuggestion("c1", "Guadalajara", "Jalisco")))

        viewModel.onCityQueryChange("g")
        advanceUntilIdle()
        assertTrue(profiles.cityQueries.isEmpty())

        viewModel.onCityQueryChange("gu")
        advanceTimeBy(100)
        viewModel.onCityQueryChange("gua")
        advanceUntilIdle()

        assertEquals(listOf("gua"), profiles.cityQueries)
        assertEquals("Guadalajara", viewModel.state.value.city.suggestions.single().name)
    }

    @Test
    fun `autocompletar sin resultados lo indica`() = runTest {
        val viewModel = tastes()

        viewModel.onCityQueryChange("zzz")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.city.noResults)
    }
}
