package com.wherever.mobile.feature.vaca.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.vaca.domain.SplitMode
import com.wherever.mobile.feature.vaca.domain.parsePesos
import com.wherever.mobile.feature.vaca.domain.splitEqually
import com.wherever.mobile.testing.FakeGroupsRepository
import com.wherever.mobile.testing.FakePlansRepository
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.FakeVacaRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import com.wherever.mobile.testing.vacaGroup
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

/** nv-26: el ejemplo de la maqueta, redondeo, marcar pagado y agregar gasto. */
@OptIn(ExperimentalCoroutinesApi::class)
class VacaViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val vaca = FakeVacaRepository(Result.Success(sampleVacaSummary()))
    private val groups = FakeGroupsRepository().apply { groupResult = Result.Success(vacaGroup()) }

    // sampleProfile() tiene id "u1": es «Tú» en el ejemplo.
    private val profiles = FakeProfileRepository(sampleProfile())
    private val handle = SavedStateHandle(mapOf("id" to "g1"))

    private fun TestScope.vacaVm(): VacaViewModel {
        val vm = VacaViewModel(handle, vaca, groups, FakePlansRepository(), profiles)
        vm.load()
        advanceUntilIdle()
        return vm
    }

    private fun TestScope.expenseVm(mode: String = "igual"): AddExpenseViewModel {
        val vm = AddExpenseViewModel(SavedStateHandle(mapOf("id" to "g1", "mode" to mode)), vaca, groups, profiles)
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `760 entre 4 da 190 y sin centavos perdidos`() {
        assertEquals(listOf(19_000L, 19_000L, 19_000L, 19_000L), splitEqually(76_000, 4))
        assertEquals(listOf(34L, 33L, 33L), splitEqually(100, 3))
        assertEquals(100L, splitEqually(100, 3).sum())
        assertEquals(1_000_001L, splitEqually(1_000_001, 7).sum())
        assertTrue(splitEqually(100, 0).isEmpty())
    }

    @Test
    fun `los montos escritos pasan a centavos enteros`() {
        assertEquals(48_000L, parsePesos("480"))
        assertEquals(48_050L, parsePesos("480.5"))
        assertEquals(120_075L, parsePesos("1,200.75"))
        assertNull(parsePesos("12.345"))
        assertNull(parsePesos("abc"))
        assertEquals("120.50", centsToText(12_050))
        assertEquals("120", centsToText(12_000))
    }

    @Test
    fun `la vaca de la maqueta da 190 por persona y Jorge debe 70`() = runTest(mainDispatcher.dispatcher) {
        val vm = vacaVm()
        val state = vm.state.value

        assertEquals(76_000L, state.summary?.totalCents)
        assertEquals(19_000L, state.perPersonCents)
        val jorge = state.summary!!.debts.single { it.debtorId == "u2" }
        assertEquals(7_000L, jorge.amountCents)
        assertTrue(state.canPay(jorge))
        assertEquals("Tacos", state.title)
    }

    @Test
    fun `marcar pagado una sola vez y recalcula`() = runTest(mainDispatcher.dispatcher) {
        val vm = vacaVm()
        val debt = vm.state.value.summary!!.debts.first()

        vm.markPaid(debt)
        vm.markPaid(debt)
        advanceUntilIdle()

        assertEquals(listOf(debt.id), vaca.paid)
        assertEquals(UiText.Res(R.string.vaca_marked_paid), vm.state.value.message)
        assertEquals(2, vaca.summaryCalls)
    }

    @Test
    fun `una deuda ya pagada avisa y recarga`() = runTest(mainDispatcher.dispatcher) {
        vaca.payResult = Result.Failure(AppError.Conflict())
        val vm = vacaVm()

        vm.markPaid(vm.state.value.summary!!.debts.first())
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.vaca_already_paid), vm.state.value.message)
    }

    @Test
    fun `quien no es deudor ni acreedor no puede marcar pagado`() = runTest(mainDispatcher.dispatcher) {
        profiles.profile.value = sampleProfile().copy(id = "u9")
        val vm = vacaVm()

        val debt = vm.state.value.summary!!.debts.first()
        assertFalse(vm.state.value.canPay(debt))
        vm.markPaid(debt)
        advanceUntilIdle()
        assertTrue(vaca.paid.isEmpty())
    }

    @Test
    fun `sin red muestra el error`() = runTest(mainDispatcher.dispatcher) {
        vaca.summaryResult = Result.Failure(AppError.Network)
        val vm = vacaVm()

        assertNotNull(vm.state.value.error)
        assertNull(vm.state.value.summary)
    }

    @Test
    fun `agregar gasto empieza contigo pagando y dividiendo entre los confirmados`() =
        runTest(mainDispatcher.dispatcher) {
            val vm = expenseVm()
            val state = vm.state.value

            assertEquals("u1", state.paidBy)
            assertEquals(listOf("u1", "u2", "u3", "u4"), state.members.map { it.userId })
            assertEquals(setOf("u1", "u2", "u3", "u4"), state.splitBetween)
            assertFalse(state.canSave)

            vm.onAmount("480")
            assertEquals(12_000L, vm.state.value.eachCents)
            assertTrue(vm.state.value.canSave)
        }

    @Test
    fun `monto mayor que 0 y al menos dos personas`() = runTest(mainDispatcher.dispatcher) {
        val vm = expenseVm()
        vm.onAmount("0")
        assertFalse(vm.state.value.canSave)
        vm.onAmount("480")
        listOf("u2", "u3", "u4").forEach(vm::toggleMember)
        assertFalse(vm.state.value.canSave)
        vm.toggleMember("u2")
        assertTrue(vm.state.value.canSave)
    }

    @Test
    fun `personalizado guarda solo si la suma iguala el monto`() = runTest(mainDispatcher.dispatcher) {
        val vm = expenseVm(mode = "personalizada")
        assertEquals(SplitMode.CUSTOM, vm.state.value.mode)
        vm.onAmount("100")

        vm.fillEqually()
        assertEquals(0L, vm.state.value.customRemaining)
        assertEquals(mapOf("u1" to "25", "u2" to "25", "u3" to "25", "u4" to "25"), vm.state.value.custom)

        vm.onCustom("u1", "40")
        assertEquals(-1_500L, vm.state.value.customRemaining)
        assertFalse(vm.state.value.canSave)
        vm.onCustom("u2", "10")
        assertTrue(vm.state.value.canSave)

        vm.save()
        advanceUntilIdle()
        val sent = vaca.added.single()
        assertEquals(mapOf("u1" to 4_000L, "u2" to 1_000L, "u3" to 2_500L, "u4" to 2_500L), sent.customCents)
        assertEquals(Unit, vm.saved.first())
    }

    @Test
    fun `si falla conserva lo escrito y reintenta con el mismo id_cliente`() = runTest(mainDispatcher.dispatcher) {
        vaca.addResult = Result.Failure(AppError.Network)
        val vm = expenseVm()
        vm.onDescription("Tacos")
        vm.onAmount("480")

        vm.save()
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)
        assertEquals("480", vm.state.value.amount)

        vaca.addResult = Result.Success(Unit)
        vm.save()
        advanceUntilIdle()
        assertEquals(1, vaca.added.map { it.clientId }.distinct().size)
        assertEquals(2, vaca.added.size)
        assertEquals("p1", vaca.added.last().planId)
    }
}
