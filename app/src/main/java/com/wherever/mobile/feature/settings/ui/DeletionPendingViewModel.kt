package com.wherever.mobile.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeletionPendingUiState(val deleteDate: String = "", val working: Boolean = false, val error: UiText? = null)

enum class DeletionPendingEvent { CANCELLED, LOGGED_OUT }

@HiltViewModel
class DeletionPendingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val clock: Clock
) : ViewModel() {
    private val mutableState = MutableStateFlow(DeletionPendingUiState())
    val state: StateFlow<DeletionPendingUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<DeletionPendingEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val date = profileRepository.observeProfile().firstOrNull()?.deleteAt
            mutableState.update { it.copy(deleteDate = date?.let { iso -> formatDate(iso, clock.zone) }.orEmpty()) }
        }
    }

    fun cancelDeletion() {
        mutableState.update { it.copy(working = true, error = null) }
        viewModelScope.launch {
            when (val result = profileRepository.cancelDeletion()) {
                is Result.Success -> eventChannel.send(DeletionPendingEvent.CANCELLED)
                is Result.Failure -> mutableState.update {
                    it.copy(working = false, error = result.error.toUiText())
                }
            }
        }
    }

    fun logout() {
        mutableState.update { it.copy(working = true, error = null) }
        viewModelScope.launch {
            authRepository.logout()
            eventChannel.send(DeletionPendingEvent.LOGGED_OUT)
        }
    }
}
