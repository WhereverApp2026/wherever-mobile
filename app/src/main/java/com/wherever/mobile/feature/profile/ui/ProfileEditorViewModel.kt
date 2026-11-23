package com.wherever.mobile.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.fieldErrors
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.isValidName
import com.wherever.mobile.feature.friends.ui.isValidUsername
import com.wherever.mobile.feature.friends.ui.normalizeUsername
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Límite de la API para la bio (backend nv-77). */
const val MAX_BIO = 160

data class ProfileEditorUiState(
    val loading: Boolean = true,
    val name: String = "",
    val username: String = "",
    val bio: String = "",
    val photoUrl: String? = null,
    /** Claves de Gustos (las etiquetas salen de 06_Gustos). */
    val tastes: List<String> = emptyList(),
    val original: Triple<String, String, String> = Triple("", "", ""),
    val nameError: UiText? = null,
    val usernameError: UiText? = null,
    val bioError: UiText? = null,
    val saving: Boolean = false,
    val error: UiText? = null
) {
    val changed: Boolean
        get() = Triple(name.trim(), username, bio.trim()) != original

    val canSave: Boolean
        get() = !loading &&
            !saving &&
            isValidName(name) &&
            (username.isEmpty() || isValidUsername(username)) &&
            bio.length <= MAX_BIO
}

/** 82_EditarPerfil (Figma `298:8875`): nombre, usuario y bio; «Listo» guarda solo lo que cambió. */
@HiltViewModel
class ProfileEditorViewModel @Inject constructor(private val repository: ProfileRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(ProfileEditorUiState())
    val state: StateFlow<ProfileEditorUiState> = mutableState.asStateFlow()
    private val doneChannel = Channel<Unit>(Channel.BUFFERED)
    val done: Flow<Unit> = doneChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val profile = repository.observeProfile().filterNotNull().first()
            val bio = repository.observeExtras().first()?.description.orEmpty()
            val username = profile.username.orEmpty()
            mutableState.update {
                it.copy(
                    loading = false,
                    name = profile.name,
                    username = username,
                    bio = bio,
                    photoUrl = profile.photoUrl,
                    tastes = profile.tastes?.categories.orEmpty(),
                    original = Triple(profile.name, username, bio)
                )
            }
        }
        viewModelScope.launch {
            repository.observeProfile().filterNotNull().collect { profile ->
                mutableState.update {
                    it.copy(photoUrl = profile.photoUrl, tastes = profile.tastes?.categories.orEmpty())
                }
            }
        }
    }

    fun onName(value: String) = mutableState.update { it.copy(name = value, nameError = null) }

    fun onUsername(value: String) = mutableState.update {
        it.copy(username = normalizeUsername(value), usernameError = null)
    }

    fun onBio(value: String) = mutableState.update {
        it.copy(bio = value.replace('\n', ' ').take(MAX_BIO), bioError = null)
    }

    fun save() {
        val current = state.value
        if (!current.changed) {
            viewModelScope.launch { doneChannel.send(Unit) }
            return
        }
        if (!current.canSave) {
            mutableState.update {
                it.copy(
                    nameError = if (isValidName(it.name)) null else UiText.Res(R.string.validation_name),
                    usernameError = if (it.username.isEmpty() || isValidUsername(it.username)) {
                        null
                    } else {
                        UiText.Res(R.string.friends_username_rule)
                    }
                )
            }
            return
        }
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val (name, username, bio) = current.original
            val failure = sequence(
                (current.name.trim() != name) to { repository.updateProfile(name = current.name.trim()) },
                (current.username != username) to { repository.updateUsername(current.username) },
                (current.bio.trim() != bio) to { repository.updateBio(current.bio) }
            )
            if (failure == null) {
                mutableState.update { it.copy(saving = false) }
                doneChannel.send(Unit)
            } else {
                mutableState.update { it.copy(saving = false).withError(failure) }
            }
        }
    }

    /** Guarda en orden y se detiene en el primer error (cada `PATCH` usa la versión que dejó el anterior). */
    private suspend fun sequence(vararg steps: Pair<Boolean, suspend () -> Result<*>>): AppError? {
        for ((needed, step) in steps) {
            if (!needed) continue
            val result = step()
            if (result is Result.Failure) return result.error
        }
        return null
    }

    private fun ProfileEditorUiState.withError(error: AppError): ProfileEditorUiState {
        val fields = error.fieldErrors()
        return when {
            fields["nombre_usuario"] != null -> copy(usernameError = fields["nombre_usuario"])
            fields["descripcion"] != null -> copy(bioError = fields["descripcion"])
            fields["nombre"] != null -> copy(nameError = fields["nombre"])
            error is AppError.Conflict && username != original.second ->
                copy(usernameError = UiText.Res(R.string.friends_username_taken))
            else -> copy(error = error.toUiText())
        }
    }
}

/** Acciones de 82_EditarPerfil. */
internal data class ProfileEditorActions(
    val onCancel: () -> Unit = {},
    val onDone: () -> Unit = {},
    val onName: (String) -> Unit = {},
    val onUsername: (String) -> Unit = {},
    val onBio: (String) -> Unit = {},
    val onTastes: () -> Unit = {},
    val onPrivacy: () -> Unit = {}
)
