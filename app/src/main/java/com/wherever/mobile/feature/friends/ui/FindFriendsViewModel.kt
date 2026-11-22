package com.wherever.mobile.feature.friends.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.friends.domain.ContactMatch
import com.wherever.mobile.feature.friends.domain.ContactMatchRepository
import com.wherever.mobile.feature.friends.domain.ContactsReader
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.Relation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FindFriendsUiState(
    val searching: Boolean = false,
    /** null hasta buscar; vacía = ninguno de tus contactos usa Wherever. */
    val matches: List<ContactMatch>? = null,
    val sending: Set<String> = emptySet(),
    val permissionDenied: Boolean = false,
    val error: UiText? = null
)

data class FindFriendsActions(
    val onSearch: () -> Unit,
    val onRequest: (ContactMatch) -> Unit,
    val onPerson: (String) -> Unit,
    val onDone: () -> Unit
)

/** 07_EncuentraAmigos y su variante «solicitud enviada» (US-233). */
@HiltViewModel
class FindFriendsViewModel @Inject constructor(
    private val contacts: ContactsReader,
    private val matcher: ContactMatchRepository,
    private val friends: FriendsRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(FindFriendsUiState())
    val state: StateFlow<FindFriendsUiState> = mutableState.asStateFlow()

    /** Resultado del permiso pedido en contexto al tocar «Buscar en mis contactos». */
    fun onPermission(granted: Boolean) {
        if (granted) search() else mutableState.update { it.copy(permissionDenied = true) }
    }

    fun search() {
        if (state.value.searching) return
        mutableState.update { it.copy(searching = true, permissionDenied = false, error = null) }
        viewModelScope.launch {
            val phones = contacts.phoneNumbers()
            if (phones.isEmpty()) {
                mutableState.update { it.copy(searching = false, matches = emptyList()) }
                return@launch
            }
            when (val result = matcher.match(phones)) {
                is Result.Success -> mutableState.update { it.copy(searching = false, matches = result.data) }
                is Result.Failure -> mutableState.update {
                    it.copy(searching = false, error = result.error.toFindError())
                }
            }
        }
    }

    fun request(match: ContactMatch) {
        val id = match.person.id
        if (id in state.value.sending || match.relation != Relation.NONE) return
        mutableState.update { it.copy(sending = it.sending + id, error = null) }
        viewModelScope.launch {
            val result = friends.request(id)
            val relation = when (result) {
                is Result.Success -> if (result.data.accepted) Relation.FRIEND else Relation.REQUEST_SENT
                // 409: ya había solicitud o ya son amigos; se muestra como pendiente.
                is Result.Failure -> if (result.error is AppError.Conflict) Relation.REQUEST_SENT else null
            }
            mutableState.update { s ->
                s.copy(
                    sending = s.sending - id,
                    matches = s.matches?.map { m ->
                        if (m.person.id == id && relation != null) m.copy(relation = relation) else m
                    },
                    error = (result as? Result.Failure)?.takeIf { relation == null }?.error?.toUiText()
                )
            }
        }
    }

    /** 429: la API deja buscar contactos 10 veces al día. */
    private fun AppError.toFindError(): UiText =
        if (this is AppError.RateLimited) UiText.Res(R.string.find_friends_rate_limited) else toUiText()
}
