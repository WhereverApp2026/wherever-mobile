package com.wherever.mobile.feature.groups.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.groups.domain.Group
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import com.wherever.mobile.feature.groups.domain.MemberStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyGroupsUiState(
    val loading: Boolean = true,
    val groups: List<Group> = emptyList(),
    val nextCursor: String? = null,
    val loadingMore: Boolean = false,
    /** Error sin nada que mostrar: pantalla de error con Reintentar. */
    val loadError: UiText? = null,
    /** Error con la lista a la vista (aceptar, rechazar o más grupos): aviso arriba. */
    val error: UiText? = null,
    val responding: Set<String> = emptySet(),
    val message: UiText? = null
) {
    val empty: Boolean
        get() = !loading && loadError == null && groups.isEmpty()
}

/** 44_MisGrupos (specs/nv-25): grupos donde estoy, por actividad, con la invitación para aceptar. */
@HiltViewModel
class MyGroupsViewModel @Inject constructor(private val repository: GroupsRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(MyGroupsUiState())
    val state: StateFlow<MyGroupsUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = it.groups.isEmpty(), loadError = null, error = null) }
        viewModelScope.launch {
            val result = repository.groups()
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(
                        loading = false,
                        groups = result.data.items,
                        nextCursor = result.data.nextCursor
                    )
                    is Result.Failure -> if (it.groups.isEmpty()) {
                        it.copy(loading = false, loadError = result.error.toUiText())
                    } else {
                        it.copy(loading = false, error = result.error.toUiText())
                    }
                }
            }
        }
    }

    fun loadMore() {
        val current = state.value
        val cursor = current.nextCursor ?: return
        if (current.loadingMore) return
        mutableState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            val result = repository.groups(cursor)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(
                        loadingMore = false,
                        groups = (it.groups + result.data.items).distinctBy(Group::id),
                        nextCursor = result.data.nextCursor
                    )
                    is Result.Failure -> it.copy(loadingMore = false, error = result.error.toUiText())
                }
            }
        }
    }

    fun respond(group: Group, accept: Boolean) {
        if (group.id in state.value.responding || group.myStatus != MemberStatus.INVITED) return
        mutableState.update { it.copy(responding = it.responding + group.id, error = null, message = null) }
        viewModelScope.launch {
            val result = repository.respond(group.id, accept)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(
                        responding = it.responding - group.id,
                        // Al rechazar, el grupo sale de la lista: ya no es mío.
                        groups = if (accept) {
                            it.groups.map { g -> if (g.id == group.id) result.data else g }
                        } else {
                            it.groups.filterNot { g -> g.id == group.id }
                        },
                        message = UiText.Res(if (accept) R.string.groups_joined else R.string.groups_declined)
                    )
                    is Result.Failure -> it.copy(responding = it.responding - group.id, error = result.error.toUiText())
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
