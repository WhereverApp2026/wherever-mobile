package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BusinessCouponsRepository
import com.wherever.mobile.feature.business.domain.Coupon
import com.wherever.mobile.feature.business.domain.CouponGroup
import com.wherever.mobile.feature.business.domain.CouponStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CouponsUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val coupons: List<Coupon> = emptyList(),
    val tab: CouponGroup = CouponGroup.ACTIVE,
    val toggling: Set<String> = emptySet(),
    val message: UiText? = null
) {
    val visible: List<Coupon> get() = coupons.filter { it.status.group == tab }

    fun count(group: CouponGroup): Int = coupons.count { it.status.group == group }
}

/** 53_NegCupones (specs/nv-37): Activos, Programados y Terminados con interruptor activo/pausado. */
@HiltViewModel
class CouponsViewModel @Inject constructor(private val repository: BusinessCouponsRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(CouponsUiState())
    val state: StateFlow<CouponsUiState> = mutableState.asStateFlow()

    private var businessId: String? = null

    /** Se pide al abrir y al volver de 54_NegCrear. */
    fun start(id: String) {
        businessId = id
        load()
    }

    fun load() {
        val id = businessId ?: return
        mutableState.update { it.copy(loading = it.coupons.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = repository.coupons(id)) {
                is Result.Success -> mutableState.update { it.copy(loading = false, coupons = result.data) }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    fun selectTab(tab: CouponGroup) = mutableState.update { it.copy(tab = tab) }

    /** Interruptor: activo ↔ pausado con `If-Match`. 409 (otra versión) recarga y avisa. */
    fun toggle(coupon: Coupon) {
        val id = businessId ?: return
        if (coupon.id in state.value.toggling || coupon.status.group != CouponGroup.ACTIVE) return
        val activate = coupon.status == CouponStatus.PAUSED
        mutableState.update { it.copy(toggling = it.toggling + coupon.id) }
        viewModelScope.launch {
            when (val result = repository.setActive(id, coupon, activate)) {
                is Result.Success -> mutableState.update { current ->
                    current.copy(
                        toggling = current.toggling - coupon.id,
                        coupons = current.coupons.map { if (it.id == coupon.id) result.data else it }
                    )
                }
                is Result.Failure -> {
                    val conflict = result.error is AppError.Conflict
                    mutableState.update {
                        it.copy(
                            toggling = it.toggling - coupon.id,
                            message = if (conflict) UiText.Res(R.string.coupons_changed) else result.error.toUiText()
                        )
                    }
                    if (conflict) load()
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
