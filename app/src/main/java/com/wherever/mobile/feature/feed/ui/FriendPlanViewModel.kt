package com.wherever.mobile.feature.feed.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.feed.domain.FeedRepository
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlansRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FriendPlanUiState(
    val loading: Boolean = true,
    val plan: Plan? = null,
    val error: UiText? = null,
    /** Hoja 14_MeApunto abierta. */
    val confirmJoin: Boolean = false,
    val joining: Boolean = false,
    val joined: Boolean = false,
    val copying: Boolean = false,
    val message: UiText? = null
)

/**
 * 29_PlanAmigo: plan público de un amigo con Me apunto (`POST /planes/{id}/me-apunto`, hoja 14_MeApunto) y
 * Hacer mi versión (`POST /planes/{id}/clonar` para mañana; la fecha se cambia en tu copia, como en populares).
 */
@HiltViewModel
class FriendPlanViewModel @Inject constructor(
    private val plans: PlansRepository,
    private val feed: FeedRepository,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val planId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(FriendPlanUiState())
    val state: StateFlow<FriendPlanUiState> = mutableState.asStateFlow()

    private val copiedChannel = Channel<String>(Channel.BUFFERED)

    /** Id de la copia propia para abrirla. */
    val copied = copiedChannel.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = plans.get(planId)) {
                is Result.Success -> mutableState.update { it.copy(loading = false, plan = result.data) }
                is Result.Failure -> mutableState.update {
                    val error = if (result.error is AppError.NotFound) {
                        UiText.Res(R.string.feed_plan_gone)
                    } else {
                        result.error.toUiText()
                    }
                    it.copy(loading = false, error = error)
                }
            }
        }
    }

    fun askJoin() = mutableState.update { if (it.joined) it else it.copy(confirmJoin = true) }

    fun dismissJoin() = mutableState.update { it.copy(confirmJoin = false) }

    fun join() {
        if (state.value.joining || state.value.joined) return
        mutableState.update { it.copy(joining = true) }
        viewModelScope.launch {
            val result = feed.joinPlan(planId)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(
                        joining = false,
                        joined = true,
                        confirmJoin = false,
                        message = UiText.Res(R.string.feed_joined)
                    )
                    is Result.Failure -> it.copy(
                        joining = false,
                        confirmJoin = false,
                        message = joinError(result.error)
                    )
                }
            }
        }
    }

    fun copy() {
        if (state.value.copying) return
        mutableState.update { it.copy(copying = true) }
        viewModelScope.launch {
            when (val result = plans.clone(planId, LocalDate.now(clock).plusDays(1).toString())) {
                is Result.Success -> {
                    mutableState.update { it.copy(copying = false) }
                    copiedChannel.send(result.data.id)
                }
                is Result.Failure -> mutableState.update { it.copy(copying = false, message = result.error.toUiText()) }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }

    private fun joinError(error: AppError): UiText = when (error) {
        is AppError.Conflict -> UiText.Res(R.string.feed_plan_full)
        is AppError.Forbidden -> UiText.Res(R.string.feed_plan_private)
        is AppError.NotFound -> UiText.Res(R.string.feed_plan_gone)
        else -> error.toUiText()
    }
}
