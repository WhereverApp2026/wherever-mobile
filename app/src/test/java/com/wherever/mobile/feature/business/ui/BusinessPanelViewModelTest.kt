package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.business.domain.DailyTip
import com.wherever.mobile.feature.business.domain.ReservationStatus
import com.wherever.mobile.testing.FakeBusinessHomeRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.businessClock
import com.wherever.mobile.testing.businessToday
import com.wherever.mobile.testing.sampleBusinessReservation
import com.wherever.mobile.testing.samplePanel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-35: el Panel oculta los datos nulos, muestra el consejo si responde y reintenta tras un error. */
@OptIn(ExperimentalCoroutinesApi::class)
class BusinessPanelViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeBusinessHomeRepository()
    private val viewModel = BusinessPanelViewModel(repository, businessClock)

    @Test
    fun `los datos nulos del panel no generan cifras`() = runTest {
        repository.panelResult = Result.Success(samplePanel().copy(reservationsToday = 12, couponsRedeemedToday = null))

        viewModel.start("n1")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(PanelTile(PanelMetric.RESERVATIONS_TODAY, 12)), state.tiles)
        assertNull(state.panel?.arrivalsToday)
    }

    @Test
    fun `sin consejo ni semana el panel se muestra igual`() = runTest {
        repository.statsResult = Result.Failure(AppError.RateLimited())

        viewModel.start("n1")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.panel)
        assertNull(state.tip)
        assertNull(state.week)
    }

    @Test
    fun `el consejo del dia se muestra si responde`() = runTest {
        repository.tipResult = Result.Success(DailyTip("Lanza un cupón el martes", "crear_cupon"))

        viewModel.start("n1")
        advanceUntilIdle()

        assertEquals("Lanza un cupón el martes", viewModel.state.value.tip?.text)
    }

    @Test
    fun `proximas hoy solo trae pendientes y confirmadas que no han pasado`() = runTest {
        repository.reservationsResult = Result.Success(
            listOf(
                sampleBusinessReservation("viejo", at = businessToday.atTime(9, 0)),
                sampleBusinessReservation("llego", ReservationStatus.ARRIVED, businessToday.atTime(15, 0)),
                sampleBusinessReservation("tarde", ReservationStatus.PENDING, businessToday.atTime(18, 30)),
                sampleBusinessReservation("luego", at = businessToday.atTime(17, 0))
            )
        )

        viewModel.start("n1")
        advanceUntilIdle()

        assertEquals(listOf("luego", "tarde"), viewModel.state.value.upcoming.map { it.id })
        assertEquals(businessToday, repository.reservationQueries.single().first)
    }

    @Test
    fun `error del panel y reintentar`() = runTest {
        repository.panelResult = Result.Failure(AppError.Network)
        viewModel.start("n1")
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)

        repository.panelResult = Result.Success(samplePanel())
        viewModel.load()
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertTrue(repository.calls.count { it == "panel" } == 2)
    }

    @Test
    fun `la grafica pide los 7 dias hasta ayer`() = runTest {
        viewModel.start("n1")
        advanceUntilIdle()

        assertEquals(businessToday.minusDays(7) to businessToday.minusDays(1), repository.statsRanges.single())
    }
}
