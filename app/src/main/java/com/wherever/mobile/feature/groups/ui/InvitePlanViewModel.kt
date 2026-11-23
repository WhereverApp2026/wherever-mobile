package com.wherever.mobile.feature.groups.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.groups.domain.GroupMembersRepository
import com.wherever.mobile.feature.plans.domain.PlanSummary
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.plans.domain.PlansTab
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvitePlanUiState(
    val personName: String = "",
    val loading: Boolean = true,
    val loadError: UiText? = null,
    val plans: List<PlanSummary> = emptyList(),
    val selected: String? = null,
    val sending: Boolean = false,
    val error: UiText? = null,
    /** Grupo al que quedó invitada la persona. */
    val sentToGroup: String? = null
) {
    val canSend: Boolean
        get() = selected != null && !sending && sentToGroup == null
}

/** InvitarPlan (specs/nv-25): invitar a un amigo a uno de mis próximos planes. */
@HiltViewModel
class InvitePlanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val members: GroupMembersRepository,
    private val plans: PlansRepository
) : ViewModel() {
    private val personId: String = checkNotNull(savedStateHandle["personId"])
    private val mutableState = MutableStateFlow(
        InvitePlanUiState(personName = savedStateHandle.get<String>("personName").orEmpty())
    )
    val state: StateFlow<InvitePlanUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            val result = plans.list(PlansTab.UPCOMING)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(loading = false, plans = result.data)
                    is Result.Failure -> it.copy(loading = false, loadError = result.error.toUiText())
                }
            }
        }
    }

    fun select(planId: String) {
        if (state.value.sentToGroup != null) return
        mutableState.update { it.copy(selected = planId, error = null) }
    }

    fun send() {
        val current = state.value
        val planId = current.selected ?: return
        if (!current.canSend) return
        mutableState.update { it.copy(sending = true, error = null) }
        viewModelScope.launch {
            val result = members.inviteToPlan(planId, listOf(personId))
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(sending = false, sentToGroup = result.data.id)
                    is Result.Failure -> it.copy(sending = false, error = result.error.toUiText())
                }
            }
        }
    }
}

data class InvitePlanActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onSelect: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onCreatePlan: () -> Unit = {},
    val onGroup: (String) -> Unit = {}
)
