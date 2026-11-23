package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.business.domain.CAPACITY_MAX
import com.wherever.mobile.feature.business.domain.CapacityConfig
import com.wherever.mobile.feature.business.domain.CouponGroup
import com.wherever.mobile.feature.business.domain.CouponType
import com.wherever.mobile.feature.business.domain.HourSlot
import com.wherever.mobile.feature.business.domain.TURN_MAX
import com.wherever.mobile.feature.business.domain.isValidCapacity
import com.wherever.mobile.feature.business.domain.isValidCoupon
import com.wherever.mobile.testing.FakeBusinessCouponsRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.businessClock
import com.wherever.mobile.testing.businessToday
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-37: reglas del cupón, interruptor con 409 y cupo en múltiplos de 15 y de 1 a 500. */
@OptIn(ExperimentalCoroutinesApi::class)
class CouponsAndCapacityViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeBusinessCouponsRepository()

    @Test
    fun `reglas por tipo, titulo de 1 a 40 y fin despues del inicio`() {
        val end = businessToday.plusDays(1)
        assertTrue(isValidCoupon("15%", CouponType.PERCENT, 15, businessToday, end))
        assertFalse(isValidCoupon("x", CouponType.PERCENT, 0, businessToday, end))
        assertFalse(isValidCoupon("x", CouponType.PERCENT, 101, businessToday, end))
        assertFalse(isValidCoupon("x", CouponType.AMOUNT, 0, businessToday, end))
        assertTrue(isValidCoupon("x", CouponType.TWO_FOR_ONE, null, businessToday, end))
        assertFalse(isValidCoupon(" ", CouponType.GIFT, null, businessToday, end))
        assertFalse(isValidCoupon("x".repeat(41), CouponType.GIFT, null, businessToday, end))
        assertFalse(isValidCoupon("x", CouponType.GIFT, null, businessToday, businessToday))
    }

    @Test
    fun `publicar manda el valor en centavos, dias, horas y limite`() = runTest(mainDispatcher.dispatcher) {
        val vm = CouponFormViewModel(repository, businessClock)
        vm.start("n1")
        vm.onType(CouponType.AMOUNT)
        vm.onValue("50")
        vm.onTitle("$50 de descuento")
        vm.toggleDay(1)
        vm.toggleDay(4)
        vm.onHoursFrom("12:00")
        vm.onHoursTo("19:00")
        vm.onLimit(50)
        assertTrue(vm.state.value.canPublish)

        vm.publish()
        advanceUntilIdle()

        val sent = checkNotNull(repository.created)
        assertEquals(5_000L, sent.value)
        assertEquals(setOf(1, 4), sent.days.toSet())
        assertEquals(LocalTime.of(12, 0), sent.hoursFrom)
        assertEquals(50, sent.usageLimit)
        assertTrue(sent.end.isAfter(sent.start))
        assertEquals(Unit, vm.published.first())
    }

    @Test
    fun `horas a medias no dejan publicar y 2x1 no lleva valor`() = runTest(mainDispatcher.dispatcher) {
        val vm = CouponFormViewModel(repository, businessClock)
        vm.start("n1")
        vm.onValue("15")
        vm.onType(CouponType.TWO_FOR_ONE)
        assertEquals("", vm.state.value.value)
        assertNull(vm.state.value.numericValue)
        vm.onTitle("2x1")
        vm.onHoursFrom("12:00")
        assertFalse(vm.state.value.canPublish)
        vm.onHoursTo("19:00")
        assertTrue(vm.state.value.canPublish)
        vm.onLimit(-10)
        assertEquals(0, vm.state.value.usageLimit)
    }

    @Test
    fun `sugerir titulo con IA y sin IA muestra el error`() = runTest(mainDispatcher.dispatcher) {
        val vm = CouponFormViewModel(repository, businessClock)
        vm.start("n1")

        vm.suggestTitle()
        advanceUntilIdle()
        assertEquals("2x1 de tarde", vm.state.value.title)

        repository.titleResult = Result.Failure(AppError.Server())
        vm.suggestTitle()
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)
        assertEquals("2x1 de tarde", vm.state.value.title)
    }

    @Test
    fun `cupones por pestana y el interruptor pausa`() = runTest(mainDispatcher.dispatcher) {
        val vm = CouponsViewModel(repository)
        vm.start("n1")
        advanceUntilIdle()

        assertEquals(2, vm.state.value.count(CouponGroup.ACTIVE))
        vm.selectTab(CouponGroup.SCHEDULED)
        assertEquals(listOf("c3"), vm.state.value.visible.map { it.id })

        val active = vm.state.value.coupons.first { it.id == "c1" }
        vm.toggle(active)
        advanceUntilIdle()
        assertTrue("estado c1 false" in repository.calls)

        // Los programados no tienen interruptor.
        vm.toggle(vm.state.value.coupons.first { it.id == "c3" })
        advanceUntilIdle()
        assertTrue(repository.calls.none { it.startsWith("estado c3") })
    }

    @Test
    fun `409 al cambiar el estado avisa y recarga`() = runTest(mainDispatcher.dispatcher) {
        repository.setResult = Result.Failure(AppError.Conflict("con_canjes"))
        val vm = CouponsViewModel(repository)
        vm.start("n1")
        advanceUntilIdle()

        vm.toggle(vm.state.value.coupons.first())
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.coupons_changed), vm.state.value.message)
        assertEquals(2, repository.calls.count { it == "cupones" })
    }

    @Test
    fun `cupo de 1 a 500 y turno en multiplos de 15`() = runTest(mainDispatcher.dispatcher) {
        assertTrue(isValidCapacity(12, 60))
        assertFalse(isValidCapacity(0, 60))
        assertFalse(isValidCapacity(501, 60))
        assertFalse(isValidCapacity(12, 50))
        assertFalse(isValidCapacity(12, 255))

        val vm = CapacityViewModel(repository, businessClock)
        vm.start("n1")
        advanceUntilIdle()
        assertFalse(vm.state.value.canSave)

        vm.onCapacity(900)
        assertEquals(CAPACITY_MAX, vm.state.value.capacity)
        vm.onTurn(50)
        assertEquals(45, vm.state.value.turnMinutes)
        vm.onTurn(1_000)
        assertEquals(TURN_MAX, vm.state.value.turnMinutes)
        vm.onTurn(5)
        assertEquals(15, vm.state.value.turnMinutes)
        assertTrue(vm.state.value.canSave)

        vm.save()
        advanceUntilIdle()
        assertEquals(CAPACITY_MAX, repository.savedConfig?.capacity)
        assertEquals(UiText.Res(R.string.capacity_saved), vm.state.value.message)
        assertFalse(vm.state.value.dirty)
    }

    @Test
    fun `ocupacion por hora y cambiar de dia`() = runTest(mainDispatcher.dispatcher) {
        val vm = CapacityViewModel(repository, businessClock)
        vm.start("n1")
        advanceUntilIdle()

        assertEquals(0.25f, vm.state.value.occupancy(HourSlot("12:00", true, 9)))
        assertEquals(1f, vm.state.value.occupancy(HourSlot("13:00", false, 0)))
        assertEquals(businessToday, repository.availabilityDates.last())

        vm.onDay(businessToday.plusDays(2))
        advanceUntilIdle()
        assertEquals(businessToday.plusDays(2), repository.availabilityDates.last())
        assertEquals(CAPACITY_DAYS, vm.state.value.days.size)
    }

    @Test
    fun `sin reservas configuradas lo explica y 409 recarga`() = runTest(mainDispatcher.dispatcher) {
        repository.capacityResult = Result.Failure(AppError.NotFound())
        val vm = CapacityViewModel(repository, businessClock)
        vm.start("n1")
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.capacity_not_configured), vm.state.value.error)

        repository.capacityResult = Result.Success(CapacityConfig(12, 60, 3))
        vm.load()
        advanceUntilIdle()
        repository.saveResult = Result.Failure(AppError.Conflict())
        vm.onCapacity(10)
        vm.save()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.capacity_changed), vm.state.value.message)
    }
}
