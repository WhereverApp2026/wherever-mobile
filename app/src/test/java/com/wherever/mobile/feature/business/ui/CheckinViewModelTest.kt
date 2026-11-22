package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.business.domain.ReservationCharge
import com.wherever.mobile.feature.business.domain.ReservationStatus
import com.wherever.mobile.testing.FakeBusinessHomeRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleBusinessReservation
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

/** nv-35: código válido, 404 y 409 `codigo_ya_usado`. */
@OptIn(ExperimentalCoroutinesApi::class)
class CheckinViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeBusinessHomeRepository()
    private val viewModel = CheckinViewModel(repository)

    @Test
    fun `sin codigo no se puede confirmar`() {
        viewModel.start("n1", null)
        assertFalse(viewModel.state.value.canConfirm)

        viewModel.onCode("wh-4821 ")
        assertEquals("WH-4821", viewModel.state.value.code)
        assertTrue(viewModel.state.value.canConfirm)
    }

    @Test
    fun `codigo de una reserva trae al cliente y su cobro`() = runTest {
        repository.reservationsResult = Result.Success(listOf(sampleBusinessReservation(deposit = 7400)))
        repository.chargeResult = Result.Success(ReservationCharge(7400, 0, 740, 6660, "pagado"))

        viewModel.start("n1", "WH-4821")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Mariana G.", state.reservation?.client)
        assertNotNull(state.charge)
        assertEquals("WH-4821", repository.reservationQueries.single().third)
    }

    @Test
    fun `codigo valido marca la llegada`() = runTest {
        viewModel.start("n1", null)
        viewModel.onCode("WH-4821")
        viewModel.confirm()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.arrived)
        assertEquals(ReservationStatus.ARRIVED, state.reservation?.status)
        assertTrue("escanear WH-4821" in repository.calls)
        assertFalse(state.canConfirm)
    }

    @Test
    fun `404 dice que no hay reserva de hoy con ese codigo`() = runTest {
        repository.checkInResult = Result.Failure(AppError.NotFound())
        viewModel.start("n1", null)
        viewModel.onCode("WH-0000")
        viewModel.confirm()
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.business_checkin_not_found), viewModel.state.value.error)
    }

    @Test
    fun `409 dice que el codigo ya se uso`() = runTest {
        repository.checkInResult = Result.Failure(AppError.Conflict("codigo_ya_usado"))
        viewModel.start("n1", null)
        viewModel.onCode("WH-4821")
        viewModel.confirm()
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.business_checkin_used), viewModel.state.value.error)
        assertFalse(viewModel.state.value.arrived)
    }

    @Test
    fun `sin reserva encontrada no se puede contactar`() = runTest {
        viewModel.start("n1", "WH-9999")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.notFound)
        assertFalse(viewModel.state.value.canContact)
        assertNull(viewModel.state.value.reservation)
    }
}
