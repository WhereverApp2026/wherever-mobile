package com.wherever.mobile.feature.vaca.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.vaca.domain.EXPENSE_DESCRIPTION_MAX
import com.wherever.mobile.feature.vaca.domain.EXPENSE_MAX_CENTS
import com.wherever.mobile.feature.vaca.domain.ExpenseConcept
import com.wherever.mobile.feature.vaca.domain.NewExpense
import com.wherever.mobile.feature.vaca.domain.SPLIT_MAX
import com.wherever.mobile.feature.vaca.domain.SPLIT_MIN
import com.wherever.mobile.feature.vaca.domain.SplitMode
import com.wherever.mobile.feature.vaca.domain.VacaRepository
import com.wherever.mobile.feature.vaca.domain.parsePesos
import com.wherever.mobile.feature.vaca.domain.splitEqually
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Integrante confirmado que puede pagar o entrar en la división. */
data class ExpenseMember(val userId: String, val name: String)

data class AddExpenseUiState(
    val loading: Boolean = true,
    val loadError: UiText? = null,
    val title: String? = null,
    val members: List<ExpenseMember> = emptyList(),
    val myId: String? = null,
    val description: String = "",
    val concept: ExpenseConcept = ExpenseConcept.FOOD,
    val amount: String = "",
    val paidBy: String? = null,
    val splitBetween: Set<String> = emptySet(),
    val mode: SplitMode = SplitMode.EQUAL,
    val custom: Map<String, String> = emptyMap(),
    val saving: Boolean = false,
    val error: UiText? = null
) {
    /** Monto en centavos enteros (nunca `Float`); nulo si no es válido. */
    val amountCents: Long? get() = parsePesos(amount)?.takeIf { it in 1..EXPENSE_MAX_CENTS }

    /** Lo que le toca a cada quien en partes iguales («$120 c/u»). */
    val eachCents: Long? get() = amountCents?.takeIf { splitBetween.isNotEmpty() }?.let { it / splitBetween.size }

    val customCents: Map<String, Long>
        get() = splitBetween.associateWith { parsePesos(custom[it].orEmpty()) ?: 0 }

    /** En personalizado, lo que falta (o sobra) para igualar el monto. */
    val customRemaining: Long? get() = amountCents?.let { it - customCents.values.sum() }

    val canSave: Boolean
        get() = !saving &&
            amountCents != null &&
            paidBy != null &&
            splitBetween.size in SPLIT_MIN..SPLIT_MAX &&
            (mode == SplitMode.EQUAL || customRemaining == 0L)
}

/** 45_AgregarGasto (specs/nv-26): partes iguales o personalizado; la suma personalizada iguala el monto. */
@Suppress("TooManyFunctions") // Un evento por control del formulario.
@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vaca: VacaRepository,
    private val groups: GroupsRepository,
    private val profiles: ProfileRepository
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["id"])
    private val clientId: String = UUID.randomUUID().toString()
    private var planId: String? = null
    private val mutableState = MutableStateFlow(
        AddExpenseUiState(
            mode = SplitMode.entries.firstOrNull { it.apiValue == savedStateHandle.get<String>("mode") }
                ?: SplitMode.EQUAL
        )
    )
    val state: StateFlow<AddExpenseUiState> = mutableState.asStateFlow()

    private val savedChannel = Channel<Unit>(Channel.BUFFERED)
    val saved = savedChannel.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            val myId = profiles.observeProfile().first()?.id
            when (val result = groups.group(groupId)) {
                is Result.Success -> {
                    planId = result.data.planId
                    val members = result.data.members.filter { it.status == MemberStatus.CONFIRMED }
                        .map { ExpenseMember(it.userId, it.name) }
                        // «Tú» primero, como en la maqueta.
                        .sortedBy { if (it.userId == myId) 0 else 1 }
                    mutableState.update {
                        it.copy(
                            loading = false,
                            title = result.data.name,
                            members = members,
                            myId = myId,
                            paidBy =
                            members.firstOrNull { m -> m.userId == myId }?.userId ?: members.firstOrNull()?.userId,
                            splitBetween = members.take(SPLIT_MAX).map { m -> m.userId }.toSet()
                        )
                    }
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loading = false, loadError = result.error.toUiText())
                }
            }
        }
    }

    fun onDescription(value: String) = mutableState.update {
        it.copy(description = value.take(EXPENSE_DESCRIPTION_MAX))
    }

    fun onConcept(concept: ExpenseConcept) = mutableState.update { it.copy(concept = concept) }

    fun onAmount(value: String) = mutableState.update {
        it.copy(amount = value.filter { c -> c.isDigit() || c == '.' }, error = null)
    }

    fun onPaidBy(userId: String) = mutableState.update { it.copy(paidBy = userId) }

    fun toggleMember(userId: String) = mutableState.update {
        val next = if (userId in it.splitBetween) it.splitBetween - userId else it.splitBetween + userId
        if (next.size > SPLIT_MAX) it else it.copy(splitBetween = next)
    }

    fun onMode(mode: SplitMode) = mutableState.update { it.copy(mode = mode) }

    fun onCustom(userId: String, value: String) =
        mutableState.update { it.copy(custom = it.custom + (userId to value.filter { c -> c.isDigit() || c == '.' })) }

    /** Reparte el monto en partes iguales sin perder centavos, para empezar a personalizar. */
    fun fillEqually() = mutableState.update { current ->
        val total = current.amountCents ?: return@update current
        val ids = current.members.map { it.userId }.filter { it in current.splitBetween }
        val parts = splitEqually(total, ids.size)
        current.copy(custom = ids.zip(parts).associate { (id, cents) -> id to centsToText(cents) })
    }

    fun save() {
        val current = state.value
        val amount = current.amountCents
        val payer = current.paidBy
        if (!current.canSave || amount == null || payer == null) return
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val expense = NewExpense(
                amountCents = amount,
                concept = current.concept,
                description = current.description,
                paidBy = payer,
                splitBetween = current.members.map { it.userId }.filter { it in current.splitBetween },
                mode = current.mode,
                customCents = current.customCents,
                planId = planId,
                clientId = clientId
            )
            when (val result = vaca.addExpense(groupId, expense)) {
                is Result.Success -> {
                    mutableState.update { it.copy(saving = false) }
                    savedChannel.send(Unit)
                }
                // Lo escrito no se pierde; reintentar manda el mismo `id_cliente` y no duplica el gasto.
                is Result.Failure -> mutableState.update { it.copy(saving = false, error = result.error.toUiText()) }
            }
        }
    }
}

/** 12050 → «120.50»; 12000 → «120». */
internal fun centsToText(cents: Long): String {
    val pesos = cents / CENTS
    val rest = cents % CENTS
    return if (rest == 0L) pesos.toString() else "$pesos.${rest.toString().padStart(2, '0')}"
}

private const val CENTS = 100L
