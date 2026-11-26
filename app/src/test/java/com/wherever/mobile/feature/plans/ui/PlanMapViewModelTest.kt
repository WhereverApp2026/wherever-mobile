package com.wherever.mobile.feature.plans.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.Transfer
import com.wherever.mobile.feature.plans.domain.into
import com.wherever.mobile.feature.plans.domain.routeTotals
import com.wherever.mobile.feature.plans.domain.routeUrl
import com.wherever.mobile.testing.FakePlacesRepository
import com.wherever.mobile.testing.FakePlansRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.TEST_POINT
import com.wherever.mobile.testing.samplePlan
import com.wherever.mobile.testing.sampleStop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-30: 13_Mapa y 14_MapaFull con traslados reales y la app de mapas del teléfono (D-30f a D-30j). */
@OptIn(ExperimentalCoroutinesApi::class)
class PlanMapViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule(UnconfinedTestDispatcher())

    private val transfers = listOf(Transfer("s1", "s2", 8, 0.6), Transfer("s2", "s3", 12, 4.3))
    private val plans = FakePlansRepository().apply { plan = samplePlan().copy(transfers = transfers) }
    private val places = FakePlacesRepository()

    private fun create(full: Boolean = false) =
        PlanMapViewModel(plans, places, SavedStateHandle(mapOf("id" to "plan1", "full" to full)))

    @Test
    fun `totales suman los traslados y miden del inicio al fin de la ultima parada`() {
        val stops = samplePlan().stops
        val totals = routeTotals(stops, transfers)

        assertEquals(20, totals.transferMinutes)
        assertEquals(4.9, totals.km, 0.001)
        // 22:00 → 00:30 del día siguiente + 60 min de la última parada.
        assertEquals(210L, totals.spanMinutes)
        assertNull(routeTotals(listOf(sampleStop("x", "n", "sin fecha")), emptyList()).spanMinutes)
        assertEquals(8, transfers.into("s2")?.minutes)
        assertNull(transfers.into("s1"))
    }

    @Test
    fun `la URL de Iniciar ruta lleva destino, paradas intermedias y el modo de la movilidad`() {
        val viewModel = create()
        val url = routeUrl(viewModel.state.value.points, Mobility.CAR)!!

        assertTrue(url.startsWith("https://www.google.com/maps/dir/?api=1&destination="))
        assertTrue(url.contains("&waypoints="))
        assertTrue(url.endsWith("&travelmode=driving"))
        assertNull(routeUrl(emptyList(), Mobility.WALK))
    }

    @Test
    fun `carga el plan y una ficha por lugar con sus coordenadas`() {
        val viewModel = create()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertEquals(listOf(1, 2, 3), state.points.map { it.number })
        assertEquals(TEST_POINT.lat, state.points.first().lat, 0.0)
        assertEquals(listOf("n1", "n2", "n3"), places.detailCalls)
        assertEquals(0, state.missing)
        assertEquals(20, state.totals.transferMinutes)
    }

    @Test
    fun `si una ficha falla la parada queda fuera del mapa y se avisa`() {
        places.detailResult = Result.Failure(AppError.Network)

        val state = create().state.value

        assertTrue(state.points.isEmpty())
        assertEquals(3, state.missing)
        assertNull(state.error)
    }

    @Test
    fun `si el plan no carga muestra error con Reintentar`() {
        plans.getResult = Result.Failure(AppError.Network)
        val viewModel = create()
        assertNotNull(viewModel.state.value.error)

        plans.getResult = null
        viewModel.load()

        assertNull(viewModel.state.value.error)
        assertEquals(3, viewModel.state.value.points.size)
    }

    @Test
    fun `Como llegar abre la primera parada y Iniciar ruta todas`() = runTest(mainDispatcher.dispatcher) {
        val viewModel = create(full = true)
        assertTrue(viewModel.state.value.full)

        viewModel.events.test {
            viewModel.directionsToFirst()
            val directions = awaitItem() as PlanMapEvent.Directions
            assertEquals("Lugar n1", directions.name)

            viewModel.startRoute()
            val route = awaitItem() as PlanMapEvent.StartRoute
            assertTrue(route.url.endsWith("&travelmode=walking"))
        }
    }

    @Test
    fun `sin app de mapas avisa`() {
        val viewModel = create()

        viewModel.noMapsApp()

        assertNotNull(viewModel.state.value.message)
    }
}
