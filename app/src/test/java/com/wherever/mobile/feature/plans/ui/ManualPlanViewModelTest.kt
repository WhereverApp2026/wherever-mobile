package com.wherever.mobile.feature.plans.ui

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.testing.FakePlacesRepository
import com.wherever.mobile.testing.FakePlansRepository
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-24: CrearPlan, plan a mano con nombre, paradas de 2 a 4, reordenar y visible para amigos. */
@OptIn(ExperimentalCoroutinesApi::class)
class ManualPlanViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    // Miércoles 18 de noviembre de 2026, 12:00 en Ciudad de México.
    private val clock = Clock.fixed(Instant.parse("2026-11-18T18:00:00Z"), ZoneId.of("America/Mexico_City"))
    private val plans = FakePlansRepository()

    private fun place(id: String) = PlaceCard(id, "Lugar $id", "cafes", 2, 150, 4.5, 10, null, null, true)

    private fun TestScope.create(): ManualPlanViewModel {
        val vm = ManualPlanViewModel(plans, FakePlacesRepository(), FakeProfileRepository(sampleProfile()), clock)
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `nombre de hasta 40 y paradas de 2 a 4 para crear`() = runTest(mainDispatcher.dispatcher) {
        val vm = create()
        vm.onName("x".repeat(50))
        assertEquals(PLAN_NAME_MAX, vm.state.value.name.length)

        vm.addPlace(place("a"))
        assertFalse(vm.state.value.canCreate)
        vm.addPlace(place("a"))
        assertEquals(1, vm.state.value.stops.size)
        listOf("b", "c", "d", "e").forEach { vm.addPlace(place(it)) }

        assertEquals(AI_MAX_STOPS, vm.state.value.stops.size)
        assertFalse(vm.state.value.canAddStop)
        assertTrue(vm.state.value.canCreate)
    }

    @Test
    fun `reordenar y quitar paradas`() = runTest(mainDispatcher.dispatcher) {
        val vm = create()
        listOf("a", "b", "c").forEach { vm.addPlace(place(it)) }

        vm.moveDown(0)
        assertEquals(listOf("b", "a", "c"), vm.state.value.stops.map { it.id })
        vm.moveUp(0)
        assertEquals(listOf("b", "a", "c"), vm.state.value.stops.map { it.id })
        vm.removeStop(1)
        assertEquals(listOf("b", "c"), vm.state.value.stops.map { it.id })
    }

    @Test
    fun `una hora que ya paso no deja crear`() = runTest(mainDispatcher.dispatcher) {
        val vm = create()
        vm.onName("Hoy")
        listOf("a", "b").forEach { vm.addPlace(place(it)) }

        vm.onDate(LocalDate.of(2026, 11, 18))
        vm.onTime(LocalTime.of(10, 0))
        assertTrue(vm.state.value.invalidDateTime)
        assertFalse(vm.state.value.canCreate)

        vm.onTime(LocalTime.of(19, 0))
        assertTrue(vm.state.value.canCreate)
    }

    @Test
    fun `crear manda paradas seguidas, personas por quien va y visibilidad`() = runTest(mainDispatcher.dispatcher) {
        val vm = create()
        vm.onName(" Sábado de cafés ")
        vm.onCompany(Company.FAMILY)
        vm.onPrice(PriceLevel.LOW)
        vm.onPublic(true)
        listOf("a", "b").forEach { vm.addPlace(place(it)) }

        vm.create()
        advanceUntilIdle()

        val plan = checkNotNull(plans.created)
        assertEquals("Sábado de cafés", plan.name)
        assertEquals("2026-11-19", plan.date)
        assertEquals(4, plan.people)
        assertEquals(200, plan.budget)
        assertEquals(Mobility.WALK, plan.mobility)
        assertTrue(plan.isPublic)
        assertEquals(listOf("2026-11-19T15:00:00Z", "2026-11-19T16:15:00Z"), plan.stops.map { it.arrives })
        assertEquals("manual1", vm.created.first())
    }

    @Test
    fun `si falla conserva lo escrito y muestra el error`() = runTest(mainDispatcher.dispatcher) {
        plans.createResult = Result.Failure(AppError.Network)
        val vm = create()
        vm.onName("Plan")
        listOf("a", "b").forEach { vm.addPlace(place(it)) }

        vm.create()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertEquals("Plan", vm.state.value.name)
        assertEquals(2, vm.state.value.stops.size)
    }
}
