package com.wherever.mobile.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.formatDateTime
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionItem(val id: String, val device: String, val lastUsed: String, val isCurrent: Boolean)

data class SessionsUiState(
    val loading: Boolean = true,
    val sessions: List<SessionItem> = emptyList(),
    val closingId: String? = null,
    val closingAll: Boolean = false,
    val error: UiText? = null,
    val loadFailed: Boolean = false
)

@HiltViewModel
class SessionsViewModel @Inject constructor(private val repository: AuthRepository, private val clock: Clock) :
    ViewModel() {
    private val mutableState = MutableStateFlow(SessionsUiState())
    val state: StateFlow<SessionsUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<Unit>(Channel.BUFFERED)
    val allClosed = eventChannel.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, error = null, loadFailed = false) }
        viewModelScope.launch {
            when (val result = repository.sessions()) {
                is Result.Success -> mutableState.update { state ->
                    state.copy(
                        loading = false,
                        sessions = result.data.map {
                            SessionItem(
                                id = it.id,
                                device = "${it.model} · ${it.system}",
                                lastUsed = formatDateTime(it.lastUsed, clock.zone),
                                isCurrent = it.isCurrent
                            )
                        }
                    )
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loading = false, loadFailed = true, error = result.error.toUiText())
                }
            }
        }
    }

    fun close(id: String) {
        mutableState.update { it.copy(closingId = id, error = null) }
        viewModelScope.launch {
            when (val result = repository.closeSession(id)) {
                is Result.Success -> mutableState.update { state ->
                    state.copy(closingId = null, sessions = state.sessions.filterNot { it.id == id })
                }
                is Result.Failure -> mutableState.update {
                    it.copy(closingId = null, error = result.error.toUiText())
                }
            }
        }
    }

    fun closeAll() {
        mutableState.update { it.copy(closingAll = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.logoutAll()) {
                is Result.Success -> eventChannel.send(Unit)
                is Result.Failure -> mutableState.update {
                    it.copy(closingAll = false, error = result.error.toUiText())
                }
            }
        }
    }
}
