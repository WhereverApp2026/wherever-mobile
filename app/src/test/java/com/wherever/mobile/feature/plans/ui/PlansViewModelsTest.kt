package com.wherever.mobile.feature.plans.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.PlansTab
import com.wherever.mobile.feature.profile.domain.Tastes
import com.wherever.mobile.testing.FakeLocationProvider
import com.wherever.mobile.testing.FakePlacesRepository
import com.wherever.mobile.testing.FakePlanAssistantRepository
import com.wherever.mobile.testing.FakePlansRepository
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.TEST_POINT
import com.wherever.mobile.testing.sampleDraft
import com.wherever.mobile.testing.samplePlan
import com.wherever.mobile.testing.sampleProfile
import com.wherever.mobile.testing.sampleStop
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-48 y nv-23: plan con IA (IAPalabra, 17_AjustaPlan y Generando) y edición del itinerario. */
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
        assertEquals(CreatePhase.READY, vm.state.value.phase)
        vm.showPlan()
        assertEquals(CreatePhase.RESULT, vm.state.value.phase)
    }

    @Test
    fun `presupuesto y animo van en la frase solo con consentimiento`() = runTest(mainDispatcher.dispatcher) {
        val vm = create("cena")
        vm.onPrice(PriceLevel.MEDIUM)
        vm.onMood(Mood.RELAXED)

        assertEquals("cena, con presupuesto de hasta $500 por persona", vm.buildPhrase(vm.state.value))

        profiles.profile.value = sampleProfile().let { it.copy(privacy = it.privacy.copy(mood = true)) }
        val allowed = create("cena")
        allowed.onMood(Mood.RELAXED)
        assertTrue(allowed.state.value.moodAllowed)
        assertEquals("cena, me siento relajado", allowed.buildPhrase(allowed.state.value))
    }

    @Test
    fun `dia y hora dan la fecha en la zona del telefono`() = runTest(mainDispatcher.dispatcher) {
        val vm = create()

        assertNull(vm.dateTimeFor(null, null))
        assertEquals("2026-11-18T20:00:00-06:00", vm.dateTimeFor(null, LocalTime.of(20, 0)))
        assertEquals("2026-11-19T09:00:00-06:00", vm.dateTimeFor(LocalDate.of(2026, 11, 19), null))
    }

    @Test
    fun `una hora que ya paso o a mas de 30 dias no deja armar`() = runTest(mainDispatcher.dispatcher) {
        val vm = create()

        vm.onTime(LocalTime.of(8, 0))
        assertTrue(vm.state.value.invalidDateTime)
        assertFalse(vm.state.value.canGenerate)

        vm.onDate(LocalDate.of(2026, 12, 20))
        assertTrue(vm.state.value.invalidDateTime)

        vm.onDate(LocalDate.of(2026, 11, 20))
        assertFalse(vm.state.value.invalidDateTime)
        assertTrue(vm.state.value.canGenerate)
    }

    @Test
    fun `sin ubicacion no llama a la IA y lo avisa`() = runTest(mainDispatcher.dispatcher) {
        location.granted = false
        val vm = create()

        vm.generate()
        advanceUntilIdle()

        assertTrue(assistant.requests.isEmpty())
        assertTrue(vm.state.value.noLocation)
        assertEquals(CreatePhase.WORD, vm.state.value.phase)
    }

    @Test
    fun `otra idea excluye lo ya propuesto`() = runTest(mainDispatcher.dispatcher) {
        val vm = create()
        vm.generate()
        advanceUntilIdle()

        vm.showPlan()
        vm.otherIdea()
        advanceUntilIdle()
        vm.showPlan()
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

        assertEquals(CreatePhase.WORD, vm.state.value.phase)
        assertNotNull(vm.state.value.error)
        assertEquals("antojo", vm.state.value.phrase)
    }

    @Test
    fun `los pasos avanzan con el tiempo y el cuarto llega con la respuesta`() = runTest(mainDispatcher.dispatcher) {
        val gate = CompletableDeferred<Unit>()
        assistant.beforeAnswer = { gate.await() }
        val vm = create()

        vm.generate()
        runCurrent()
        assertEquals(CreatePhase.GENERATING, vm.state.value.phase)
        assertEquals(0, vm.state.value.step)
        advanceTimeBy(STEP_MILLIS + 1)
        assertEquals(1, vm.state.value.step)
        advanceTimeBy(STEP_MILLIS * 5)
        assertEquals(2, vm.state.value.step)

        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(CreatePhase.READY, vm.state.value.phase)
        assertEquals(GENERATING_STEPS - 1, vm.state.value.step)
    }

    @Test
    fun `cancelar regresa al formulario de donde se pidio con lo escrito`() = runTest(mainDispatcher.dispatcher) {
        assistant.beforeAnswer = { CompletableDeferred<Unit>().await() }
        val vm = create("tacos")
        vm.adjust()
        vm.onCompany(Company.COUPLE)

        vm.generate()
        runCurrent()
        vm.cancel()
        advanceUntilIdle()

        assertEquals(CreatePhase.ADJUST, vm.state.value.phase)
        assertEquals("tacos", vm.state.value.phrase)
        assertEquals(Company.COUPLE, vm.state.value.company)
    }

    @Test
    fun `sin red avisa que no se arman planes sin internet`() = runTest(mainDispatcher.dispatcher) {
        assistant.generateResult = Result.Failure(AppError.Network)
        val vm = create()

        vm.generate()
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.plan_offline), vm.state.value.error)
        assertEquals(CreatePhase.WORD, vm.state.value.phase)
    }

    @Test
    fun `sin resultados propone ajustar los detalles`() = runTest(mainDispatcher.dispatcher) {
        assistant.generateResult = Result.Failure(AppError.Validation())
        val vm = create()

        vm.generate()
        advanceUntilIdle()

        assertEquals(CreatePhase.ADJUST, vm.state.value.phase)
        assertEquals(UiText.Res(R.string.plan_no_results), vm.state.value.error)
    }

    @Test
    fun `quien va y paradas van en la frase dentro de 2 a 4`() = runTest(mainDispatcher.dispatcher) {
        val vm = create("tacos")
        vm.onCompany(Company.FRIENDS)
        vm.onStops(9)
        assertEquals(AI_MAX_STOPS, vm.state.value.stops)
        vm.onStops(1)
        assertEquals(AI_MIN_STOPS, vm.state.value.stops)
        vm.onPeople(0)
        assertEquals(1, vm.state.value.people)

        assertEquals("tacos, con amigos, con 2 paradas", vm.buildPhrase(vm.state.value))
    }

    @Test
    fun `los gustos solo se muestran con aprendizaje personalizado`() = runTest(mainDispatcher.dispatcher) {
        val tastes = Tastes(listOf("cafes", "tacos"), 0, 0, emptyList(), "")
        profiles.profile.value = sampleProfile(tastes = tastes)
        assertEquals(listOf("cafes", "tacos"), create().state.value.tastes)

        profiles.profile.value = sampleProfile(tastes = tastes).let {
            it.copy(privacy = it.privacy.copy(personalizedLearning = false))
        }
        assertTrue(create().state.value.tastes.isEmpty())
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
    fun `cambiar por una alternativa elegida`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()

        vm.openAlternatives(vm.stops()[0])
        advanceUntilIdle()
        vm.replaceSelected()
        assertTrue(plans.calls.none { it.startsWith("replace") })

        vm.selectAlternative(checkNotNull(vm.state.value.alternatives).items.single())
        vm.replaceSelected()
        advanceUntilIdle()

        assertTrue("replace:s1:n7" in plans.calls)
        assertNull(vm.state.value.alternatives)
    }

    @Test
    fun `buscar otro lugar pide alternativas por nombre desde 2 letras`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()
        vm.openAlternatives(vm.stops()[1])
        advanceUntilIdle()

        vm.openAlternativeSearch()
        vm.onAlternativeQuery("m")
        advanceUntilIdle()
        vm.onAlternativeQuery("mirador")
        advanceUntilIdle()

        assertEquals(listOf("s2" to null, "s2" to "mirador"), plans.alternativeQueries)
        assertTrue(checkNotNull(vm.state.value.alternatives).searchOpen)
    }

    @Test
    fun `quitar esta parada desde la hoja`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()
        vm.openAlternatives(vm.stops()[2])
        advanceUntilIdle()

        vm.removeFromSheet()
        advanceUntilIdle()

        assertTrue("remove:s3" in plans.calls)
        assertNull(vm.state.value.alternatives)
    }

    @Test
    fun `sugeridas son las alternativas de la ultima parada`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()

        vm.openAddStop()
        advanceUntilIdle()
        val sheet = checkNotNull(vm.state.value.addStop)
        assertFalse(sheet.loadingSuggestions)
        assertEquals("s3" to null, plans.alternativeQueries.single())

        vm.addSuggestion(sheet.suggestions.single())
        advanceUntilIdle()
        assertTrue("add:n7" in plans.calls)
        assertEquals("2026-11-19T01:45:00Z", plans.lastArrives)
    }

    @Test
    fun `con 4 paradas no se agrega otra`() = runTest(mainDispatcher.dispatcher) {
        plans.plan = samplePlan().let { it.copy(stops = it.stops + sampleStop("s4", "n4", "2026-11-19T01:45:00Z")) }
        val vm = detail()

        vm.openAddStop()

        assertNull(vm.state.value.addStop)
    }

    @Test
    fun `un 403 al editar un plan ajeno no cambia el plan en pantalla`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()
        val before = vm.state.value.plan
        plans.editResult = Result.Failure(AppError.Forbidden("no eres el dueño"))

        vm.moveDown(vm.stops()[0])
        advanceUntilIdle()

        assertEquals(before, vm.state.value.plan)
        assertNotNull(vm.state.value.message)
        assertFalse(vm.state.value.busy)
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
