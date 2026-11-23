package com.wherever.mobile.feature.places.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.places.domain.MapArea
import com.wherever.mobile.feature.places.domain.MapPin
import com.wherever.mobile.feature.places.domain.PlaceMap
import com.wherever.mobile.feature.places.domain.PlaceMapRepository
import com.wherever.mobile.testing.FakeLocationProvider
import com.wherever.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-21 (Unit): 32_ExpMapa con la zona visible, filtros y «Buscar en esta zona». */
@OptIn(ExperimentalCoroutinesApi::class)
class ExploreMapViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val fallback = GeoPoint(19.43, -99.13)
    private val pin = MapPin("n1", "Café Tacuba", "cafeteria", 4.6, 19.43, -99.13)

    private class FakeMap(var result: Result<PlaceMap>) : PlaceMapRepository {
        val calls = mutableListOf<Pair<MapArea, String?>>()

        override suspend fun area(area: MapArea, category: String?): Result<PlaceMap> {
            calls += area to category
            return result
        }
    }

    private val area = MapArea.of(19.40, -99.16, 19.46, -99.10, 15.0)

    @Test
    fun `sin permiso centra en el respaldo y la primera zona busca sola`() = runTest {
        val map = FakeMap(Result.Success(PlaceMap(false, listOf(pin), emptyList())))
        val vm = ExploreMapViewModel(map, FakeLocationProvider(granted = false), fallback)
        advanceUntilIdle()
        assertEquals(fallback, vm.state.value.center)

        vm.onArea(area)
        advanceUntilIdle()
        assertEquals(1, map.calls.size)
        assertEquals(listOf(pin), vm.state.value.pins)
        assertFalse(vm.state.value.moved)
    }

    @Test
    fun `mover el mapa pide confirmar y el filtro vuelve a buscar`() = runTest {
        val map = FakeMap(Result.Success(PlaceMap(false, listOf(pin), emptyList())))
        val vm = ExploreMapViewModel(map, FakeLocationProvider(granted = true), fallback)
        vm.onArea(area)
        advanceUntilIdle()

        vm.onArea(area.copy(zoom = 16))
        assertTrue(vm.state.value.moved)
        assertEquals(1, map.calls.size)
        vm.searchHere()
        advanceUntilIdle()
        assertEquals(2, map.calls.size)

        vm.selectCategory("bar")
        advanceUntilIdle()
        assertEquals("bar", map.calls.last().second)
        vm.selectCategory("bar")
        advanceUntilIdle()
        assertNull("tocarlo otra vez lo quita", map.calls.last().second)
    }

    @Test
    fun `tocar un pin muestra su tarjeta y una zona vacia avisa`() = runTest {
        val map = FakeMap(Result.Success(PlaceMap(false, emptyList(), emptyList())))
        val vm = ExploreMapViewModel(map, FakeLocationProvider(granted = false), fallback)
        vm.onArea(area)
        advanceUntilIdle()
        assertTrue(vm.state.value.empty)

        vm.select(pin)
        assertEquals("Café Tacuba", vm.state.value.selected?.name)
    }

    @Test
    fun `sin red muestra el error y reintenta la misma zona`() = runTest {
        val map = FakeMap(Result.Failure(AppError.Network))
        val vm = ExploreMapViewModel(map, FakeLocationProvider(granted = false), fallback)
        vm.onArea(area)
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.error_network), vm.state.value.error)
        vm.retry()
        advanceUntilIdle()
        assertEquals(listOf(area, area), map.calls.map { it.first })
    }

    @Test
    fun `la zona se recorta a lo que acepta la API`() {
        val wide = MapArea.of(-95.0, -200.0, 95.0, 200.0, 1.0)
        assertEquals(-90.0, wide.minLat, 0.0)
        assertEquals(180.0, wide.maxLng, 0.0)
        assertEquals(MapArea.MIN_ZOOM, wide.zoom)
    }
}
