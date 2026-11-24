package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.business.domain.REPLY_MAX
import com.wherever.mobile.feature.business.domain.ReplyStatus
import com.wherever.mobile.feature.business.domain.ReviewPeriod
import com.wherever.mobile.feature.business.domain.SealCriterion
import com.wherever.mobile.feature.business.domain.SealProgress
import com.wherever.mobile.feature.business.domain.isValidReply
import com.wherever.mobile.testing.FakeBusinessHomeRepository
import com.wherever.mobile.testing.FakeBusinessReviewsRepository
import com.wherever.mobile.testing.FakeReviewRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleMoment
import com.wherever.mobile.testing.samplePanel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-36: periodo mes/trimestre, respuesta de 1 a 500, 422 de moderación y momentos sin duplicar. */
@OptIn(ExperimentalCoroutinesApi::class)
class BusinessReviewsViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeBusinessReviewsRepository()
    private val home = FakeBusinessHomeRepository()
    private val placeReviews = FakeReviewRepository().apply {
        placeReviewsResult = Result.Success(PageResult(sampleReviews(), null))
    }

    @Test
    fun `respuesta valida de 1 a 500 sin contar espacios`() {
        assertFalse(isValidReply("   "))
        assertTrue(isValidReply(" a "))
        assertTrue(isValidReply("x".repeat(REPLY_MAX)))
        assertFalse(isValidReply("x".repeat(REPLY_MAX + 1)))
    }

    @Test
    fun `resenas recientes, resumen del mes y cambio a trimestre`() = runTest {
        home.panelResult = Result.Success(
            samplePanel().copy(
                seal = true,
                sealProgress = SealProgress(true, listOf(SealCriterion("Reseñas respondidas (%)", 92.0, 80.0, true)))
            )
        )
        val viewModel = BusinessReviewsViewModel(repository, placeReviews, home)

        viewModel.start("n1")
        advanceUntilIdle()
        val state = viewModel.state.value
        assertEquals(2, state.reviews.size)
        assertTrue(placeReviews.queries.single().recent)
        assertTrue(state.hasSeal)
        assertEquals(92, state.replyProgress)
        assertEquals(1, state.newMoments)

        viewModel.onPeriod(ReviewPeriod.QUARTER)
        advanceUntilIdle()
        assertEquals(listOf(ReviewPeriod.MONTH, ReviewPeriod.QUARTER), repository.periods)
        assertEquals(ReviewPeriod.QUARTER, viewModel.state.value.period)
    }

    @Test
    fun `sin resumen ni momentos la lista de resenas se muestra igual`() = runTest {
        repository.summaryResult = Result.Failure(AppError.Server())
        repository.momentsResult = Result.Failure(AppError.Server())
        val viewModel = BusinessReviewsViewModel(repository, placeReviews, home)

        viewModel.start("n1")
        advanceUntilIdle()
        val state = viewModel.state.value
        assertNull(state.error)
        assertNull(state.summary)
        assertNull(state.newMoments)
        assertEquals(2, state.reviews.size)
    }

    @Test
    fun `publicar solo con texto valido y avisa si quedo en revision`() = runTest {
        repository.replyResult = Result.Success(ReplyStatus.IN_MODERATION)
        val viewModel = ReviewReplyViewModel(repository)
        viewModel.start("n1", "r1", null)

        viewModel.publish()
        advanceUntilIdle()
        assertTrue(repository.replies.isEmpty())

        viewModel.onText("x".repeat(REPLY_MAX + 20))
        assertEquals(REPLY_MAX, viewModel.state.value.text.length)
        viewModel.onText("¡Gracias por venir!")
        viewModel.publish()
        advanceUntilIdle()
        assertEquals(listOf("¡Gracias por venir!"), repository.replies)
        assertEquals(R.string.reply_in_moderation, viewModel.published.first())
    }

    @Test
    fun `422 de moderacion muestra el motivo y conserva lo escrito`() = runTest {
        repository.replyResult = Result.Failure(AppError.Validation(apiMessage = "Lenguaje no permitido"))
        val viewModel = ReviewReplyViewModel(repository)
        viewModel.start("n1", "r1", null)
        viewModel.onText("texto")

        viewModel.publish()
        advanceUntilIdle()
        val state = viewModel.state.value
        assertEquals(UiText.Plain("Lenguaje no permitido"), state.error)
        assertEquals("texto", state.text)
        assertFalse(state.saving)
    }

    @Test
    fun `la IA deja el texto en el campo y sin IA avisa`() = runTest {
        val viewModel = ReviewReplyViewModel(repository)
        viewModel.start("n1", "r1", "respuesta previa")
        assertEquals("respuesta previa", viewModel.state.value.text)

        viewModel.suggest()
        advanceUntilIdle()
        assertEquals("¡Gracias por venir!", viewModel.state.value.text)
        assertTrue(repository.replies.isEmpty())

        repository.suggestResult = Result.Failure(AppError.Server())
        viewModel.suggest()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.reply_ai_unavailable), viewModel.state.value.error)
    }

    @Test
    fun `agradecer una sola vez e ignorar el doble toque`() = runTest {
        val viewModel = BusinessMomentsViewModel(repository)
        viewModel.start("n1")
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.newCount)

        val moment = viewModel.state.value.all.first()
        viewModel.thank(moment)
        viewModel.thank(moment)
        advanceUntilIdle()
        assertEquals(1, repository.calls.count { it.startsWith("marcar m1") })
        assertEquals(0, viewModel.state.value.newCount)
        assertEquals(UiText.Res(R.string.moments_thanked), viewModel.state.value.message)

        viewModel.thank(viewModel.state.value.all.first())
        advanceUntilIdle()
        assertEquals(1, repository.calls.count { it.startsWith("marcar m1") })
    }

    @Test
    fun `destacar y quitar de destacados actualiza la pestana`() = runTest {
        val viewModel = BusinessMomentsViewModel(repository)
        viewModel.start("n1")
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.featured.size)

        viewModel.toggleFeatured(viewModel.state.value.all.first())
        advanceUntilIdle()
        assertTrue(repository.calls.contains("marcar m1 null true"))
        assertEquals(2, viewModel.state.value.featured.size)
        viewModel.selectTab(MomentsTab.FEATURED)
        assertEquals(2, viewModel.state.value.visible.size)
    }

    @Test
    fun `si falla marcar se libera el momento y se avisa`() = runTest {
        repository.markResult = Result.Failure(AppError.Network)
        val viewModel = BusinessMomentsViewModel(repository)
        viewModel.start("n1")
        advanceUntilIdle()

        viewModel.thank(sampleMoment("m1"))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.busy.isEmpty())
        assertFalse(viewModel.state.value.all.first().thanked)
        assertTrue(viewModel.state.value.message != null)
    }
}
