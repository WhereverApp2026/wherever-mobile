package com.wherever.mobile.feature.groups.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.groups.domain.MemberTrip
import com.wherever.mobile.testing.FakeGroupsRepository
import com.wherever.mobile.testing.FakeLocationProvider
import com.wherever.mobile.testing.FakeMeetingPointRepository
import com.wherever.mobile.testing.FakePlansRepository
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** nv-25 (Unit): 47_PuntoMedio. */
@OptIn(ExperimentalCoroutinesApi::class)
class MeetingPointViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private lateinit var groups: FakeGroupsRepository
    private lateinit var meeting: FakeMeetingPointRepository
    private lateinit var plans: FakePlansRepository
    private lateinit var location: FakeLocationProvider
    private val clock = Clock.fixed(Instant.parse("2026-11-18T18:00:00Z"), ZoneId.of("America/Mexico_City"))

    @Before
    fun setUp() {
        groups = FakeGroupsRepository()
        meeting = FakeMeetingPointRepository()
        plans = FakePlansRepository()
        location = FakeLocationProvider(granted = true)
    }

    private fun TestScope.viewModel(): MeetingPointViewModel {
        val vm = MeetingPointViewModel(
            SavedStateHandle(mapOf("id" to "g1")),
            groups,
            meeting,
            plans,
            FakeProfileRepository(sampleProfile()),
            location,
            clock
        )
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `carga el grupo, el plan y los puntos sugeridos sin traslados si no hay punto`() = runTest {
        val vm = viewModel()

        val state = vm.state.value
        assertEquals(listOf("suggest:g1"), meeting.calls)
        assertEquals(2, state.suggestion?.places?.size)
        assertFalse(state.sharing)
        assertEquals(2, state.confirmed)
        assertFalse(state.canUse)
    }

    @Test
    fun `con menos de 2 ubicaciones explica que falta compartir`() = runTest {
        meeting.suggestResult = Result.Failure(AppError.Validation(apiMessage = "faltan_ubicaciones"))
        val vm = viewModel()
        assertTrue(vm.state.value.needLocations)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `compartir manda la ubicacion aproximada y vuelve a pedir sugerencias`() = runTest {
        val vm = viewModel()
        vm.share(granted = true)
        advanceUntilIdle()

        assertTrue(meeting.calls.contains("share:g1:20.6767,-103.3475"))
        assertTrue(vm.state.value.sharing)
        assertEquals(2, meeting.calls.count { it == "suggest:g1" })

        vm.stopSharing()
        advanceUntilIdle()
        assertTrue(meeting.calls.contains("share:g1:off"))
        assertFalse(vm.state.value.sharing)
    }

    @Test
    fun `sin permiso no manda nada y lo explica`() = runTest {
        val vm = viewModel()
        vm.share(granted = false)
        advanceUntilIdle()
        assertTrue(meeting.calls.none { it.startsWith("share") })
        assertEquals(UiText.Res(R.string.meeting_no_location), vm.state.value.error)
    }

    @Test
    fun `usar este punto fija el lugar a la hora de la primera parada y trae los traslados`() = runTest {
        meeting.tripsResult = Result.Success(listOf(MemberTrip("u1", "Ana", null, true, 12, 25, null, false)))
        val vm = viewModel()
        vm.select("n1")
        assertTrue(vm.state.value.canUse)
        vm.usePoint()
        advanceUntilIdle()

        assertTrue(meeting.calls.contains("set:g1:n1:2026-11-18T22:00:00Z"))
        assertEquals("Restaurante Colonial", vm.state.value.group?.meetingPoint?.name)
        assertEquals(12, vm.state.value.trips.single().minutes)
        assertFalse("el punto ya elegido no se vuelve a mandar", vm.state.value.canUse)
    }

    @Test
    fun `si la API rechaza la hora muestra su motivo`() = runTest {
        meeting.setResult = { Result.Failure(AppError.Validation(apiMessage = "la hora debe ser del día del plan")) }
        val vm = viewModel()
        vm.select("n2")
        vm.usePoint()
        advanceUntilIdle()
        assertEquals(UiText.Plain("La hora debe ser del día del plan"), vm.state.value.error)
        assertFalse(vm.state.value.busy)
    }
}
