package com.wherever.mobile.feature.coupons.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.coupons.domain.CouponRepository
import com.wherever.mobile.feature.coupons.domain.CouponsTab
import com.wherever.mobile.feature.coupons.domain.Expiry
import com.wherever.mobile.feature.coupons.domain.RedeemCode
import com.wherever.mobile.feature.coupons.domain.WalletCoupon
import com.wherever.mobile.feature.coupons.domain.expiryOf
import com.wherever.mobile.feature.coupons.domain.secondsLeft
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TICK_MS = 1_000L

data class CouponDetailUiState(
    val loading: Boolean = true,
    val coupon: WalletCoupon? = null,
    val expiry: Expiry = Expiry.Unknown,
    val conditions: String? = null,
    val code: RedeemCode? = null,
    val secondsLeft: Long = 0,
    val redeeming: Boolean = false,
    val error: UiText? = null,
    val message: UiText? = null
) {
    /** Un cupón usado ya no pide código. */
    val canRedeem: Boolean get() = coupon != null && coupon.state != USED_STATE && expiry != Expiry.Expired
}

/** Estado de la cartera de un cupón ya canjeado. */
const val USED_STATE = "usado"

/** 47_DetCupon y variantes `DetCupon · *` (specs/nv-29). Lee `id` (el de la cartera) de SavedStateHandle. */
@HiltViewModel
class CouponDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CouponRepository,
    private val clock: Clock
) : ViewModel() {
    private val id: String = savedStateHandle["id"] ?: ""
    private val mutableState = MutableStateFlow(CouponDetailUiState())
    val state: StateFlow<CouponDetailUiState> = mutableState.asStateFlow()
    private var countdown: Job? = null

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = it.coupon == null, error = null) }
        viewModelScope.launch {
            val coupon = repository.cached(id) ?: findInWallet()
            if (coupon == null) {
                // findInWallet ya dejó el error (de red o "no encontrado").
                mutableState.update { it.copy(loading = false) }
                return@launch
            }
            mutableState.update {
                it.copy(loading = false, coupon = coupon, expiry = expiryOf(coupon.expiresAt, clock), error = null)
            }
            val conditions = repository.conditions(coupon.couponId)
            if (conditions is Result.Success) mutableState.update { it.copy(conditions = conditions.data) }
        }
    }

    /** Pide el código de canje; mientras vive se muestra con su cuenta regresiva. */
    fun redeem() {
        if (state.value.redeeming || !state.value.canRedeem) return
        mutableState.update { it.copy(redeeming = true) }
        viewModelScope.launch {
            when (val result = repository.redeem(id)) {
                is Result.Success -> {
                    mutableState.update { it.copy(redeeming = false, code = result.data) }
                    startCountdown(result.data)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(redeeming = false, message = result.error.toUiText())
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }

    private fun startCountdown(code: RedeemCode) {
        countdown?.cancel()
        countdown = viewModelScope.launch {
            var left = secondsLeft(code, clock)
            while (left > 0) {
                mutableState.update { it.copy(secondsLeft = left) }
                delay(TICK_MS)
                left = secondsLeft(code, clock)
            }
            // Venció: se oculta y se puede pedir otro.
            mutableState.update { it.copy(code = null, secondsLeft = 0) }
        }
    }

    /** Sin el cupón en memoria (la app se cerró): se busca en las dos pestañas de la cartera. */
    private suspend fun findInWallet(): WalletCoupon? {
        for (tab in CouponsTab.entries) {
            when (val page = repository.wallet(tab)) {
                is Result.Success -> page.data.items.firstOrNull { it.id == id }?.let { return it }
                is Result.Failure -> {
                    mutableState.update { it.copy(error = page.error.toUiText()) }
                    return null
                }
            }
        }
        mutableState.update { it.copy(error = AppError.NotFound().toUiText()) }
        return null
    }
}
