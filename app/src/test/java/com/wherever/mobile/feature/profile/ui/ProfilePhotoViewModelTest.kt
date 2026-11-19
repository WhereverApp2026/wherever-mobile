package com.wherever.mobile.feature.profile.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.feature.photos.domain.PhotoRepository
import com.wherever.mobile.feature.photos.domain.UploadedImage
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-50: foto de perfil desde galería o cámara. */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfilePhotoViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val profiles = FakeProfileRepository(sampleProfile())
    private var result: Result<UploadedImage> =
        Result.Success(UploadedImage("img1", ImageState.PUBLISHED, "https://img/1080.jpg", null))
    private val sources = mutableListOf<String>()
    private val photos = object : PhotoRepository {
        override suspend fun upload(source: String): Result<UploadedImage> {
            sources += source
            return result
        }
    }

    private fun viewModel() = ProfilePhotoViewModel(photos, profiles)

    @Test
    fun `publicada se guarda en el perfil`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()
        vm.openChooser()

        vm.onPicked("content://media/1")
        assertTrue(vm.state.value.uploading)
        assertFalse(vm.state.value.choosing)
        advanceUntilIdle()

        assertEquals(listOf("content://media/1"), sources)
        assertEquals(listOf("https://img/1080.jpg"), profiles.photoUpdates)
        assertEquals(UiText.Res(R.string.photo_saved), vm.state.value.message)
        assertFalse(vm.state.value.uploading)
    }

    @Test
    fun `cancelar no sube nada`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()

        vm.onPicked(null)
        advanceUntilIdle()

        assertTrue(sources.isEmpty())
    }

    @Test
    fun `en revision o rechazada no cambia el perfil`() = runTest(mainDispatcher.dispatcher) {
        result = Result.Success(UploadedImage("img1", ImageState.REJECTED, null, null))
        val vm = viewModel()

        vm.onPicked("content://media/1")
        advanceUntilIdle()

        assertTrue(profiles.photoUpdates.isEmpty())
        assertEquals(UiText.Res(R.string.photo_rejected), vm.state.value.message)
    }

    @Test
    fun `foto ilegible da su mensaje propio`() = runTest(mainDispatcher.dispatcher) {
        result = Result.Failure(AppError.Validation())
        val vm = viewModel()

        vm.onPicked("content://media/roto")
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.photo_unreadable), vm.state.value.message)
    }
}
