package com.wherever.mobile.feature.places.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.places.domain.PlaceFilters
import com.wherever.mobile.feature.places.domain.PlacePage
import com.wherever.mobile.feature.places.domain.ReportReason
import com.wherever.mobile.testing.FakeLocationProvider
import com.wherever.mobile.testing.FakePlacesRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.NETWORK_FAILURE
import com.wherever.mobile.testing.samplePlace
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

/** nv-47: Explorar, ficha del lugar y favoritos. */
@OptIn(ExperimentalCoroutinesApi::class)
class PlacesViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val places = FakePlacesRepository()

    // Miércoles 18 de noviembre de 2026, 12:00 en Ciudad de México.
    private val clock = Clock.fixed(Instant.parse("2026-11-18T18:00:00Z"), ZoneId.of("America/Mexico_City"))

    private fun TestScope.explore(args: Map<String, Any?> = emptyMap()): ExploreViewModel {
        val vm = ExploreViewModel(places, FakeLocationProvider(), SavedStateHandle(args))
        advanceUntilIdle()
        return vm
    }

    private fun TestScope.detail(): PlaceDetailViewModel {
        val vm = PlaceDetailViewModel(
            places,
            places,
            places,
            clock,
            SavedStateHandle(
                mapOf(
                    "id" to "p1",
                    "origin" to "inicio"
                )
            )
        )
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `explorar abre con la categoria del chip de Inicio`() = runTest(mainDispatcher.dispatcher) {
        val vm = explore(mapOf("category" to "cafeteria"))

        assertEquals("cafeteria", places.listCalls.single().first.category)
        assertEquals(1, vm.state.value.results.size)
        assertEquals(1, vm.state.value.categories.size)
        assertEquals(1, vm.state.value.recentSearches.size)
    }

    @Test
    fun `buscar espera a que se deje de escribir`() = runTest(mainDispatcher.dispatcher) {
        val vm = explore()

        vm.onQueryChange("ta")
        vm.onQueryChange("tac")
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS - 1)
        runCurrent()
        assertTrue(places.searches.isEmpty())

        vm.onQueryChange("taco")
        advanceUntilIdle()
        assertEquals(listOf("taco"), places.searches)
    }

    @Test
    fun `una letra no busca y lista con filtros`() = runTest(mainDispatcher.dispatcher) {
        val vm = explore()

        vm.onQueryChange("t")
        advanceUntilIdle()

        assertTrue(places.searches.isEmpty())
        assertEquals(2, places.listCalls.size)
    }

    @Test
    fun `los filtros se encienden y se apagan`() = runTest(mainDispatcher.dispatcher) {
        val vm = explore()

        vm.onOpenNow()
        vm.onMaxPrice(2)
        advanceUntilIdle()
        assertEquals(PlaceFilters(openNow = true, maxPriceLevel = 2), places.listCalls.last().first)

        vm.onMaxPrice(2)
        advanceUntilIdle()
        assertNull(places.listCalls.last().first.maxPriceLevel)
    }

    @Test
    fun `sin resultados muestra el vacio y quitar filtros limpia todo`() = runTest(mainDispatcher.dispatcher) {
        places.listResult = Result.Success(PlacePage(emptyList(), null))
        val vm = explore()
        vm.onOpenNow()
        advanceUntilIdle()
        assertTrue(vm.state.value.showEmpty)

        vm.clearFilters()
        advanceUntilIdle()
        assertTrue(vm.state.value.filters.isEmpty)
    }

    @Test
    fun `cargar mas agrega la siguiente pagina`() = runTest(mainDispatcher.dispatcher) {
        places.listResult = Result.Success(PlacePage(listOf(samplePlace("p1")), "c2"))
        val vm = explore()
        places.listResult = Result.Success(PlacePage(listOf(samplePlace("p2")), null))

        vm.loadMore()
        advanceUntilIdle()

        assertEquals("c2", places.listCalls.last().second)
        assertEquals(listOf("p1", "p2"), vm.state.value.results.map { it.id })
        assertNull(vm.state.value.nextCursor)
    }

    @Test
    fun `la busqueda respeta los filtros sobre la pagina`() {
        val filters = PlaceFilters(category = "bar", openNow = true)
        assertTrue(filters.matches(samplePlace(category = "bar")))
        assertFalse(filters.matches(samplePlace(category = "bar", openNow = false)))
        assertFalse(filters.matches(samplePlace(category = "cafeteria")))
    }

    @Test
    fun `la ficha carga, registra la vista y marca hoy`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()

        assertNotNull(vm.state.value.place)
        assertEquals(listOf("p1" to "inicio"), places.views)
        assertEquals(3, vm.state.value.today)
        assertNotNull(vm.state.value.traffic)
    }

    @Test
    fun `el favorito cambia al instante y vuelve si falla`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()

        vm.toggleFavorite()
        advanceUntilIdle()
        assertTrue(vm.state.value.favorite)
        assertEquals(listOf("p1" to true), places.favoriteCalls)

        places.favoriteResult = NETWORK_FAILURE
        vm.toggleFavorite()
        advanceUntilIdle()
        assertTrue(vm.state.value.favorite)
    }

    @Test
    fun `reportar otro exige detalle`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()
        vm.openReport()
        vm.onReportReason(ReportReason.OTHER)
        assertFalse(vm.state.value.report?.canSend == true)

        vm.onReportDetail("Ya no abre los lunes")
        vm.sendReport()
        advanceUntilIdle()

        assertEquals(listOf(ReportReason.OTHER to "Ya no abre los lunes"), places.reports)
        assertNull(vm.state.value.report)
    }

    @Test
    fun `un 409 del reporte queda en el dialogo`() = runTest(mainDispatcher.dispatcher) {
        places.reportResult = Result.Failure(AppError.Conflict("ya tienes un reporte abierto"))
        val vm = detail()
        vm.openReport()
        vm.onReportReason(ReportReason.SCHEDULE)

        vm.sendReport()
        advanceUntilIdle()

        assertNotNull(vm.state.value.report?.error)
    }

    @Test
    fun `filtrar por estrellas y tocar otra vez lo quita`() = runTest(mainDispatcher.dispatcher) {
        val vm = detail()

        vm.onStarsFilter(5)
        advanceUntilIdle()
        assertEquals(5, places.reviewQueries.last().stars)

        vm.onStarsFilter(5)
        advanceUntilIdle()
        assertNull(places.reviewQueries.last().stars)
    }

    @Test
    fun `util solo se marca una vez y se desmarca si falla`() = runTest(mainDispatcher.dispatcher) {
        places.usefulResult = NETWORK_FAILURE
        val vm = detail()

        vm.markUseful("r1")
        advanceUntilIdle()

        assertFalse("r1" in vm.state.value.usefulMarked)
        assertNotNull(vm.state.value.message)
    }

    @Test
    fun `favoritos carga y quitar uno lo saca de la lista`() = runTest(mainDispatcher.dispatcher) {
        val vm = FavoritesViewModel(places)
        vm.load()
        advanceUntilIdle()
        assertEquals(1, vm.state.value.items.size)

        vm.remove("p1")
        advanceUntilIdle()
        assertTrue(vm.state.value.items.isEmpty())
        assertEquals(listOf("p1" to false), places.favoriteCalls)
    }
}
