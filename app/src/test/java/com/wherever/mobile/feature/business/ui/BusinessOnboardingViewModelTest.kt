package com.wherever.mobile.feature.business.ui

import app.cash.turbine.test
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.business.domain.Address
import com.wherever.mobile.feature.business.domain.BusinessState
import com.wherever.mobile.feature.business.domain.DESCRIPTION_MAX
import com.wherever.mobile.feature.business.domain.DocumentType
import com.wherever.mobile.feature.business.domain.isValidBusinessName
import com.wherever.mobile.feature.business.domain.isValidClabe
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.feature.photos.domain.UploadedImage
import com.wherever.mobile.testing.FakeBusinessOnboardingRepository
import com.wherever.mobile.testing.FakeBusinessPhotoRepository
import com.wherever.mobile.testing.FakeLocationProvider
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleBusinessDraft
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-34: avance por pasos, retomar en `paso_alta`, Guardar y salir, 422 con faltantes y validaciones. */
@OptIn(ExperimentalCoroutinesApi::class)
class BusinessOnboardingViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeBusinessOnboardingRepository()
    private val photos =
        FakeBusinessPhotoRepository(Result.Success(UploadedImage("i1", ImageState.PUBLISHED, "u", "t")))
    private val location = FakeLocationProvider(granted = true)

    private fun create() = BusinessOnboardingViewModel(repository, photos, location)

    @Test
    fun `alta nueva empieza en el registro y Continuar pide nombre y giro`() = runTest {
        val viewModel = create()
        advanceUntilIdle()
        assertEquals(OnboardingStep.REGISTER, viewModel.state.value.step)
        assertFalse(viewModel.state.value.canContinue)

        viewModel.onName("Ca")
        viewModel.onCategory("cafeteria")
        assertFalse(viewModel.state.value.canContinue)
        viewModel.onName("Café Raíz")
        assertTrue(viewModel.state.value.canContinue)
    }

    @Test
    fun `recorre los pasos y manda cada uno con la version que trae`() = runTest {
        val viewModel = create()
        advanceUntilIdle()
        viewModel.onName("Café Raíz")
        viewModel.onCategory("cafeteria")
        viewModel.next()
        advanceUntilIdle()
        assertEquals(OnboardingStep.LOCATION, viewModel.state.value.step)

        viewModel.onAddress(Address(street = "Hidalgo 23", neighborhood = "Centro", city = "Tula"))
        assertFalse(viewModel.state.value.canContinue)
        viewModel.locate()
        advanceUntilIdle()
        viewModel.next()
        advanceUntilIdle()
        assertEquals(OnboardingStep.OPERATION, viewModel.state.value.step)

        viewModel.onPriceLevel(2)
        viewModel.onAveragePrice("180")
        viewModel.next()
        advanceUntilIdle()
        assertEquals(OnboardingStep.VERIFY, viewModel.state.value.step)
        assertEquals(
            listOf("create:Café Raíz:cafeteria", "alta2:v1", "alta3:v2", "alta5:v3:2:180"),
            repository.calls.drop(1)
        )
    }

    @Test
    fun `retoma en el paso que sigue a paso_alta`() = runTest {
        repository.mineResult = Result.Success(listOf(sampleBusinessDraft(step = 2)))
        assertEquals(OnboardingStep.OPERATION, create().also { advanceUntilIdle() }.state.value.step)

        repository.mineResult = Result.Success(listOf(sampleBusinessDraft(step = 5)))
        assertEquals(OnboardingStep.VERIFY, create().also { advanceUntilIdle() }.state.value.step)

        repository.mineResult = Result.Success(listOf(sampleBusinessDraft(step = 6, state = BusinessState.IN_REVIEW)))
        assertEquals(OnboardingStep.DONE, create().also { advanceUntilIdle() }.state.value.step)
    }

    @Test
    fun `Guardar y salir avisa para cerrar sin perder lo guardado`() = runTest {
        repository.mineResult = Result.Success(listOf(sampleBusinessDraft(step = 2)))
        val viewModel = create()
        advanceUntilIdle()
        viewModel.exit.test {
            viewModel.saveAndExit()
            awaitItem()
        }
        assertFalse(repository.calls.any { it.startsWith("alta") })
    }

    @Test
    fun `enviar pide 3 fotos e INE y comprobante`() = runTest {
        repository.mineResult = Result.Success(listOf(sampleBusinessDraft(step = 5, photos = 2)))
        val viewModel = create()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.canContinue)

        viewModel.onPhotoPicked("content://foto")
        advanceUntilIdle()
        DocumentType.entries.forEach {
            viewModel.onDocumentPicked(it, "content://doc")
            advanceUntilIdle()
        }
        assertTrue(viewModel.state.value.canContinue)

        viewModel.next()
        advanceUntilIdle()
        assertEquals(OnboardingStep.DONE, viewModel.state.value.step)
        assertEquals(
            listOf("alta4", "alta6", "submit"),
            repository.calls.filter {
                it.startsWith("alta") ||
                    it == "submit"
            }
        )
    }

    @Test
    fun `422 de enviar-revision lista lo que falta tal cual`() = runTest {
        repository.mineResult = Result.Success(listOf(sampleBusinessDraft(step = 6, photos = 3)))
        repository.submitResult = Result.Failure(
            AppError.Validation(
                listOf(FieldError("fotos", "Faltan fotos"), FieldError("documentos", "Falta el comprobante"))
            )
        )
        val viewModel = create()
        advanceUntilIdle()
        DocumentType.entries.forEach {
            viewModel.onDocumentPicked(it, "content://doc")
            advanceUntilIdle()
        }
        viewModel.next()
        advanceUntilIdle()

        assertEquals(listOf("Faltan fotos", "Falta el comprobante"), viewModel.state.value.missing)
        assertEquals(OnboardingStep.VERIFY, viewModel.state.value.step)
    }

    @Test
    fun `CLABE escrita debe ser valida y llevar titular`() = runTest {
        repository.mineResult = Result.Success(listOf(sampleBusinessDraft(step = 6, photos = 3)))
        val viewModel = create()
        advanceUntilIdle()
        DocumentType.entries.forEach {
            viewModel.onDocumentPicked(it, "content://doc")
            advanceUntilIdle()
        }
        viewModel.onClabe("002010077777777771")
        assertFalse(viewModel.state.value.canContinue)
        viewModel.onHolder("Osmar Reséndiz")
        assertTrue(viewModel.state.value.canContinue)
        viewModel.onClabe("002010077777777770")
        assertFalse(viewModel.state.value.canContinue)
    }

    @Test
    fun `sin ubicacion lo explica`() = runTest {
        location.point = null
        val viewModel = create()
        advanceUntilIdle()
        viewModel.locate()
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `validaciones del alta`() {
        assertFalse(isValidBusinessName("Ca"))
        assertTrue(isValidBusinessName("Café"))
        assertFalse(isValidBusinessName("x".repeat(61)))
        assertTrue(isValidClabe("002010077777777771"))
        assertFalse(isValidClabe("002010077777777770"))
        assertFalse(isValidClabe("12345"))
        val viewModel = BusinessOnboardingViewModel(repository, photos, location)
        viewModel.onDescription("a".repeat(DESCRIPTION_MAX + 10))
        assertEquals(DESCRIPTION_MAX, viewModel.state.value.description.length)
    }
}
