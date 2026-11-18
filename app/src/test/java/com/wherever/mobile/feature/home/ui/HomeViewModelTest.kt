package com.wherever.mobile.feature.home.ui

import com.wherever.mobile.feature.home.domain.Company
import com.wherever.mobile.testing.FakeHomeRepository
import com.wherever.mobile.testing.FakeLocationPermissionPreferences
import com.wherever.mobile.testing.FakeLocationProvider
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.NETWORK_FAILURE
import com.wherever.mobile.testing.TEST_POINT
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

/** nv-47: Inicio con recomendaciones reales, ubicación, compañía y "No me interesa". */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val home = FakeHomeRepository()
    private val profiles = FakeProfileRepository(sampleProfile(name = "Ana López"))
    private val location = FakeLocationProvider()
    private val prefs = FakeLocationPermissionPreferences()

    private fun TestScope.viewModel(): HomeViewModel {
        val vm = HomeViewModel(home, profiles, location, prefs)
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `sin permiso pide la ubicacion y carga recomendaciones generales`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()

        val state = vm.state.value
        assertEquals("Ana", state.firstName)
        assertEquals(LocationPrompt.ASK, state.locationPrompt)
        assertEquals(listOf(null to null), home.feedCalls)
        assertEquals(2, state.recommendations.size)
        assertTrue(home.eventCalls.isEmpty())
        assertFalse(state.loading)
    }

    @Test
    fun `con permiso manda lat y lng y trae eventos`() = runTest(mainDispatcher.dispatcher) {
        location.granted = true
        val vm = viewModel()

        assertEquals(TEST_POINT, home.feedCalls.single().first)
        assertEquals(LocationPrompt.NONE, vm.state.value.locationPrompt)
        assertTrue(vm.state.value.usingLocation)
        assertEquals(1, vm.state.value.events.size)
    }

    @Test
    fun `la compania se manda y tocarla otra vez la quita`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()

        vm.onCompany(Company.COUPLE)
        advanceUntilIdle()
        assertEquals(Company.COUPLE, home.feedCalls.last().second)

        vm.onCompany(Company.COUPLE)
        advanceUntilIdle()
        assertNull(home.feedCalls.last().second)
        assertNull(vm.state.value.company)
    }

    @Test
    fun `no me interesa quita la tarjeta y deshacer la regresa a su lugar`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()
        val first = vm.state.value.recommendations.first()

        vm.notInterested(first)
        advanceUntilIdle()
        assertEquals(listOf("p1"), home.dismissed)
        assertEquals(listOf("p2"), vm.state.value.recommendations.map { it.id })
        assertNotNull(vm.state.value.dismissed)

        vm.undoNotInterested()
        advanceUntilIdle()
        assertEquals(listOf("p1"), home.undone)
        assertEquals(listOf("p1", "p2"), vm.state.value.recommendations.map { it.id })
        assertNull(vm.state.value.dismissed)
    }

    @Test
    fun `si la API rechaza el descarte la tarjeta vuelve`() = runTest(mainDispatcher.dispatcher) {
        home.dismissResult = NETWORK_FAILURE
        val vm = viewModel()

        vm.notInterested(vm.state.value.recommendations.first())
        advanceUntilIdle()

        assertEquals(2, vm.state.value.recommendations.size)
        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `dos rechazos del permiso dejan solo el aviso de Ajustes`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()

        vm.onPermissionResult(false)
        assertEquals(LocationPrompt.ASK, vm.state.value.locationPrompt)
        vm.onPermissionResult(false)
        assertEquals(LocationPrompt.SETTINGS, vm.state.value.locationPrompt)

        vm.onLocationNotNow()
        assertEquals(LocationPrompt.NONE, vm.state.value.locationPrompt)
    }

    @Test
    fun `al volver de Ajustes con permiso recarga con ubicacion`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()
        location.granted = true

        vm.onResume()
        advanceUntilIdle()

        assertEquals(TEST_POINT, home.feedCalls.last().first)
        assertEquals(LocationPrompt.NONE, vm.state.value.locationPrompt)
    }

    @Test
    fun `sin red muestra el error y deslizar reintenta`() = runTest(mainDispatcher.dispatcher) {
        home.feedResult = NETWORK_FAILURE
        val vm = viewModel()
        assertNotNull(vm.state.value.error)

        vm.refresh()
        advanceUntilIdle()

        assertEquals(2, home.feedCalls.size)
        assertFalse(vm.state.value.refreshing)
    }
}
