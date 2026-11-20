package com.wherever.mobile.feature.status.ui

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.ConnectivityMonitor
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.status.domain.AppStatus
import com.wherever.mobile.feature.status.domain.AppStatusRepository
import com.wherever.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeMonitor(initial: Boolean) : ConnectivityMonitor {
    val flow = MutableStateFlow(initial)
    override val online = flow
}

private class FakeStatusRepository : AppStatusRepository {
    var result: Result<AppStatus> = Result.Success(AppStatus("0.0.0", maintenance = false))
    var calls = 0

    override suspend fun status(): Result<AppStatus> {
        calls++
        return result
    }
}

/** 89_SinConexion, mantenimiento y versión mínima (specs/nv-16), con monitor de red falso inyectado. */
@OptIn(ExperimentalCoroutinesApi::class)
class AppStatusViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = FakeStatusRepository()

    private fun TestScope.viewModel(monitor: FakeMonitor, version: String = "0.7.0"): AppStatusViewModel {
        val vm = AppStatusViewModel(monitor, repository, version)
        runCurrent()
        return vm
    }

    @Test
    fun `con red revisa la version una vez y no muestra nada`() = runTest(mainRule.dispatcher) {
        val vm = viewModel(FakeMonitor(true))
        advanceUntilIdle()

        assertEquals(1, repository.calls)
        assertFalse(vm.state.value.offline)
        assertFalse(vm.state.value.updateRequired)
    }

    @Test
    fun `version minima mayor pide actualizar`() = runTest(mainRule.dispatcher) {
        repository.result = Result.Success(AppStatus("0.8.0", maintenance = false))
        val vm = viewModel(FakeMonitor(true))
        advanceUntilIdle()

        assertTrue(vm.state.value.updateRequired)
    }

    @Test
    fun `sin red muestra la pantalla tras el margen y se quita al volver`() = runTest(mainRule.dispatcher) {
        val monitor = FakeMonitor(true)
        val vm = viewModel(monitor)
        advanceUntilIdle()

        monitor.flow.value = false
        runCurrent()
        advanceTimeBy(OFFLINE_DEBOUNCE_MS - 100)
        assertFalse(vm.state.value.offline)
        advanceTimeBy(200)
        assertTrue(vm.state.value.offline)

        monitor.flow.value = true
        runCurrent()
        assertFalse(vm.state.value.offline)
    }

    @Test
    fun `un corte breve no muestra nada`() = runTest(mainRule.dispatcher) {
        val monitor = FakeMonitor(true)
        val vm = viewModel(monitor)
        advanceUntilIdle()

        monitor.flow.value = false
        runCurrent()
        advanceTimeBy(500)
        monitor.flow.value = true
        advanceUntilIdle()

        assertFalse(vm.state.value.offline)
    }

    @Test
    fun `reintentar sin red intenta la llamada y avisa que sigue sin conexion`() = runTest(mainRule.dispatcher) {
        val vm = viewModel(FakeMonitor(false))
        advanceUntilIdle()
        assertTrue(vm.state.value.offline)
        repository.result = Result.Failure(AppError.Network)

        vm.retry()
        advanceUntilIdle()

        // nv-40: nunca se bloquea la petición por un chequeo previo de red.
        assertEquals(1, repository.calls)
        assertEquals(StatusNotice.STILL_OFFLINE, vm.state.value.notice)
        assertTrue(vm.state.value.offline)
    }

    @Test
    fun `reintentar con red pero servidor caido lo distingue`() = runTest(mainRule.dispatcher) {
        val vm = viewModel(FakeMonitor(false))
        advanceUntilIdle()
        repository.result = Result.Failure(AppError.Server())

        vm.retry()
        advanceUntilIdle()

        assertTrue(vm.state.value.offline)
        assertEquals(StatusNotice.SERVER_DOWN, vm.state.value.notice)
        assertEquals(1, repository.calls)
    }

    @Test
    fun `reintentar con exito cierra el aviso`() = runTest(mainRule.dispatcher) {
        val vm = viewModel(FakeMonitor(false))
        advanceUntilIdle()

        vm.retry()
        advanceUntilIdle()

        assertFalse(vm.state.value.offline)
        assertNull(vm.state.value.notice)
    }

    @Test
    fun `mantenimiento bloquea con su propio texto`() = runTest(mainRule.dispatcher) {
        repository.result = Result.Success(AppStatus("0.0.0", maintenance = true))
        val vm = viewModel(FakeMonitor(true))
        advanceUntilIdle()

        assertTrue(vm.state.value.maintenance)
        assertTrue(vm.state.value.blocking)
        assertFalse(vm.state.value.offline)
    }

    @Test
    fun `atras cierra el aviso hasta el siguiente corte`() = runTest(mainRule.dispatcher) {
        val monitor = FakeMonitor(false)
        val vm = viewModel(monitor)
        advanceUntilIdle()

        vm.dismiss()
        assertFalse(vm.state.value.offline)

        monitor.flow.value = true
        advanceUntilIdle()
        monitor.flow.value = false
        advanceUntilIdle()
        assertTrue(vm.state.value.offline)
        assertNull(vm.state.value.notice)
    }
}
