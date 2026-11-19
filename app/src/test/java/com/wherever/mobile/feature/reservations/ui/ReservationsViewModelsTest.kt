package com.wherever.mobile.feature.reservations.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.reservations.domain.CancellationPolicy
import com.wherever.mobile.feature.reservations.domain.NewReservation
import com.wherever.mobile.feature.reservations.domain.RefundQuote
import com.wherever.mobile.feature.reservations.domain.Reservation
import com.wherever.mobile.feature.reservations.domain.ReservationPage
import com.wherever.mobile.feature.reservations.domain.ReservationStatus
import com.wherever.mobile.feature.reservations.domain.ReservationsRepository
import com.wherever.mobile.feature.reservations.domain.TimeSlot
import com.wherever.mobile.testing.FakePlacesRepository
import com.wherever.mobile.testing.MainDispatcherRule
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val SLOT_19 = TimeSlot("2026-11-20T01:00:00Z", available = true, freeSeats = 6)
private val SLOT_20 = TimeSlot("2026-11-20T02:00:00Z", available = true, freeSeats = 2)
private val FULL = TimeSlot("2026-11-20T03:00:00Z", available = false, freeSeats = 0)

private fun reservation(
    id: String = "r1",
    status: ReservationStatus = ReservationStatus.CONFIRMED,
    dateTime: String = SLOT_19.time,
    version: Int = 1
) = Reservation(
    id = id,
    code = "K7M2P9",
    placeId = "p1",
    dateTime = dateTime,
    people = 2,
    note = null,
    status = status,
    holdExpiresAt = null,
    depositCents = 0,
    policy = CancellationPolicy("flexible", 24, 100, 50),
    refundCents = 0,
    canChange = true,
    canCancel = true,
    version = version
)

private class FakeReservationsRepository : ReservationsRepository {
    var slots: Result<List<TimeSlot>> = Result.Success(listOf(SLOT_19, SLOT_20, FULL))
    var createResult: Result<Reservation> = Result.Success(reservation())
    var getResult: Result<Reservation> = Result.Success(reservation())
    var listResult: Result<ReservationPage> = Result.Success(ReservationPage(listOf(reservation()), null))
    var refund: Result<RefundQuote> = Result.Success(RefundQuote(0, 100))
    var cancelResult: Result<Reservation> = Result.Success(reservation(status = ReservationStatus.CANCELLED))
    var changeResult: Result<Reservation> = Result.Success(reservation(dateTime = SLOT_20.time, version = 2))
    val created = mutableListOf<NewReservation>()
    val availabilityCalls = mutableListOf<Pair<LocalDate, Int>>()
    val calls = mutableListOf<String>()

    override suspend fun availability(placeId: String, date: LocalDate, people: Int): Result<List<TimeSlot>> {
        availabilityCalls += date to people
        return slots
    }

    override suspend fun create(reservation: NewReservation): Result<Reservation> {
        created += reservation
        return createResult
    }

    override suspend fun list(upcoming: Boolean, cursor: String?): Result<ReservationPage> {
        calls += "list:$upcoming:$cursor"
        return listResult
    }

    override suspend fun get(id: String): Result<Reservation> = getResult

    override suspend fun refundQuote(id: String): Result<RefundQuote> = refund

    override suspend fun cancel(id: String, reason: String?): Result<Reservation> {
        calls += "cancel:$reason"
        return cancelResult
    }

    override suspend fun changeTime(reservation: Reservation, dateTime: String): Result<Reservation> {
        calls += "change:${reservation.version}:$dateTime"
        return changeResult
    }
}

