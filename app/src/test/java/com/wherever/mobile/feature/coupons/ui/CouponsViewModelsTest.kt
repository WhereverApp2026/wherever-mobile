package com.wherever.mobile.feature.coupons.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.coupons.domain.CouponsTab
import com.wherever.mobile.feature.coupons.domain.Expiry
import com.wherever.mobile.feature.coupons.domain.RedeemCode
import com.wherever.mobile.feature.coupons.domain.expiryOf
import com.wherever.mobile.testing.FakeCouponRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleWalletCoupon
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val MX = ZoneId.of("America/Mexico_City")

/** 25 de noviembre de 2026, 12:00 en CDMX. */
private val NOW: Clock = Clock.fixed(Instant.parse("2026-11-25T18:00:00Z"), MX)

/** nv-29: vigencia, pestañas de 29_Cupones y código de canje de 47_DetCupon. */
@OptIn(ExperimentalCoroutinesApi::class)
class CouponsViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule(UnconfinedTestDispatcher())

    private val repository = FakeCouponRepository()

    @Test
    fun `vigencia hoy, en dias, en fecha y vencido con el reloj fijo`() {
        assertEquals(Expiry.Today, expiryOf("2026-11-26T05:00:00Z", NOW))
        assertEquals(Expiry.InDays(3), expiryOf("2026-11-28T20:00:00Z", NOW))
        assertTrue(Expiry.InDays(3).soon)
        assertEquals(Expiry.OnDate(LocalDate.of(2026, 12, 15)), expiryOf("2026-12-15T20:00:00Z", NOW))
        assertFalse(Expiry.OnDate(LocalDate.of(2026, 12, 15)).soon)
        assertEquals(Expiry.Expired, expiryOf("2026-11-25T17:00:00Z", NOW))
        assertEquals(Expiry.Unknown, expiryOf("", NOW))
    }

    @Test
    fun `carga Disponibles, cuenta y calcula la vigencia`() {
        val viewModel = CouponsViewModel(repository, NOW)

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertEquals(1, state.availableCount)
        assertEquals(Expiry.InDays(2), state.current.single().expiry)
        assertEquals(listOf("wallet vigentes"), repository.calls)
    }

    @Test
    fun `Usados se pide al cambiar de pestana y vacio queda sin error`() {
        val viewModel = CouponsViewModel(repository, NOW)

        viewModel.onTab(CouponsTab.USED)

        assertEquals(listOf("wallet vigentes", "wallet usados"), repository.calls)
        assertTrue(viewModel.state.value.current.isEmpty())
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `sin red muestra error con Reintentar y al reintentar carga`() {
        repository.walletResults[CouponsTab.AVAILABLE] = Result.Failure(AppError.Network)
        val viewModel = CouponsViewModel(repository, NOW)
        assertNotNull(viewModel.state.value.error)

        repository.walletResults[CouponsTab.AVAILABLE] = Result.Success(PageResult(listOf(sampleWalletCoupon()), null))
        viewModel.load()

        assertNull(viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.current.size)
    }

    @Test
    fun `un error con datos ya cargados los conserva y avisa`() {
        val viewModel = CouponsViewModel(repository, NOW)
        repository.walletResults[CouponsTab.AVAILABLE] = Result.Failure(AppError.Network)

        viewModel.load()

        assertEquals(1, viewModel.state.value.current.size)
        assertNull(viewModel.state.value.error)
        assertNotNull(viewModel.state.value.message)
    }

    @Test
    fun `el detalle usa el cupon en memoria y pide sus condiciones`() {
        repository.cache["w1"] = sampleWalletCoupon("w1")

        val viewModel = CouponDetailViewModel(SavedStateHandle(mapOf("id" to "w1")), repository, NOW)

        val state = viewModel.state.value
        assertEquals("w1", state.coupon?.id)
        assertEquals("Lunes a jueves.", state.conditions)
        assertTrue(state.canRedeem)
        assertEquals(listOf("conditions c-w1"), repository.calls)
    }

    @Test
    fun `sin el cupon en memoria lo busca en la cartera`() {
        val viewModel = CouponDetailViewModel(SavedStateHandle(mapOf("id" to "w1")), repository, NOW)

        assertEquals("w1", viewModel.state.value.coupon?.id)
        assertEquals("wallet vigentes", repository.calls.first())
    }

    @Test
    fun `un cupon que no esta en la cartera da error`() {
        repository.walletResults[CouponsTab.AVAILABLE] = Result.Success(PageResult(emptyList(), null))

        val viewModel = CouponDetailViewModel(SavedStateHandle(mapOf("id" to "w9")), repository, NOW)

        assertNotNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.coupon)
    }

    @Test
    fun `Mostrar codigo lo pide, cuenta hacia atras y al vencer se oculta`() = runTest(mainDispatcher.dispatcher) {
        repository.cache["w1"] = sampleWalletCoupon("w1")
        val clock = MutableClock(Instant.parse("2026-11-25T18:04:58Z"))
        repository.redeemResult = Result.Success(RedeemCode("K7Q2MX", "2026-11-25T18:05:00Z"))
        val viewModel = CouponDetailViewModel(SavedStateHandle(mapOf("id" to "w1")), repository, clock)

        viewModel.redeem()
        assertEquals("K7Q2MX", viewModel.state.value.code?.code)
        assertEquals(2L, viewModel.state.value.secondsLeft)

        clock.now = Instant.parse("2026-11-25T18:05:00Z")
        advanceTimeBy(1_100)

        assertNull(viewModel.state.value.code)
        assertTrue(repository.calls.contains("redeem w1"))
    }

    @Test
    fun `un 409 al canjear muestra el mensaje de la API`() {
        repository.cache["w1"] = sampleWalletCoupon("w1")
        repository.redeemResult = Result.Failure(AppError.Conflict("el cupón ya venció"))
        val viewModel = CouponDetailViewModel(SavedStateHandle(mapOf("id" to "w1")), repository, NOW)

        viewModel.redeem()

        assertNull(viewModel.state.value.code)
        assertNotNull(viewModel.state.value.message)
        assertFalse(viewModel.state.value.redeeming)
    }

    @Test
    fun `un cupon usado no pide codigo`() {
        repository.cache["w1"] = sampleWalletCoupon("w1", state = USED_STATE)
        val viewModel = CouponDetailViewModel(SavedStateHandle(mapOf("id" to "w1")), repository, NOW)

        viewModel.redeem()

        assertFalse(viewModel.state.value.canRedeem)
        assertFalse(repository.calls.any { it.startsWith("redeem") })
    }
}

/** Reloj que la prueba mueve a mano. */
private class MutableClock(var now: Instant, private val zone: ZoneId = MX) : Clock() {
    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = MutableClock(now, zone)

    override fun instant(): Instant = now
}
