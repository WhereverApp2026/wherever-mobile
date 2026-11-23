package com.wherever.mobile.feature.groups.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.groups.domain.Group
import com.wherever.mobile.feature.groups.domain.GroupMember
import com.wherever.mobile.feature.groups.domain.GroupMembersRepository
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.groups.domain.Vote
import com.wherever.mobile.feature.groups.domain.VotesRepository
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlansRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Límite de la API para el nombre del grupo (backend nv-69). */
const val MAX_GROUP_NAME = 40

data class GroupDetailUiState(
    val loading: Boolean = true,
    val loadError: UiText? = null,
    val group: Group? = null,
    val plan: Plan? = null,
    val votes: List<Vote> = emptyList(),
    val busy: Boolean = false,
    val error: UiText? = null,
    val message: UiText? = null,
    /** Nombre en edición (solo el organizador); `null` = sin editar. */
    val nameDraft: String? = null,
    val confirmLeave: Boolean = false,
    val confirmRemove: GroupMember? = null,
    /** Salí del grupo o ya no es mío: la pantalla se cierra. */
    val gone: Boolean = false
) {
    val openVotes: Int
        get() = votes.count(Vote::open)

    /** Quienes no declinaron, el organizador primero. */
    val members: List<GroupMember>
        get() = group?.members.orEmpty()
            .filter { it.status != MemberStatus.DECLINED }
            .sortedWith(
                compareByDescending<GroupMember> {
                    it.organizer
                }.thenBy { it.status != MemberStatus.CONFIRMED }
            )

    val canSaveName: Boolean
        get() = nameDraft?.trim()?.let { it.isNotEmpty() && it.length <= MAX_GROUP_NAME } == true && !busy
}

/** 46_Grupo (specs/nv-25): integrantes, plan, votaciones y La vaca; el organizador invita, renombra y saca. */
@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groups: GroupsRepository,
    private val members: GroupMembersRepository,
    private val votesRepository: VotesRepository,
    private val plans: PlansRepository
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(GroupDetailUiState())
    val state: StateFlow<GroupDetailUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() = reload(clearError = true)

    private fun reload(clearError: Boolean) {
        mutableState.update {
            it.copy(loading = it.group == null, loadError = null, error = if (clearError) null else it.error)
        }
        viewModelScope.launch {
            when (val result = groups.group(groupId)) {
                is Result.Success -> {
                    val group = result.data
                    val plan = async { plans.get(group.planId) }
                    val votes = async { votesRepository.votes(groupId) }
                    mutableState.update {
                        it.copy(
                            loading = false,
                            group = group,
                            plan = (plan.await() as? Result.Success)?.data ?: it.plan,
                            votes = (votes.await() as? Result.Success)?.data ?: it.votes
                        )
                    }
                }
                is Result.Failure -> mutableState.update {
                    when {
                        result.error is AppError.NotFound -> it.copy(loading = false, gone = true)
                        it.group == null -> it.copy(loading = false, loadError = result.error.toUiText())
                        else -> it.copy(loading = false, error = result.error.toUiText())
                    }
                }
            }
        }
    }

    fun startRename() {
        val group = state.value.group ?: return
        if (!group.iAmOrganizer) return
        mutableState.update { it.copy(nameDraft = group.name.orEmpty()) }
    }

    fun onName(value: String) = mutableState.update { it.copy(nameDraft = value.take(MAX_GROUP_NAME)) }

    fun cancelRename() = mutableState.update { it.copy(nameDraft = null) }

    fun saveName() {
        val current = state.value
        val group = current.group
        val name = current.nameDraft?.trim()
        if (group == null || name == null || !current.canSaveName) return
        mutableState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val result = members.rename(group, name)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(busy = false, group = result.data, nameDraft = null)
                    is Result.Failure -> if (result.error is AppError.Conflict) {
                        // Otra persona cambió el grupo: se recarga la versión y se pide repetir.
                        it.copy(busy = false, error = UiText.Res(R.string.group_detail_conflict))
                    } else {
                        it.copy(busy = false, error = result.error.toUiText())
                    }
                }
            }
            if (result is Result.Failure && result.error is AppError.Conflict) reload(clearError = false)
        }
    }

    fun askLeave() = mutableState.update { it.copy(confirmLeave = true) }

    fun askRemove(member: GroupMember) = mutableState.update { it.copy(confirmRemove = member) }

    fun dismissConfirm() = mutableState.update { it.copy(confirmLeave = false, confirmRemove = null) }

    fun leave() {
        mutableState.update { it.copy(confirmLeave = false, busy = true, error = null) }
        viewModelScope.launch {
            when (val result = members.leave(groupId)) {
                is Result.Success -> mutableState.update { it.copy(busy = false, gone = true) }
                is Result.Failure -> mutableState.update { it.copy(busy = false, error = result.error.toUiText()) }
            }
        }
    }

    fun remove() {
        val member = state.value.confirmRemove ?: return
        mutableState.update { it.copy(confirmRemove = null, busy = true, error = null) }
        viewModelScope.launch {
            when (val result = members.removeMember(groupId, member.userId)) {
                is Result.Success -> mutableState.update {
                    it.copy(
                        busy = false,
                        group = it.group?.copy(members = it.group.members.filterNot { m -> m.userId == member.userId }),
                        message = UiText.Res(R.string.group_detail_removed)
                    )
                }
                is Result.Failure -> mutableState.update { it.copy(busy = false, error = result.error.toUiText()) }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
