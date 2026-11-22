package com.wherever.mobile.feature.reviews.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.reviews.domain.PendingReview
import com.wherever.mobile.feature.reviews.domain.PlaceReview
import com.wherever.mobile.feature.reviews.domain.ReportReason
import com.wherever.mobile.feature.reviews.domain.ReviewedVisit
import com.wherever.mobile.feature.reviews.domain.UsefulCount
import com.wherever.mobile.testing.FakeReviewRepository
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

private fun placeReview(id: String, useful: Int = 3) =
    PlaceReview(id, "ana", null, 5, "Rico", emptyList(), useful, null, "2026-11-19T18:00:00Z")

/** nv-32: 77_ResPend y 80_ResLugar (filtros, Útil y Reportar). */
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewListsViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeReviewRepository()

    @Test
    fun `por resenar muestra pendientes y ya resenadas`() = runTest {
        repository.pendingResult = Result.Success(listOf(PendingReview("c1", "p1", "Fonda", "2026-11-20", "")))
        repository.visitsResult = Result.Success(listOf(ReviewedVisit("c2", "p2", "Mercado", "2026-11-01", null)))
        val viewModel = PendingReviewsViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.pending.size)
        assertEquals(1, state.reviewed.size)
        assertFalse(state.empty)
    }

    @Test
    fun `por resenar vacio y con error`() = runTest {
        val empty = PendingReviewsViewModel(repository)
        advanceUntilIdle()
        assertTrue(empty.state.value.empty)

        repository.pendingResult = Result.Failure(AppError.Network)
        val failing = PendingReviewsViewModel(repository)
        advanceUntilIdle()
        assertNotNull(failing.state.value.error)
        assertFalse(failing.state.value.empty)
    }

    @Test
    fun `si fallan las ya resenadas, los pendientes se muestran igual`() = runTest {
        repository.pendingResult = Result.Success(listOf(PendingReview("c1", "p1", "Fonda", "2026-11-20", "")))
        repository.visitsResult = Result.Failure(AppError.Server())
        val viewModel = PendingReviewsViewModel(repository)
        advanceUntilIdle()
        assertNull(viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.pending.size)
    }

    private fun placeViewModel() = PlaceReviewsViewModel(repository, SavedStateHandle(mapOf("placeId" to "p1")))

    @Test
    fun `resenas del lugar con promedio y filtros`() = runTest {
        repository.placeReviewsResult = Result.Success(PageResult(listOf(placeReview("r1")), "cur"))
        val viewModel = placeViewModel()
        advanceUntilIdle()

        assertEquals("Café Raíz", viewModel.state.value.placeName)
        assertEquals(4.5, viewModel.state.value.place!!.average, 0.0)
        viewModel.onQuery(viewModel.state.value.query.copy(withPhotos = true, stars = 5))
        advanceUntilIdle()
        assertTrue(repository.queries.last().withPhotos)
        assertEquals(5, repository.queries.last().stars)

        viewModel.loadMore()
        advanceUntilIdle()
        assertTrue("placeReviews:cur" in repository.calls)
    }

    @Test
    fun `Util cambia el contador con lo que dice la API`() = runTest {
        repository.placeReviewsResult = Result.Success(PageResult(listOf(placeReview("r1", useful = 3)), null))
        repository.usefulResult = Result.Success(UsefulCount(true, 4))
        val viewModel = placeViewModel()
        advanceUntilIdle()

        viewModel.toggleUseful(viewModel.state.value.reviews.single())
        advanceUntilIdle()
        assertEquals(4, viewModel.state.value.reviews.single().useful)
        assertTrue("r1" in viewModel.state.value.usefulMarked)

        repository.usefulResult = Result.Success(UsefulCount(false, 3))
        viewModel.toggleUseful(viewModel.state.value.reviews.single())
        advanceUntilIdle()
        assertTrue("useful:r1:false" in repository.calls)
        assertEquals(3, viewModel.state.value.reviews.single().useful)
    }

    @Test
    fun `Reportar pide motivo y avisa al enviar`() = runTest {
        repository.placeReviewsResult = Result.Success(PageResult(listOf(placeReview("r1")), null))
        val viewModel = placeViewModel()
        advanceUntilIdle()

        viewModel.openReport(viewModel.state.value.reviews.single())
        assertFalse(viewModel.state.value.report!!.canSend)
        viewModel.onReportReason(ReportReason.SPAM)
        viewModel.sendReport()
        advanceUntilIdle()

        assertTrue("report:r1:spam" in repository.calls)
        assertNull(viewModel.state.value.report)
        assertEquals(UiText.Res(R.string.reviews_report_sent), viewModel.state.value.message)
    }

    @Test
    fun `reportar dos veces no es un error`() = runTest {
        repository.placeReviewsResult = Result.Success(PageResult(listOf(placeReview("r1")), null))
        repository.reportResult = Result.Failure(AppError.Conflict())
        val viewModel = placeViewModel()
        advanceUntilIdle()
        viewModel.openReport(viewModel.state.value.reviews.single())
        viewModel.onReportReason(ReportReason.FAKE)
        viewModel.sendReport()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.reviews_report_already), viewModel.state.value.message)
    }

    @Test
    fun `lugar sin resenas muestra vacio`() = runTest {
        val viewModel = placeViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.empty)
    }
}
