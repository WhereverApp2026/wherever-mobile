package com.wherever.mobile.feature.business.ui

import app.cash.turbine.test
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.business.domain.BusinessState
import com.wherever.mobile.feature.business.domain.StatsRange
import com.wherever.mobile.testing.FakeBusinessHomeRepository
import com.wherever.mobile.testing.FakeBusinessOnboardingRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.businessClock
import com.wherever.mobile.testing.businessToday
import com.wherever.mobile.testing.sampleBusinessDraft
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-35: entrar al modo negocio, negocio activo y estadísticas con su reporte. */
@OptIn(ExperimentalCoroutinesApi::class)
class BusinessHomeViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val home = FakeBusinessHomeRepository()
    private val onboarding = FakeBusinessOnboardingRepository()

    @Test
    fun `sin rol comerciante manda al alta`() = runTest {
        home.modeResult = Result.Failure(AppError.Forbidden())
        val viewModel = BusinessHomeViewModel(home, onboarding)

        viewModel.needsOnboarding.test { awaitItem() }
    }

    @Test
    fun `un borrador sigue en el alta`() = runTest {
        onboarding.mineResult = Result.Success(listOf(sampleBusinessDraft(state = BusinessState.DRAFT)))
        val viewModel = BusinessHomeViewModel(home, onboarding)

        viewModel.needsOnboarding.test { awaitItem() }
    }

    @Test
    fun `el negocio activo es el primero enviado`() = runTest {
        onboarding.mineResult = Result.Success(
            listOf(
                sampleBusinessDraft(state = BusinessState.PUBLISHED).copy(id = "n2", name = "Taller"),
                sampleBusinessDraft(state = BusinessState.IN_REVIEW)
            )
        )
        val viewModel = BusinessHomeViewModel(home, onboarding)
        advanceUntilIdle()

        assertEquals("n2", viewModel.state.value.selectedId)
        assertEquals(2, viewModel.state.value.businesses.size)
        assertEquals(listOf("modo"), home.calls)

        viewModel.select("n1")
        assertEquals("Café Raíz", viewModel.state.value.selected?.name)
    }

    @Test
    fun `estadisticas por rango y reporte del mes`() = runTest {
        val viewModel = BusinessStatsViewModel(home, businessClock)
        viewModel.start("n1")
        advanceUntilIdle()
        assertEquals(businessToday.minusDays(7) to businessToday.minusDays(1), home.statsRanges.last())
        assertEquals(90, viewModel.state.value.stats!!.attendance)

        viewModel.selectRange(StatsRange.QUARTER)
        advanceUntilIdle()
        assertEquals(businessToday.minusDays(90), home.statsRanges.last().first)

        viewModel.downloadReport()
        advanceUntilIdle()
        assertTrue("reporte 2026-10" in home.calls)
        assertEquals(
            UiText.Res(R.string.business_stats_report_saved, listOf("wherever-2026-10.pdf")),
            viewModel.state.value.message
        )
    }
}