/** nv-54: reservar, confirmación, cambiar hora, cancelar con reembolso y Mis reservas. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReservationsViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeReservationsRepository()
    private val places = FakePlacesRepository()
    private val clock: Clock = Clock.fixed(Instant.parse("2026-11-19T18:00:00Z"), ZoneId.of("America/Mexico_City"))

    private fun reserve() = ReserveViewModel(
        SavedStateHandle(mapOf("placeId" to "p1", "placeName" to "Café Andrade")),
        repository,
        clock
    )

    @Test
    fun `reservar ofrece dos semanas desde hoy y carga turnos para 2 personas`() = runTest(mainDispatcher.dispatcher) {
        val vm = reserve()
        advanceUntilIdle()

        assertEquals(RESERVE_DAYS, vm.state.value.dates.size)
        assertEquals(LocalDate.of(2026, 11, 19), vm.state.value.dates.first())
        assertEquals(listOf(LocalDate.of(2026, 11, 19) to 2), repository.availabilityCalls)
        assertFalse(vm.state.value.canSubmit)
    }

    @Test
    fun `cambiar personas o dia recarga y quita el turno elegido`() = runTest(mainDispatcher.dispatcher) {
        val vm = reserve()
        advanceUntilIdle()
        vm.onSlot(SLOT_19)

        vm.onPeople(4)
        advanceUntilIdle()
        assertNull(vm.state.value.slot)
        vm.onDate(LocalDate.of(2026, 11, 21))
        advanceUntilIdle()

        assertEquals(LocalDate.of(2026, 11, 21) to 4, repository.availabilityCalls.last())
        vm.onPeople(99)
        assertEquals(MAX_PEOPLE_RESERVE, vm.state.value.people)
    }

    @Test
    fun `un turno lleno no se elige`() = runTest(mainDispatcher.dispatcher) {
        val vm = reserve()
        advanceUntilIdle()

        vm.onSlot(FULL)

        assertNull(vm.state.value.slot)
    }

    @Test
    fun `confirmar manda turno, personas, nota y abre la reserva`() = runTest(mainDispatcher.dispatcher) {
        val vm = reserve()
        advanceUntilIdle()
        vm.onSlot(SLOT_20)
        vm.onNote("Cumpleaños".padEnd(200, '!'))

        vm.created.test {
            vm.submit()
            assertEquals("r1", awaitItem())
        }
        val sent = repository.created.single()
        assertEquals(SLOT_20.time, sent.dateTime)
        assertEquals(2, sent.people)
        assertEquals(NOTE_MAX, sent.note?.length)
        assertTrue(sent.idempotencyKey.isNotBlank())
    }

    @Test
    fun `reintentar tras falla de red repite la llave y un turno nuevo la cambia`() =
        runTest(mainDispatcher.dispatcher) {
            repository.createResult = Result.Failure(AppError.Network)
            val vm = reserve()
            advanceUntilIdle()
            vm.onSlot(SLOT_19)

            vm.submit()
            advanceUntilIdle()
            vm.submit()
            advanceUntilIdle()
            vm.onSlot(SLOT_20)
            vm.submit()
            advanceUntilIdle()

            val keys = repository.created.map { it.idempotencyKey }
            assertEquals(keys[0], keys[1])
            assertNotEquals(keys[1], keys[2])
        }

    @Test
    fun `horario ocupado recarga turnos y correo sin verificar lo explica`() = runTest(mainDispatcher.dispatcher) {
        repository.createResult = Result.Failure(AppError.Conflict("horario ocupado"))
        val vm = reserve()
        advanceUntilIdle()
        vm.onSlot(SLOT_19)

        vm.submit()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.reserve_slot_taken), vm.state.value.error)
        assertNull(vm.state.value.slot)
        assertEquals(2, repository.availabilityCalls.size)

        repository.createResult = Result.Failure(AppError.Forbidden("correo no verificado"))
        vm.onSlot(SLOT_20)
        vm.submit()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.reserve_verify_email), vm.state.value.error)
    }

    private fun detail() = ReservationViewModel(SavedStateHandle(mapOf("id" to "r1")), repository, places, clock)

    @Test
    fun `la reserva carga con el nombre del lugar`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()
        advanceUntilIdle()

        assertEquals("K7M2P9", vm.state.value.reservation?.code)
        assertEquals("Café Andrade", vm.state.value.placeName)
    }

    @Test
    fun `cancelar muestra el reembolso antes y luego cancela con motivo`() = runTest(mainDispatcher.dispatcher) {
        repository.refund = Result.Success(RefundQuote(15_000, 50))
        val vm = detail()
        advanceUntilIdle()

        vm.askCancel()
        advanceUntilIdle()
        assertEquals(RefundQuote(15_000, 50), vm.state.value.cancelQuote)
        assertFalse(repository.calls.any { it.startsWith("cancel:") })

        vm.confirmCancel("Cambio de planes")
        advanceUntilIdle()
        assertTrue("cancel:Cambio de planes" in repository.calls)
        assertEquals(ReservationStatus.CANCELLED, vm.state.value.reservation?.status)
        assertEquals(UiText.Res(R.string.reservation_cancelled), vm.state.value.message)
    }

    @Test
    fun `cambiar hora ofrece los otros turnos del dia y manda la version`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()
        advanceUntilIdle()

        vm.openChange()
        advanceUntilIdle()
        // 19:00 es la hora actual de la reserva: no se ofrece.
        assertEquals(listOf(SLOT_20, FULL), vm.state.value.changeSlots)
        // 2026-11-20T01:00Z es el 19 de noviembre en CDMX.
        assertEquals(LocalDate.of(2026, 11, 19), repository.availabilityCalls.last().first)

        vm.changeTo(SLOT_20)
        advanceUntilIdle()
        assertTrue("change:1:${SLOT_20.time}" in repository.calls)
        assertEquals(2, vm.state.value.reservation?.version)
        assertNull(vm.state.value.changeSlots)
    }

    @Test
    fun `cambio con conflicto vuelve a leer la reserva`() = runTest(mainDispatcher.dispatcher) {
        repository.changeResult = Result.Failure(AppError.Conflict("versión vieja"))
        val vm = detail()
        advanceUntilIdle()
        repository.getResult = Result.Success(reservation(version = 3))

        vm.changeTo(SLOT_20)
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.reserve_slot_taken), vm.state.value.error)
        assertEquals(3, vm.state.value.reservation?.version)
    }

    @Test
    fun `mis reservas cambia de pestana y pone nombres`() = runTest(mainDispatcher.dispatcher) {
        val vm = MyReservationsViewModel(repository, places)
        vm.load()
        advanceUntilIdle()
        assertEquals("Café Andrade", vm.state.value.placeNames["p1"])

        vm.onUpcoming(false)
        advanceUntilIdle()

        assertEquals(listOf("list:true:null", "list:false:null"), repository.calls)
        assertNotNull(vm.state.value.items.firstOrNull())
    }

    @Test
    fun `pesos desde centavos`() {
        assertTrue(formatCents(150_050).contains("1,500.50"))
    }
}
