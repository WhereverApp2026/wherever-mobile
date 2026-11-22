package com.wherever.mobile.feature.reviews.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.feature.photos.domain.UploadedImage
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.COMMENT_MAX_LENGTH
import com.wherever.mobile.feature.reviews.domain.MAX_PHOTOS
import com.wherever.mobile.testing.FakePhotoRepository
import com.wherever.mobile.testing.FakeReviewRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleMyReview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-32: validaciones de 68_PostVisita, 78_ResCom antes de publicar, fotos, 422 de moderación y edición. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewFormViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeReviewRepository()
    private val photos = FakePhotoRepository(Result.Success(UploadedImage("i1", ImageState.PUBLISHED, "u", "t")))

    private fun create(vararg args: Pair<String, String>) = ReviewFormViewModel(
        repository,
        photos,
        SavedStateHandle(mapOf("checkinId" to "c1", "placeId" to "p1", "placeName" to "Café Raíz") + args.toMap())
    )

    private fun ReviewFormViewModel.rateAll(value: Int = 4) = Aspect.entries.forEach { onAspect(it, value) }

    @Test
    fun `sin estrellas no se puede publicar`() = runTest {
        val viewModel = create()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.canPublish)
        viewModel.publish()
        assertTrue(repository.drafts.isEmpty())

        viewModel.onRating(4)
        assertTrue(viewModel.state.value.canPublish)
    }

    @Test
    fun `el comentario no pasa de 500`() = runTest {
        val viewModel = create()
        viewModel.onComment("a".repeat(COMMENT_MAX_LENGTH + 10))
        assertEquals(COMMENT_MAX_LENGTH, viewModel.state.value.comment.length)
    }

    @Test
    fun `publicar sin aspectos abre Calificar a detalle y no envia`() = runTest {
        val viewModel = create()
        viewModel.onRating(5)
        viewModel.publish()
        assertEquals(ReviewStep.DETAIL, viewModel.state.value.step)
        assertTrue(repository.drafts.isEmpty())
    }

    @Test
    fun `Listo con los cinco aspectos publica y muestra Publicado`() = runTest {
        val viewModel = create()
        advanceUntilIdle()
        viewModel.onRating(5)
        viewModel.onComment("  Buenísimo  ")
        viewModel.publish()
        viewModel.rateAll(4)
        viewModel.onDetailDone()
        advanceUntilIdle()

        val draft = repository.drafts.single()
        assertEquals("c1", draft.checkinId)
        assertEquals(5, draft.rating)
        assertEquals(Aspect.entries.toSet(), draft.aspects.keys)
        assertEquals(ReviewStep.PUBLISHED, viewModel.state.value.step)
    }

    @Test
    fun `Listo con aspectos incompletos regresa al formulario`() = runTest {
        val viewModel = create()
        viewModel.onRating(5)
        viewModel.openDetail()
        viewModel.onAspect(Aspect.TASTE, 5)
        viewModel.onDetailDone()
        assertEquals(ReviewStep.FORM, viewModel.state.value.step)
        assertTrue(repository.drafts.isEmpty())
    }

    @Test
    fun `hasta 3 fotos y solo las publicadas`() = runTest {
        val viewModel = create()
        repeat(MAX_PHOTOS + 1) {
            viewModel.onPhotoPicked("content://foto/$it")
            advanceUntilIdle()
        }
        assertEquals(MAX_PHOTOS, viewModel.state.value.photos.size)
        assertEquals(MAX_PHOTOS, photos.sources.size)
        assertFalse(viewModel.state.value.canAddPhoto)
    }

    @Test
    fun `una foto en revision no se agrega y se explica`() = runTest {
        photos.result = Result.Success(UploadedImage("i2", ImageState.IN_REVIEW, null, null))
        val viewModel = create()
        viewModel.onPhotoPicked("content://foto/1")
        advanceUntilIdle()
        assertTrue(viewModel.state.value.photos.isEmpty())
        assertEquals(UiText.Res(R.string.reviews_photo_in_review), viewModel.state.value.photoMessage)
    }

    @Test
    fun `422 de moderacion explica sin perder lo escrito`() = runTest {
        repository.createResult = Result.Failure(
            AppError.Validation(
                fields = listOf(FieldError("comentario", "el texto no cumple las reglas")),
                apiMessage = "texto rechazado"
            )
        )
        val viewModel = create()
        viewModel.onRating(3)
        viewModel.onComment("Texto que no pasa")
        viewModel.rateAll()
        viewModel.publish()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(ReviewStep.FORM, state.step)
        assertEquals("Texto que no pasa", state.comment)
        assertEquals(UiText.Plain("El texto no cumple las reglas"), state.commentError)
        assertFalse(state.submitting)
    }

    @Test
    fun `422 sin campo muestra el mensaje de la API`() = runTest {
        repository.createResult = Result.Failure(AppError.Validation(apiMessage = "texto rechazado por moderacion"))
        val viewModel = create()
        viewModel.onRating(3)
        viewModel.rateAll()
        viewModel.publish()
        advanceUntilIdle()
        assertEquals(UiText.Plain("Texto rechazado por moderacion"), viewModel.state.value.error)
    }

    @Test
    fun `editar carga la resena y manda la version`() = runTest {
        repository.cache["r1"] = sampleMyReview(version = 7)
        val viewModel = create("reviewId" to "r1")
        assertTrue(viewModel.state.value.editing)
        assertEquals(4, viewModel.state.value.rating)

        viewModel.onRating(2)
        viewModel.publish()
        advanceUntilIdle()

        assertTrue("update:r1:v7" in repository.calls)
        assertEquals(ReviewStep.PUBLISHED, viewModel.state.value.step)
    }

    @Test
    fun `409 al editar recarga la version y lo explica`() = runTest {
        repository.cache["r1"] = sampleMyReview(version = 7)
        repository.updateResult = Result.Failure(AppError.Conflict())
        repository.mineResult = Result.Success(
            com.wherever.mobile.core.common.PageResult(listOf(sampleMyReview(version = 8)), null)
        )
        val viewModel = create("reviewId" to "r1")
        viewModel.publish()
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.reviews_edit_conflict), viewModel.state.value.error)
        repository.updateResult = null
        viewModel.publish()
        advanceUntilIdle()
        assertTrue("update:r1:v8" in repository.calls)
    }

    @Test
    fun `editar sin la resena en memoria lo avisa`() = runTest {
        val viewModel = create("reviewId" to "zz")
        assertTrue(viewModel.state.value.missing)
        assertNull(viewModel.state.value.published)
    }
}
