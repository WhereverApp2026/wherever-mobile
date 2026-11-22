package com.wherever.mobile.feature.reviews.ui

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.reviews.domain.PendingReview
import com.wherever.mobile.testing.FakeReviewRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleMyReview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-32: Publicadas y Por reseñar, editable, eliminar con Deshacer (el reloj es el del TestScope). */
@OptIn(ExperimentalCoroutinesApi::class)
class MyReviewsViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeReviewRepository().apply {
        mineResult = Result.Success(
            PageResult(
                listOf(
                    sampleMyReview("r1", editable = true, createdAt = "2026-11-20T10:00:00Z"),
                    sampleMyReview("r2", editable = false, createdAt = "2026-11-10T10:00:00Z")
                ),
                null
            )
        )
        pendingResult = Result.Success(listOf(PendingReview("c9", "p9", "Jardín del Cerro", "2026-11-18", "")))
    }

    /** Alcance de la app con el reloj virtual de las pruebas. */
    private val appScope = TestScope(mainDispatcher.dispatcher)

    private fun create() = MyReviewsViewModel(repository, appScope)

    @Test
    fun `carga publicadas, por resenar y si se pueden editar`() {
        val viewModel = create()
        appScope.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertEquals(listOf("r1", "r2"), state.reviews.map { it.id })
        assertEquals(listOf(true, false), state.reviews.map { it.editable })
        assertEquals(1, state.pending.size)
    }

    @Test
    fun `error al cargar muestra Reintentar`() {
        repository.mineResult = Result.Failure(AppError.Network)
        val viewModel = create()
        appScope.advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `eliminar pide confirmacion y cancelar no borra`() {
        val viewModel = create()
        appScope.advanceUntilIdle()
        viewModel.askDelete(viewModel.state.value.reviews.first())
        assertNotNull(viewModel.state.value.confirmDelete)
        viewModel.cancelDelete()
        assertNull(viewModel.state.value.confirmDelete)
        assertEquals(2, viewModel.state.value.reviews.size)
    }

    @Test
    fun `el DELETE se manda al vencer los 5 segundos`() {
        val viewModel = create()
        appScope.advanceUntilIdle()
        viewModel.askDelete(viewModel.state.value.reviews.first())
        viewModel.confirmDelete()

        assertEquals(listOf("r2"), viewModel.state.value.reviews.map { it.id })
        assertEquals("r1", viewModel.state.value.undoable?.id)
        appScope.advanceTimeBy(UNDO_WINDOW_MS - 1)
        appScope.runCurrent()
        assertFalse(repository.calls.any { it.startsWith("delete") })

        appScope.advanceTimeBy(2)
        appScope.runCurrent()
        assertTrue("delete:r1" in repository.calls)
        assertNull(viewModel.state.value.undoable)
    }

    @Test
    fun `Deshacer antes del plazo no borra y regresa la resena`() {
        val viewModel = create()
        appScope.advanceUntilIdle()
        viewModel.askDelete(viewModel.state.value.reviews.first())
        viewModel.confirmDelete()
        appScope.advanceTimeBy(UNDO_WINDOW_MS / 2)
        viewModel.undo()
        appScope.advanceUntilIdle()

        assertFalse(repository.calls.any { it.startsWith("delete") })
        assertEquals(listOf("r1", "r2"), viewModel.state.value.reviews.map { it.id })
        assertNull(viewModel.state.value.undoable)
    }

    @Test
    fun `si el DELETE falla la resena vuelve y se explica`() {
        repository.deleteResult = Result.Failure(AppError.Network)
        val viewModel = create()
        appScope.advanceUntilIdle()
        viewModel.askDelete(viewModel.state.value.reviews.first())
        viewModel.confirmDelete()
        appScope.advanceUntilIdle()

        assertEquals(listOf("r1", "r2"), viewModel.state.value.reviews.map { it.id })
        assertNotNull(viewModel.state.value.message)
    }

    @Test
    fun `un segundo borrado manda el primero sin esperar`() {
        val viewModel = create()
        appScope.advanceUntilIdle()
        viewModel.askDelete(viewModel.state.value.reviews[0])
        viewModel.confirmDelete()
        viewModel.askDelete(viewModel.state.value.reviews[0])
        viewModel.confirmDelete()
        appScope.runCurrent()

        assertTrue("delete:r1" in repository.calls)
        assertFalse("delete:r2" in repository.calls)
        assertEquals("r2", viewModel.state.value.undoable?.id)
    }

    @Test
    fun `cambiar de pestana`() {
        val viewModel = create()
        viewModel.onTab(MyReviewsTab.PENDING)
        assertEquals(MyReviewsTab.PENDING, viewModel.state.value.tab)
    }
}
