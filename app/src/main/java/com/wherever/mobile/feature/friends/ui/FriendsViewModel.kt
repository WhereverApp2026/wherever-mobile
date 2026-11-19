package com.wherever.mobile.feature.friends.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.Friendship
import com.wherever.mobile.feature.friends.domain.FriendshipList
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Normalizer
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FriendsUiState(
    val tab: FriendshipList = FriendshipList.FRIENDS,
    val items: List<Friendship> = emptyList(),
    val pending: Int = 0,
    val query: String = "",
    val loading: Boolean = true,
    val loadError: UiText? = null,
    val error: UiText? = null,
    /** Solicitudes con una acción en curso: sus botones se deshabilitan. */
    val busy: Set<String> = emptySet()
) {
    /** La API filtra por nombre sin acentos; aquí se filtra lo cargado igual, sin otra llamada por letra. */
    val visible: List<Friendship>
        get() {
            val needle = query.normalized()
            if (needle.isEmpty()) return items
            return items.filter {
                it.person.name.normalized().contains(needle) || it.person.username.orEmpty().contains(needle)
            }
        }
}

internal fun String.normalized(): String =
    Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")

/** Amigos: lista, solicitudes recibidas y enviadas (US-233). */
@HiltViewModel
class FriendsViewModel @Inject constructor(private val repository: FriendsRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(FriendsUiState())
    val state: StateFlow<FriendsUiState> = mutableState.asStateFlow()

    private var loadJob: Job? = null

    /** Al volver a la pantalla: una solicitud pudo cambiar en el perfil de la persona. */
    fun load() {
        val tab = state.value.tab
        loadJob?.cancel()
        mutableState.update { it.copy(loading = it.items.isEmpty(), loadError = null) }
        loadJob = viewModelScope.launch {
            when (val result = repository.list(tab)) {
                is Result.Success -> mutableState.update {
                    it.copy(loading = false, items = result.data.items, pending = result.data.pending)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loading = false, loadError = result.error.toUiText())
                }
            }
        }
    }

    fun onTab(tab: FriendshipList) {
        if (tab == state.value.tab) return
        mutableState.update { it.copy(tab = tab, items = emptyList(), query = "", error = null) }
        load()
    }

    fun onQuery(value: String) = mutableState.update { it.copy(query = value) }

    fun accept(friendship: Friendship) = act(friendship) { repository.respond(friendship.id, accept = true) }

    /** Rechazar no avisa a la otra persona (backend nv-36). */
    fun reject(friendship: Friendship) = act(friendship) { repository.respond(friendship.id, accept = false) }

    fun cancel(friendship: Friendship) = act(friendship) { repository.remove(friendship.id) }

    private fun act(friendship: Friendship, call: suspend () -> Result<Unit>) {
        if (friendship.id in state.value.busy) return
        mutableState.update { it.copy(busy = it.busy + friendship.id, error = null) }
        viewModelScope.launch {
            val result = call()
            mutableState.update { state ->
                when (result) {
                    is Result.Success -> state.copy(
                        busy = state.busy - friendship.id,
                        items = state.items.filterNot { it.id == friendship.id },
                        pending = if (friendship.received) (state.pending - 1).coerceAtLeast(0) else state.pending
                    )
                    is Result.Failure -> state.copy(busy = state.busy - friendship.id, error = result.error.toUiText())
                }
            }
        }
    }
}
