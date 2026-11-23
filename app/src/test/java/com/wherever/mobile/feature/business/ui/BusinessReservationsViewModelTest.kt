package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.business.domain.BlockReason
import com.wherever.mobile.feature.business.domain.CONTACT_MAX
import com.wherever.mobile.feature.business.domain.ReservationStatus
import com.wherever.mobile.testing.FakeBusinessHomeRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.businessClock
import com.wherever.mobile.testing.businessToday
import com.wherever.mobile.testing.sampleBusinessReservation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-35: pestañas, confirmar, llegó/no llegó (409 → recargar), contactar y cerrar horarios. */
@OptIn(ExperimentalCoroutinesApi::class)
class BusinessReservationsViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeBusinessHomeRepository()
    private val viewModel = BusinessReservationsViewModel(repository, businessClock)

    private fun startWith(vararg statuses: ReservationStatus) {
        repository.reservationsResult = Result.Success(
            statuses.mapIndexed { i, status -> sampleBusinessReservation("r$i", status) }
        )
        viewModel.start("n1")
    }

    @Test
    fun `hoy pide la fecha de hoy y cambiar de pestana cambia la fecha`() = runTest {
        startWith(ReservationStatus.CONFIRMED)
        advanceUntilIdle()
        assertEquals(businessToday, repository.reservationQueries.last().first)

        viewModel.selectTab(ReservationsTab.UPCOMING)
        advanceUntilIdle()
        assertEquals(businessToday.plusDays(1), repository.reservationQueries.last().first)

        viewModel.selectTab(ReservationsTab.PAST)
        advanceUntilIdle()
        assertEquals(businessToday.minusDays(1), repository.reservationQueries.last().first)

        viewModel.nextDay()
        advanceUntilIdle()
        assertEquals(businessToday.minusDays(2), repository.reservationQueries.last().first)
    }

    @Test
    fun `proximas no regresa a hoy`() = runTest {
        startWith()
        viewModel.selectTab(ReservationsTab.UPCOMING)
        advanceUntilIdle()
        val calls = repository.reservationQueries.size

        viewModel.previousDay()
        advanceUntilIdle()

        assertEquals(calls, repository.reservationQueries.size)
        assertEquals(businessToday.plusDays(1), viewModel.state.value.date)
    }

    @Test
    fun `confirmar una pendiente la actualiza en la lista`() = runTest {
        startWith(ReservationStatus.PENDING)
        advanceUntilIdle()

        viewModel.open(viewModel.state.value.items.single())
        viewModel.confirm()
        advanceUntilIdle()

        assertTrue("patch confirmada" in repository.calls)
        assertEquals(ReservationStatus.CONFIRMED, viewModel.state.value.items.single().status)
        assertNull(viewModel.state.value.selected)
    }

    @Test
    fun `llego y no llego mandan su estado`() = runTest {
        startWith(ReservationStatus.CONFIRMED, ReservationStatus.CONFIRMED)
        advanceUntilIdle()

        viewModel.open(viewModel.state.value.items[0])
        viewModel.markArrived()
        advanceUntilIdle()
        viewModel.open(viewModel.state.value.items[1])
        viewModel.markNoShow()
        advanceUntilIdle()

        assertTrue("patch asistio" in repository.calls)
        assertTrue("patch no_asistio" in repository.calls)
    }

    @Test
    fun `409 al cambiar el estado recarga el dia`() = runTest {
        startWith(ReservationStatus.CONFIRMED)
        advanceUntilIdle()
        repository.updateResult = Result.Failure(AppError.Conflict())

        viewModel.open(viewModel.state.value.items.single())
        viewModel.markArrived()
        advanceUntilIdle()

        assertEquals(2, repository.calls.count { it == "reservas" })
        assertEquals(UiText.Res(R.string.business_reservation_changed), viewModel.state.value.message)
    }

    @Test
    fun `contactar valida el texto y no lo pierde si falla`() = runTest {
        startWith(ReservationStatus.CONFIRMED)
        advanceUntilIdle()
        viewModel.open(viewModel.state.value.items.single())
        viewModel.openContact()

        viewModel.onContactText("<b>hola</b>")
        assertFalse(viewModel.state.value.canSendContact)

        viewModel.onContactText("x".repeat(CONTACT_MAX + 10))
        assertEquals(CONTACT_MAX, viewModel.state.value.contactText.length)

        repository.contactResult = Result.Failure(AppError.RateLimited())
        viewModel.onContactText("Tu mesa está lista")
        viewModel.sendContact()
        advanceUntilIdle()

        assertEquals("Tu mesa está lista", viewModel.state.value.contactText)
        assertTrue(viewModel.state.value.contactOpen)
    }

    @Test
    fun `cerrar y reabrir un horario`() = runTest {
        startWith()
        advanceUntilIdle()

        viewModel.openBlock()
        viewModel.onBlockForm(BlockForm("20:00", "18:00"))
        assertFalse(viewModel.state.value.blockForm!!.valid)
        viewModel.onBlockForm(BlockForm("18:00", "20:00", BlockReason.PRIVATE_EVENT))
        viewModel.saveBlock()
        advanceUntilIdle()

        val block = viewModel.state.value.blocks.single()
        assertEquals(businessToday.atTime(18, 0), block.from)
        assertNull(viewModel.state.value.blockForm)

        viewModel.unblock(block)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.blocks.isEmpty())
        assertTrue("reabrir b1" in repository.calls)
    }

    @Test
    fun `error de red muestra el error`() = runTest {
        repository.reservationsResult = Result.Failure(AppError.Network)
        viewModel.start("n1")
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.error_network), viewModel.state.value.error)
    }
}
