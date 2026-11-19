package com.wherever.mobile.feature.friends.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.fieldErrors
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.friends.domain.FriendSuggestion
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.domain.PersonDetail
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val USERNAME = Regex("^[a-z0-9_]{3,20}$")
private val FRIEND_CODE = Regex("^[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{8}$")
private const val MIN_QUERY = 3

/** Regla de la API (backend nv-34): 3 a 20 de `[a-z0-9_]`; `@` y mayúsculas se normalizan. */
fun normalizeUsername(value: String): String = value.trim().removePrefix("@").lowercase()

fun isValidUsername(value: String): Boolean = USERNAME.matches(normalizeUsername(value))

fun isValidFriendCode(value: String): Boolean = FRIEND_CODE.matches(value.trim().uppercase())

data class AddFriendUiState(
    val myUsername: String? = null,
    val myCode: String? = null,
    val query: String = "",
    val searching: Boolean = false,
    /** null = aún no se busca; vacío = sin resultados. */
    val results: List<Person>? = null,
    val code: String = "",
    val codeResult: PersonDetail? = null,
    val codeError: UiText? = null,
    val suggestions: List<FriendSuggestion> = emptyList(),
    /** Personas a las que ya se mandó solicitud en esta pantalla. */
    val sent: Set<String> = emptySet(),
    val sending: Set<String> = emptySet(),
    val editingUsername: Boolean = false,
    val usernameDraft: String = "",
    val usernameError: UiText? = null,
    val savingUsername: Boolean = false,
    val message: UiText? = null,
    val error: UiText? = null
) {
    val canSearch: Boolean
        get() = query.trim().length >= MIN_QUERY && !searching

    val canSaveUsername: Boolean
        get() = isValidUsername(usernameDraft) && !savingUsername
}

/** Agregar amigos: buscar, código, sugerencias y mi nombre de usuario (US-232, US-234, US-235). */
@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val friends: FriendsRepository,
    private val profiles: ProfileRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(AddFriendUiState())
    val state: StateFlow<AddFriendUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            profiles.observeProfile().collect { profile ->
                mutableState.update { it.copy(myUsername = profile?.username, myCode = profile?.friendCode) }
            }
        }
        viewModelScope.launch {
            // El código de amigo llega con el perfil: se refresca por si Room tiene la versión anterior.
            if (profiles.observeProfile().first()?.friendCode == null) profiles.refresh()
        }
        viewModelScope.launch {
            (friends.suggestions() as? Result.Success)?.let { result ->
                mutableState.update { it.copy(suggestions = result.data) }
            }
        }
    }

    fun onQuery(value: String) = mutableState.update { it.copy(query = value, error = null) }

    fun search() {
        val current = state.value
        if (!current.canSearch) return
        mutableState.update { it.copy(searching = true, error = null) }
        viewModelScope.launch {
            val result = friends.search(current.query)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(searching = false, results = result.data)
                    is Result.Failure -> it.copy(searching = false, error = result.error.toUiText())
                }
            }
        }
    }

    fun onCode(value: String) = mutableState.update {
        it.copy(code = value.trim().uppercase().take(FRIEND_CODE_LENGTH), codeError = null, codeResult = null)
    }

    fun findByCode() {
        val code = state.value.code
        if (!isValidFriendCode(code)) {
            mutableState.update { it.copy(codeError = UiText.Res(R.string.friends_code_invalid)) }
            return
        }
        viewModelScope.launch {
            val result = friends.byCode(code)
            mutableState.update {
                when {
                    result is Result.Success -> it.copy(codeResult = result.data)
                    (result as Result.Failure).error is AppError.NotFound ->
                        it.copy(codeError = UiText.Res(R.string.friends_code_not_found))
                    else -> it.copy(codeError = result.error.toUiText())
                }
            }
        }
    }

    fun request(person: Person) {
        val current = state.value
        if (person.id in current.sent || person.id in current.sending) return
        mutableState.update { it.copy(sending = it.sending + person.id, error = null, message = null) }
        viewModelScope.launch {
            val result = friends.request(person.id)
            mutableState.update {
                when {
                    result is Result.Success -> it.copy(
                        sending = it.sending - person.id,
                        sent = it.sent + person.id,
                        message = UiText.Res(
                            if (result.data.accepted) R.string.friends_now_friends else R.string.friends_request_sent,
                            listOf(person.name)
                        )
                    )
                    // 409: ya había solicitud o ya son amigos; para la persona es lo mismo que enviada.
                    (result as Result.Failure).error is AppError.Conflict ->
                        it.copy(sending = it.sending - person.id, sent = it.sent + person.id)
                    else -> it.copy(sending = it.sending - person.id, error = result.error.toUiText())
                }
            }
        }
    }

    fun editUsername() = mutableState.update {
        it.copy(editingUsername = true, usernameDraft = it.myUsername.orEmpty(), usernameError = null)
    }

    fun dismissUsername() = mutableState.update { it.copy(editingUsername = false) }

    fun onUsername(value: String) = mutableState.update {
        it.copy(usernameDraft = normalizeUsername(value), usernameError = null)
    }

    fun saveUsername() {
        val current = state.value
        if (!current.canSaveUsername) {
            mutableState.update { it.copy(usernameError = UiText.Res(R.string.friends_username_rule)) }
            return
        }
        mutableState.update { it.copy(savingUsername = true, usernameError = null) }
        viewModelScope.launch {
            val result = profiles.updateUsername(current.usernameDraft)
            mutableState.update {
                when {
                    result is Result.Success -> it.copy(savingUsername = false, editingUsername = false)
                    (result as Result.Failure).error is AppError.Conflict ->
                        it.copy(savingUsername = false, usernameError = UiText.Res(R.string.friends_username_taken))
                    else -> it.copy(
                        savingUsername = false,
                        usernameError = result.error.fieldErrors()["nombre_usuario"] ?: result.error.toUiText()
                    )
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }

    private companion object {
        const val FRIEND_CODE_LENGTH = 8
    }
}
