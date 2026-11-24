package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.Balance
import com.wherever.mobile.feature.business.domain.BusinessProfileRepository
import com.wherever.mobile.feature.business.domain.DepositAccount
import com.wherever.mobile.feature.business.domain.Settlement
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentsUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val balance: Balance? = null,
    val account: DepositAccount? = null,
    val connected: Boolean = false,
    val settlements: List<Settlement> = emptyList(),
    val nextCursor: String? = null,
    val loadingMore: Boolean = false,
    val connecting: Boolean = false,
    val message: UiText? = null
)

/** 57_NegCobros (specs/nv-38): saldo, depósitos semanales paginados y conectar cobros con Stripe. */
@HiltViewModel
class PaymentsViewModel @Inject constructor(private val repository: BusinessProfileRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(PaymentsUiState())
    val state: StateFlow<PaymentsUiState> = mutableState.asStateFlow()

    private val urlChannel = Channel<String>(Channel.BUFFERED)

    /** URL del registro de Stripe para abrirla fuera de la app. */
    val connectUrl = urlChannel.receiveAsFlow()

    private var businessId: String? = null

    fun start(id: String) {
        businessId = id
        load()
    }

    fun load() {
        val id = businessId ?: return
        mutableState.update { it.copy(loading = it.balance == null, error = null) }
        viewModelScope.launch {
            val profile = async { repository.profile(id) }
            val settlements = async { repository.settlements(id, cursor = null) }
            when (val balance = repository.balance(id)) {
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = balance.error.toUiText()) }
                is Result.Success -> {
                    val profileData = (profile.await() as? Result.Success)?.data
                    val page = (settlements.await() as? Result.Success)?.data
                    mutableState.update {
                        it.copy(
                            loading = false,
                            balance = balance.data,
                            account = profileData?.depositAccount,
                            connected = profileData?.paymentsConnected == true,
                            settlements = page?.items.orEmpty(),
                            nextCursor = page?.nextCursor
                        )
                    }
                }
            }
        }
    }

    fun loadMore() {
        val id = businessId ?: return
        val cursor = state.value.nextCursor ?: return
        if (state.value.loadingMore) return
        mutableState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            when (val page = repository.settlements(id, cursor)) {
                is Result.Success -> mutableState.update {
                    it.copy(
                        loadingMore = false,
                        settlements = it.settlements + page.data.items,
                        nextCursor = page.data.nextCursor
                    )
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loadingMore = false, message = page.error.toUiText())
                }
            }
        }
    }

    /** Sin llave de Stripe la API responde 503: se explica que los cobros aún no están disponibles. */
    fun connect() {
        val id = businessId ?: return
        if (state.value.connecting) return
        mutableState.update { it.copy(connecting = true) }
        viewModelScope.launch {
            when (val result = repository.connectPayments(id)) {
                is Result.Success -> {
                    mutableState.update { it.copy(connecting = false) }
                    if (result.data.startsWith("https://")) {
                        urlChannel.send(result.data)
                    } else {
                        mutableState.update { it.copy(message = UiText.Res(R.string.payments_unavailable)) }
                    }
                }
                is Result.Failure -> mutableState.update {
                    it.copy(
                        connecting = false,
                        message = if (result.error is AppError.Server) {
                            UiText.Res(R.string.payments_unavailable)
                        } else {
                            result.error.toUiText()
                        }
                    )
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
