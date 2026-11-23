package com.wherever.mobile.feature.groups.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.groups.domain.GroupMembersRepository
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.plans.domain.PlansRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Un grupo tiene hasta 12 lugares: el organizador y 11 amigos (backend nv-37). */
const val MAX_INVITEES = 11

data class CreateGroupUiState(
    val loading: Boolean = true,
    val loadError: UiText? = null,
    /** `true` = invitar a un grupo que ya existe; `false` = crear el grupo del plan. */
    val inviting: Boolean = false,
    val planName: String? = null,
    val name: String = "",
    val query: String = "",
    val friends: List<Person> = emptyList(),
    /** Quienes ya están en el grupo (al invitar): no se pueden volver a elegir. */
    val alreadyIn: Set<String> = emptySet(),
    val selected: Set<String> = emptySet(),
    val saving: Boolean = false,
    val error: UiText? = null
) {
    /** Lugares que quedan para invitar. */
    val seatsLeft: Int
        get() = (MAX_INVITEES - alreadyIn.size).coerceAtLeast(0)

    val visibleFriends: List<Person>
        get() {
            val q = query.trim()
            return friends.filter {
                q.isEmpty() ||
                    it.name.contains(q, ignoreCase = true) ||
                    it.username?.contains(q, ignoreCase = true) == true
            }
        }

    val canSubmit: Boolean
        get() = !saving &&
            !loading &&
            selected.isNotEmpty() &&
            selected.size <= seatsLeft &&
            name.trim().length <= MAX_GROUP_NAME

    fun canPick(id: String): Boolean = id !in alreadyIn && (id in selected || selected.size < seatsLeft)
}

sealed interface CreateGroupEvent {
    data class Done(val groupId: String) : CreateGroupEvent
}

/** 43_CrearGrupo (specs/nv-25): el grupo nace de un plan; también invita a más amigos a un grupo existente. */
@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groups: GroupsRepository,
    private val members: GroupMembersRepository,
    private val friendsRepository: FriendsRepository,
    private val plans: PlansRepository
) : ViewModel() {
    private val planId: String = checkNotNull(savedStateHandle["planId"])
    private val groupId: String = savedStateHandle.get<String>("groupId").orEmpty()
    private val mutableState = MutableStateFlow(CreateGroupUiState(inviting = groupId.isNotEmpty()))
    val state: StateFlow<CreateGroupUiState> = mutableState.asStateFlow()
    private val eventChannel = Channel<CreateGroupEvent>(Channel.BUFFERED)
    val events: Flow<CreateGroupEvent> = eventChannel.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            val plan = async { plans.get(planId) }
            val group = async { if (groupId.isEmpty()) null else groups.group(groupId) }
            val friends = friendsRepository.list(FriendshipList.FRIENDS)
            val members = (group.await() as? Result.Success)?.data?.members.orEmpty()
                .filter { it.status != MemberStatus.DECLINED }
                .map { it.userId }
                .toSet()
            mutableState.update {
                when (friends) {
                    is Result.Success -> it.copy(
                        loading = false,
                        planName = (plan.await() as? Result.Success)?.data?.name,
                        friends = friends.data.items.filter { f -> f.accepted }.map { f -> f.person }
                            .sortedBy { p -> p.name.lowercase() },
                        alreadyIn = members
                    )
                    is Result.Failure -> it.copy(loading = false, loadError = friends.error.toUiText())
                }
            }
        }
    }

    fun onName(value: String) = mutableState.update { it.copy(name = value.take(MAX_GROUP_NAME)) }

    fun onQuery(value: String) = mutableState.update { it.copy(query = value) }

    fun toggle(person: Person) {
        val current = state.value
        if (!current.canPick(person.id)) return
        mutableState.update {
            it.copy(selected = if (person.id in it.selected) it.selected - person.id else it.selected + person.id)
        }
    }

    fun submit() {
        val current = state.value
        if (!current.canSubmit) return
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val ids = current.selected.toList()
            val result = if (groupId.isNotEmpty()) members.invite(groupId, ids) else members.create(planId, ids)
            when (result) {
                is Result.Success -> {
                    val name = current.name.trim()
                    // El nombre es opcional: si falla, el grupo ya existe y se puede renombrar en 46_Grupo.
                    if (groupId.isEmpty() && name.isNotEmpty()) members.rename(result.data, name)
                    mutableState.update { it.copy(saving = false) }
                    eventChannel.send(CreateGroupEvent.Done(result.data.id))
                }
                is Result.Failure -> mutableState.update { it.copy(saving = false, error = result.error.toUiText()) }
            }
        }
    }
}

data class CreateGroupActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onName: (String) -> Unit = {},
    val onQuery: (String) -> Unit = {},
    val onToggle: (Person) -> Unit = {},
    val onSubmit: () -> Unit = {},
    val onAddFriends: () -> Unit = {}
)
