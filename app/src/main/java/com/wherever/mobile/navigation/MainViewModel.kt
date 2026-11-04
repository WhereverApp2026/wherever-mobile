package com.wherever.mobile.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.SessionEvents
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StartDestination { WELCOME, HOME, DELETION_PENDING }

enum class RootEvent { SESSION_EXPIRED, DELETION_PENDING }

private const val STOP_TIMEOUT_MILLIS = 5_000L

/**
 * Decide el primer destino (SessionGate de specs/000-arquitectura §2.4).
 * Con sesión entra de inmediato con lo que hay en Room y valida detrás: si el servidor rechaza
 * el refresh, [SessionEvents] lleva a Login.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val sessionEvents: SessionEvents
) : ViewModel() {
    private val mutableStart = MutableStateFlow<StartDestination?>(null)
    val start: StateFlow<StartDestination?> = mutableStart.asStateFlow()

    /** `claro`, `oscuro` o `sistema`. */
    val theme: StateFlow<String> = profileRepository.observeProfile()
        .map { it?.preferences?.theme ?: "sistema" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), "sistema")

    private val eventChannel = Channel<RootEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            sessionEvents.sessionExpired.collect {
                authRepository.clearLocalSession()
                eventChannel.send(RootEvent.SESSION_EXPIRED)
            }
        }
        viewModelScope.launch { decideStart() }
    }

    private suspend fun decideStart() {
        if (!authRepository.hasSession()) {
            mutableStart.value = StartDestination.WELCOME
            return
        }
        val cached = profileRepository.observeProfile().firstOrNull()
        mutableStart.value = if (cached?.deleteAt != null) StartDestination.DELETION_PENDING else StartDestination.HOME
        val refreshed = profileRepository.refresh()
        if (cached?.deleteAt == null && (refreshed as? Result.Success)?.data?.deleteAt != null) {
            eventChannel.send(RootEvent.DELETION_PENDING)
        }
    }
}
