package com.wherever.mobile.feature.vaca.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.vaca.domain.Debt
import com.wherever.mobile.feature.vaca.domain.SplitMode
import com.wherever.mobile.feature.vaca.domain.VacaRepository
import com.wherever.mobile.feature.vaca.domain.VacaSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VacaUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val summary: VacaSummary? = null,
    val myId: String? = null,
    /** Nombre del grupo o de su plan («Viernes de tacos»). */
    val title: String? = null,
    /** Partes iguales o Personalizado: con lo que abre Agregar gasto. */
    val mode: SplitMode = SplitMode.EQUAL,
    val paying: Set<String> = emptySet(),
    val message: UiText? = null
) {
    /** Por persona: lo que le toca a cada quien si es igual; si no, el promedio. */
    val perPersonCents: Long
        get() {
            val people = summary?.people.orEmpty()
            if (people.isEmpty()) return 0
            val shares = people.map { it.shareCents }.distinct()
            return shares.singleOrNull() ?: (summary?.totalCents ?: 0) / people.size
        }

    fun nameOf(userId: String): String = summary?.people?.firstOrNull { it.userId == userId }?.name.orEmpty()

    /** Solo el deudor o el acreedor pueden marcar la deuda como pagada (otro integrante = 403). */
    fun canPay(debt: Debt): Boolean = myId != null && (debt.debtorId == myId || debt.creditorId == myId)
}

/** 28_Vaca y sus variantes (specs/nv-26). Wherever no mueve dinero: solo calcula y registra lo pagado. */
@HiltViewModel
class VacaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vaca: VacaRepository,
    private val groups: GroupsRepository,
    private val plans: PlansRepository,
    private val profiles: ProfileRepository
) : ViewModel() {
    val groupId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(VacaUiState())
    val state: StateFlow<VacaUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val myId = profiles.observeProfile().first()?.id
            mutableState.update { it.copy(myId = myId) }
            loadTitle()
        }
    }

    private suspend fun loadTitle() {
        val group = (groups.group(groupId) as? Result.Success)?.data ?: return
        val title = group.name ?: (plans.get(group.planId) as? Result.Success)?.data?.name
        mutableState.update { it.copy(title = title) }
    }

    /** Se pide al abrir y al volver de Agregar gasto: la API recalcula el corte. */
    fun load() {
        mutableState.update { it.copy(loading = it.summary == null, error = null) }
        viewModelScope.launch {
            when (val result = vaca.summary(groupId)) {
                is Result.Success -> mutableState.update { it.copy(loading = false, summary = result.data) }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    fun onMode(mode: SplitMode) = mutableState.update { it.copy(mode = mode) }

    /** «Marcar pagado»: un toque basta; el segundo se ignora mientras responde la API. */
    fun markPaid(debt: Debt) {
        val current = state.value
        if (debt.id in current.paying || !current.canPay(debt)) return
        mutableState.update { it.copy(paying = it.paying + debt.id) }
        viewModelScope.launch {
            val result = vaca.markPaid(debt.id)
            mutableState.update {
                it.copy(
                    paying = it.paying - debt.id,
                    message = when (result) {
                        is Result.Success -> UiText.Res(R.string.vaca_marked_paid)
                        is Result.Failure ->
                            if (result.error is AppError.Conflict) {
                                UiText.Res(R.string.vaca_already_paid)
                            } else {
                                result.error.toUiText()
                            }
                    }
                )
            }
            load()
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
