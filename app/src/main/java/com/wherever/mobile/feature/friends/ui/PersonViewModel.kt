package com.wherever.mobile.feature.friends.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.PersonDetail
import com.wherever.mobile.feature.friends.domain.Relation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Confirmaciones de 25_PerfilAmigo. */
enum class PersonConfirm { REMOVE, BLOCK }

data class PersonUiState(
    val detail: PersonDetail? = null,
    /** Id de la amistad o solicitud; la API no lo trae en `GET /personas/{id}`. */
    val friendshipId: String? = null,
    val loading: Boolean = true,
    val loadError: UiText? = null,
    val working: Boolean = false,
    val confirm: PersonConfirm? = null,
    val error: UiText? = null,
    /** Bloqueada: la pantalla se cierra. */
    val blocked: Boolean = false,
    /** Hoja 27_AmigoOpc abierta. */
    val options: Boolean = false
)

/** Perfil de otra persona con la acción que toca según la relación (US-232, US-233, US-238). */
@HiltViewModel
class PersonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FriendsRepository
) : ViewModel() {
    private val personId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(PersonUiState())
    val state: StateFlow<PersonUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = it.detail == null, loadError = null) }
        viewModelScope.launch {
            when (val result = repository.person(personId)) {
                is Result.Success -> {
                    val friendshipId = friendshipFor(result.data.relation)
                    mutableState.update { it.copy(loading = false, detail = result.data, friendshipId = friendshipId) }
                }
                is Result.Failure -> mutableState.update {
                    it.copy(
                        loading = false,
                        loadError = if (result.error is AppError.NotFound) {
                            UiText.Res(R.string.friends_person_not_found)
                        } else {
                            result.error.toUiText()
                        }
                    )
                }
            }
        }
    }

    private suspend fun friendshipFor(relation: Relation): String? {
        val list = when (relation) {
            Relation.FRIEND -> FriendshipList.FRIENDS
            Relation.REQUEST_RECEIVED -> FriendshipList.RECEIVED
            Relation.REQUEST_SENT -> FriendshipList.SENT
            Relation.NONE -> return null
        }
        return (repository.list(list) as? Result.Success)?.data?.items?.firstOrNull { it.person.id == personId }?.id
    }

    fun add() = perform { repository.request(personId).map { } }

    fun accept() = withFriendship { repository.respond(it, accept = true) }

    fun reject() = withFriendship { repository.respond(it, accept = false) }

    /** Cancela la solicitud enviada sin preguntar; eliminar a un amigo pide confirmación. */
    fun cancelRequest() = withFriendship { repository.remove(it) }

    fun openOptions() = mutableState.update { it.copy(options = true) }

    fun closeOptions() = mutableState.update { it.copy(options = false) }

    /** Silenciar o quitar el silencio; la hoja sigue abierta con la marca que confirma la API. */
    fun setMuted(muted: Boolean) {
        val id = state.value.friendshipId ?: return
        if (state.value.working) return
        mutableState.update { it.copy(working = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.setMuted(id, muted)) {
                is Result.Success -> mutableState.update { state ->
                    state.copy(working = false, detail = state.detail?.copy(muted = result.data))
                }
                is Result.Failure -> mutableState.update { it.copy(working = false, error = result.error.toUiText()) }
            }
        }
    }

    fun askRemove() = mutableState.update { it.copy(options = false, confirm = PersonConfirm.REMOVE) }

    fun askBlock() = mutableState.update { it.copy(options = false, confirm = PersonConfirm.BLOCK) }

    fun dismissConfirm() = mutableState.update { it.copy(confirm = null) }

    fun confirm() {
        val action = state.value.confirm ?: return
        mutableState.update { it.copy(confirm = null) }
        when (action) {
            PersonConfirm.REMOVE -> withFriendship { repository.remove(it) }
            PersonConfirm.BLOCK -> withFriendship(afterSuccess = { mutableState.update { it.copy(blocked = true) } }) {
                repository.block(it)
            }
        }
    }

    private fun withFriendship(afterSuccess: () -> Unit = { load() }, call: suspend (String) -> Result<Unit>) {
        val id = state.value.friendshipId ?: return
        perform(afterSuccess) { call(id) }
    }

    private fun perform(afterSuccess: () -> Unit = { load() }, call: suspend () -> Result<Unit>) {
        if (state.value.working) return
        mutableState.update { it.copy(working = true, error = null) }
        viewModelScope.launch {
            val result = call()
            mutableState.update { it.copy(working = false, error = (result as? Result.Failure)?.error?.toUiText()) }
            if (result is Result.Success) afterSuccess()
        }
    }
}

/** Eventos de 27_AmigoOpc. */
internal data class FriendOptionsActions(
    val onMute: (Boolean) -> Unit,
    val onRemove: () -> Unit,
    val onBlock: () -> Unit,
    val onDismiss: () -> Unit
)
