package com.wherever.mobile.feature.plans.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.PlansTab
import com.wherever.mobile.testing.FakeLocationProvider
import com.wherever.mobile.testing.FakePlacesRepository
import com.wherever.mobile.testing.FakePlanAssistantRepository
import com.wherever.mobile.testing.FakePlansRepository
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.TEST_POINT
import com.wherever.mobile.testing.sampleDraft
import com.wherever.mobile.testing.sampleProfile
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-48: plan con IA, hoja rápida y edición del itinerario. */
@OptIn(ExperimentalCoroutinesApi::class)
class PlansViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    // Miércoles 18 de noviembre de 2026, 12:00 en Ciudad de México.
    private val clock = Clock.fixed(Instant.parse("2026-11-18T18:00:00Z"), ZoneId.of("America/Mexico_City"))
    private val assistant = FakePlanAssistantRepository()
    private val plans = FakePlansRepository()
    private val location = FakeLocationProvider(granted = true)
    private val profiles = FakeProfileRepository(sampleProfile())

    private fun TestScope.create(phrase: String = "antojo"): CreatePlanViewModel {
        val vm = CreatePlanViewModel(
            assistant,
            plans,
            profiles,
            location,
            clock,
            SavedStateHandle(
                mapOf(
                    "phrase" to phrase
                )
            )
        )
        advanceUntilIdle()
        return vm
    }

    private fun TestScope.detail(): PlanDetailViewModel {
        val vm = PlanDetailViewModel(plans, FakePlacesRepository(), clock, SavedStateHandle(mapOf("id" to "plan1")))
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `una palabra basta y manda ubicacion, personas y movilidad`() = runTest(mainDispatcher.dispatcher) {
        val vm = create()

        vm.onMobility(Mobility.CAR)
        vm.onPeople(4)
        vm.generate()
        advanceUntilIdle()

        val request = assistant.requests.single()
        assertEquals("antojo", request.phrase)
        assertEquals(TEST_POINT, request.near)
        assertEquals(4, request.people)
        assertEquals(Mobility.CAR, request.mobility)
        assertNull(request.dateTime)
        assertEquals(CreatePhase.RESULT, vm.state.value.phase)
    }

    @Test
    fun `presupuesto y animo van en la frase solo con consentimiento`() = runTest(mainDispatcher.dispatcher) {
        val vm = create("cena")
        vm.onBudget(400)
        vm.onMood(Mood.RELAXED)

        assertEquals("cena, con presupuesto de hasta $400 por persona", vm.buildPhrase(vm.state.value))

        profiles.profile.value = sampleProfile().let { it.copy(privacy = it.privacy.copy(mood = true)) }
        val allowed = create("cena")
        allowed.onMood(Mood.RELAXED)
        assertTrue(allowed.state.value.moodAllowed)
        assertEquals("cena, me siento relajado", allowed.buildPhrase(allowed.state.value))
    }

    @Test
    fun `hoy en la noche y manana dan la hora en la zona del telefono`() = runTest(mainDispatcher.dispatcher) {
        val vm = create()

        assertEquals("2026-11-18T20:00:00-06:00", vm.dateTimeFor(PlanWhen.TONIGHT))
        assertEquals("2026-11-19T14:00:00-06:00", vm.dateTimeFor(PlanWhen.TOMORROW))
        assertEquals("2026-11-18T14:00:00-06:00", vm.dateTimeFor(PlanWhen.LATER))
    }

    @Test
    fun `sin ubicacion no llama a la IA y lo avisa`() = runTest(mainDispatcher.dispatcher) {
        location.granted = false
        val vm = create()

        vm.generate()
        advanceUntilIdle()

        assertTrue(assistant.requests.isEmpty())
        assertTrue(vm.state.value.noLocation)
        assertEquals(CreatePhase.EDITING, vm.state.value.phase)
    }

    @Test
    fun `otra idea excluye lo ya propuesto`() = runTest(mainDispatcher.dispatcher) {
        val vm = create()
        vm.generate()
        advanceUntilIdle()

        vm.otherIdea()
        advanceUntilIdle()
        vm.otherIdea()
        advanceUntilIdle()

        assertEquals(listOf("n1", "n2"), assistant.excluded.first())
        assertEquals(listOf("n1", "n2", "n9"), assistant.excluded.last())
    }

    @Test
    fun `el respaldo por reglas tambien da un plan`() = runTest(mainDispatcher.dispatcher) {
        assistant.generateResult = Result.Success(sampleDraft(source = SOURCE_RULES))
        val vm = create()

        vm.generate()
        advanceUntilIdle()

        assertEquals(SOURCE_RULES, vm.state.value.draft?.source)
    }

    @Test
    fun `si falla vuelve a editar con el error`() = runTest(mainDispatcher.dispatcher) {
        assistant.generateResult = Result.Failure(AppError.RateLimited(60))
        val vm = create()

        vm.generate()
        advanceUntilIdle()

        assertEquals(CreatePhase.EDITING, vm.state.value.phase)
        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `guardar abre el plan nuevo`() = runTest(mainDispatcher.dispatcher) {
        val vm = create()
        vm.generate()
        advanceUntilIdle()

        vm.save()
        advanceUntilIdle()

        assertEquals("plan1", vm.saved.first())
        assertTrue("save" in plans.calls)
    }

    @Test
    fun `quitar parada y deshacer la vuelve a agregar con su hora`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()
        val stop = vm.stops()[1]

        vm.removeStop(stop)
        advanceUntilIdle()
        assertNotNull(vm.state.value.removed)

        vm.undoRemove()
        advanceUntilIdle()
        assertTrue("remove:s2" in plans.calls)
        assertTrue("add:n2" in plans.calls)
        assertEquals(stop.arrives, plans.lastArrives)
    }

    @Test
    fun `la ultima parada no se quita`() = runTest(mainDispatcher.dispatcher) {
        plans.plan = plans.plan.copy(stops = plans.plan.stops.take(1))
        val vm = detail()

        vm.removeStop(plans.plan.stops.single())
        advanceUntilIdle()

        assertFalse(plans.calls.any { it.startsWith("remove") })
        assertNotNull(vm.state.value.message)
    }

    @Test
    fun `subir y bajar cambia el orden`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()

        vm.moveDown(vm.stops()[0])
        advanceUntilIdle()

        assertEquals(listOf("s2", "s1", "s3"), plans.lastOrder)
    }

    @Test
    fun `mover la hora suma 15 minutos`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()

        vm.shiftTime(vm.stops()[0], forward = true)
        advanceUntilIdle()

        assertEquals("2026-11-18T22:15:00Z", plans.lastArrives)
    }

    @Test
    fun `cambiar por una alternativa`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()

        vm.openAlternatives(vm.stops()[0])
        advanceUntilIdle()
        vm.replaceWith(checkNotNull(vm.state.value.alternatives).items.single())
        advanceUntilIdle()

        assertTrue("replace:s1:n7" in plans.calls)
        assertNull(vm.state.value.alternatives)
    }

    @Test
    fun `agregar una parada 15 minutos despues de la ultima`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()
        vm.openAddStop()
        vm.onAddQuery("cafe")
        advanceUntilIdle()

        vm.addPlace(checkNotNull(vm.state.value.addStop).results.first())
        advanceUntilIdle()

        assertEquals("2026-11-19T01:45:00Z", plans.lastArrives)
    }

    @Test
    fun `un 409 recarga el plan y avisa`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()
        plans.editResult = Result.Failure(AppError.Conflict())

        vm.moveUp(vm.stops()[1])
        advanceUntilIdle()

        assertEquals(2, plans.calls.count { it == "get" })
        assertNotNull(vm.state.value.message)
    }

    @Test
    fun `compartir y repetir mandan eventos`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()

        vm.share()
        advanceUntilIdle()
        assertEquals(PlanEvent.Share("enlace-de-prueba/plan#codigo=abc"), vm.events.first())

        vm.repeatTomorrow()
        advanceUntilIdle()
        assertEquals(PlanEvent.Open("plan2"), vm.events.first())
        assertEquals("2026-11-19", plans.clonedTo)
    }

    @Test
    fun `renombrar recorta y no llama si no cambia`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()

        vm.startRename()
        vm.confirmRename()
        advanceUntilIdle()
        assertFalse(plans.calls.any { it.startsWith("rename") })

        vm.startRename()
        vm.onRename("  Cena  ")
        vm.confirmRename()
        advanceUntilIdle()
        assertTrue("rename:Cena" in plans.calls)
    }

    @Test
    fun `mis planes cambia de pestana`() = runTest(mainDispatcher.dispatcher) {
        val vm = MyPlansViewModel(plans)

        vm.onTab(PlansTab.PAST)
        advanceUntilIdle()

        assertEquals("list:pasados", plans.calls.last())
        assertEquals(1, vm.state.value.items.size)
    }

    private fun PlanDetailViewModel.stops() = checkNotNull(state.value.plan).stops
}
