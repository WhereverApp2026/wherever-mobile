package com.wherever.mobile.feature.plans.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.plans.domain.PlanSummary
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.plans.domain.PlansTab
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyPlansUiState(
    val tab: PlansTab = PlansTab.UPCOMING,
    val loading: Boolean = true,
    val items: List<PlanSummary> = emptyList(),
    val error: UiText? = null
)

/** 24_MisPlanes: próximos, guardados y pasados (US-058). */
@HiltViewModel
class MyPlansViewModel @Inject constructor(private val plans: PlansRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(MyPlansUiState())
    val state: StateFlow<MyPlansUiState> = mutableState.asStateFlow()
    private var job: Job? = null

    fun onTab(tab: PlansTab) {
        mutableState.update { it.copy(tab = tab, items = emptyList()) }
        load()
    }

    fun load() {
        job?.cancel()
        mutableState.update { it.copy(loading = it.items.isEmpty(), error = null) }
        job = viewModelScope.launch {
            when (val result = plans.list(state.value.tab)) {
                is Result.Success -> mutableState.update { it.copy(loading = false, items = result.data) }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }
}
