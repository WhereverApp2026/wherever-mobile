package com.wherever.mobile.feature.checkin.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.checkin.domain.Checkin
import com.wherever.mobile.feature.checkin.domain.CheckinRepository
import com.wherever.mobile.testing.FakeLocationProvider
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.TEST_POINT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private data class Call(val placeId: String, val planId: String?, val point: GeoPoint?, val code: String?)

private class FakeCheckinRepository : CheckinRepository {
    val calls = mutableListOf<Call>()
    var result: Result<Checkin> = Result.Success(Checkin("c1", "n3", true, null, "2026-10-18T23:02:00Z"))

    override suspend fun checkIn(placeId: String, planId: String?, point: GeoPoint?, code: String?): Result<Checkin> {
        calls += Call(placeId, planId, point, code)
        return result
    }
}

/** nv-30: llegada por ubicación o código, sin permiso, fuera de radio y segundo check-in del día. */
@OptIn(ExperimentalCoroutinesApi::class)
class ArrivalViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeCheckinRepository()
    private val location = FakeLocationProvider(granted = true)
    private val handle = SavedStateHandle(
        mapOf("placeId" to "n3", "placeName" to "Fonda", "planId" to "p1", "nextName" to "Café", "nextMinutes" to 8)
    )

    private fun vm() = ArrivalViewModel(handle, repository, location)

    @Test
    fun `con permiso manda la ubicacion y confirma`() = runTest(mainDispatcher.dispatcher) {
        val vm = vm()

        vm.start()
        advanceUntilIdle()

        assertEquals(Call("n3", "p1", TEST_POINT, null), repository.calls.single())
        assertEquals(ArrivalPhase.DONE, vm.state.value.phase)
        assertEquals("Café", vm.state.value.nextName)
        assertEquals(8, vm.state.value.nextMinutes)
    }

    @Test
    fun `sin permiso no pide la ubicacion y ofrece opciones`() = runTest(mainDispatcher.dispatcher) {
        location.granted = false
        val vm = vm()

        vm.start()
        advanceUntilIdle()

        assertTrue(repository.calls.isEmpty())
        assertEquals(ArrivalPhase.NEEDS_PERMISSION, vm.state.value.phase)
    }

    @Test
    fun `fuera de radio muestra el mensaje neutro de la API`() = runTest(mainDispatcher.dispatcher) {
        repository.result =
            Result.Failure(AppError.Validation(apiMessage = "parece que aún no llegas. ¿Escaneas el código?"))
        val vm = vm()

        vm.start()
        advanceUntilIdle()

        assertEquals(ArrivalPhase.NOT_YET, vm.state.value.phase)
        assertEquals(UiText.Plain("Parece que aún no llegas. ¿Escaneas el código?"), vm.state.value.message)
    }

    @Test
    fun `el segundo check-in del dia cuenta como llegada`() = runTest(mainDispatcher.dispatcher) {
        repository.result = Result.Failure(AppError.Conflict())
        val vm = vm()

        vm.start()
        advanceUntilIdle()

        assertEquals(ArrivalPhase.DONE, vm.state.value.phase)
        assertTrue(vm.state.value.alreadyToday)
        assertNull(vm.state.value.checkin)
    }

    @Test
    fun `el codigo del mostrador va sin ubicacion y lo raro no llega a la API`() = runTest(mainDispatcher.dispatcher) {
        val vm = vm()

        vm.onScanned("https://sitio-raro.example/x?y=1")
        assertEquals(UiText.Res(R.string.arrival_bad_code), vm.state.value.message)
        assertTrue(repository.calls.isEmpty())

        vm.onScanned(" WH-ABC123 ")
        advanceUntilIdle()
        assertEquals(Call("n3", "p1", null, "WH-ABC123"), repository.calls.single())
        assertEquals(ArrivalPhase.DONE, vm.state.value.phase)
    }

    @Test
    fun `sin senal de ubicacion propone el codigo`() = runTest(mainDispatcher.dispatcher) {
        location.point = null
        val vm = vm()

        vm.start()
        advanceUntilIdle()

        assertTrue(repository.calls.isEmpty())
        assertEquals(UiText.Res(R.string.arrival_no_location), vm.state.value.message)
    }
}
