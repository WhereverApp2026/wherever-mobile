package com.wherever.mobile.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val DELETE_CONFIRMATION = "ELIMINAR"

data class DeleteAccountUiState(
    val confirmation: String = "",
    val deleting: Boolean = false,
    val error: UiText? = null
) {
    val canDelete: Boolean
        get() = confirmation == DELETE_CONFIRMATION && !deleting
}

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(DeleteAccountUiState())
    val state: StateFlow<DeleteAccountUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<Unit>(Channel.BUFFERED)
    val deleted = eventChannel.receiveAsFlow()

    fun onConfirmationChange(value: String) = mutableState.update { it.copy(confirmation = value, error = null) }

    fun delete() {
        val current = state.value
        if (!current.canDelete) return
        mutableState.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            when (val result = profileRepository.deleteAccount(current.confirmation)) {
                is Result.Success -> {
                    // El servidor cerró todas las sesiones.
                    authRepository.clearLocalSession()
                    eventChannel.send(Unit)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(deleting = false, error = result.error.toUiText())
                }
            }
        }
    }
}
