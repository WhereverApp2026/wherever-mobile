package com.wherever.mobile.feature.coupons.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.coupons.domain.CouponRepository
import com.wherever.mobile.feature.coupons.domain.CouponsTab
import com.wherever.mobile.feature.coupons.domain.Expiry
import com.wherever.mobile.feature.coupons.domain.WalletCoupon
import com.wherever.mobile.feature.coupons.domain.expiryOf
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Un cupón con su vigencia ya calculada con el reloj inyectado. */
data class CouponCard(val coupon: WalletCoupon, val expiry: Expiry)

/** Lo leído de una pestaña: null mientras no se ha pedido. */
data class CouponsPage(val items: List<CouponCard>, val cursor: String?)

data class CouponsUiState(
    val tab: CouponsTab = CouponsTab.AVAILABLE,
    val loading: Boolean = true,
    val pages: Map<CouponsTab, CouponsPage> = emptyMap(),
    val loadingMore: Boolean = false,
    val error: UiText? = null,
    val message: UiText? = null
) {
    val current: List<CouponCard> get() = pages[tab]?.items.orEmpty()
    val cursor: String? get() = pages[tab]?.cursor

    /** "Disponibles (N)": solo se cuenta cuando ya se leyó la pestaña. */
    val availableCount: Int? get() = pages[CouponsTab.AVAILABLE]?.items?.size
}

/** 29_Cupones: Disponibles y Usados de `GET /mis-cupones` (specs/nv-29). */
@HiltViewModel
class CouponsViewModel @Inject constructor(private val repository: CouponRepository, private val clock: Clock) :
    ViewModel() {
    private val mutableState = MutableStateFlow(CouponsUiState())
    val state: StateFlow<CouponsUiState> = mutableState.asStateFlow()
    private var job: Job? = null

    init {
        load()
    }

    fun load() {
        val tab = state.value.tab
        job?.cancel()
        mutableState.update { it.copy(loading = it.pages[tab] == null, error = null) }
        job = viewModelScope.launch {
            when (val page = repository.wallet(tab)) {
                is Result.Success -> mutableState.update {
                    it.copy(
                        loading = false,
                        pages = it.pages + (tab to CouponsPage(page.data.items.toCards(), page.data.nextCursor))
                    )
                }
                is Result.Failure -> mutableState.update { state ->
                    // Si ya había datos de la pestaña se conservan y el error va en un aviso.
                    if (state.pages[tab] != null) {
                        state.copy(loading = false, message = page.error.toUiText())
                    } else {
                        state.copy(loading = false, error = page.error.toUiText())
                    }
                }
            }
        }
    }

    fun onTab(tab: CouponsTab) {
        if (tab == state.value.tab) return
        mutableState.update { it.copy(tab = tab, error = null) }
        load()
    }

    fun loadMore() {
        val tab = state.value.tab
        val cursor = state.value.cursor ?: return
        if (state.value.loadingMore) return
        mutableState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            when (val page = repository.wallet(tab, cursor)) {
                is Result.Success -> mutableState.update {
                    val previous = it.pages[tab]?.items.orEmpty()
                    it.copy(
                        loadingMore = false,
                        pages =
                        it.pages + (tab to CouponsPage(previous + page.data.items.toCards(), page.data.nextCursor))
                    )
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loadingMore = false, message = page.error.toUiText())
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }

    private fun List<WalletCoupon>.toCards() = map { CouponCard(it, expiryOf(it.expiresAt, clock)) }
}
