package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.business.domain.BusinessProfile
import com.wherever.mobile.feature.business.domain.BusinessProfileRepository
import com.wherever.mobile.feature.business.domain.PAUSE_REASON_MAX
import com.wherever.mobile.feature.business.domain.isValidPauseUntil
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Opciones de la hoja de pausa: días desde hoy (nulo = sin fecha de regreso). */
enum class PauseLength(val days: Long?) { WEEK(WEEK_DAYS), TWO_WEEKS(2 * WEEK_DAYS), MONTH(MONTH_DAYS), OPEN(null) }

private const val WEEK_DAYS = 7L
private const val MONTH_DAYS = 30L

/** Salidas de 58_NegPerfil hacia el grafo principal. */
enum class ProfileExit { EXPLORE, LOGGED_OUT, DELETED }

data class BusinessProfileUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val profile: BusinessProfile? = null,
    val working: Boolean = false,
    val message: UiText? = null,
    val pauseOpen: Boolean = false,
    val pauseLength: PauseLength = PauseLength.WEEK,
    val pauseReason: String = "",
    val deleteOpen: Boolean = false,
    val deleteText: String = "",
    val deleteError: UiText? = null
) {
    /** Eliminar solo con el nombre exacto (la API vuelve a revisarlo). */
    val canDelete: Boolean get() = !working && profile != null && deleteText == profile.name
}

/** 58_NegPerfil (specs/nv-38): perfil, recibir reservaciones, pausa, volver a explorar y eliminar. */
@Suppress("TooManyFunctions") // Una acción por control de 58 y de sus dos hojas.
@HiltViewModel
class BusinessProfileViewModel @Inject constructor(
    private val repository: BusinessProfileRepository,
    private val auth: AuthRepository,
    private val clock: Clock
) : ViewModel() {
    private val mutableState = MutableStateFlow(BusinessProfileUiState())
    val state: StateFlow<BusinessProfileUiState> = mutableState.asStateFlow()

    private val exitChannel = Channel<ProfileExit>(Channel.BUFFERED)
    val exits = exitChannel.receiveAsFlow()

    private var businessId: String? = null

    fun start(id: String) {
        businessId = id
        load()
    }

    fun load() {
        val id = businessId ?: return
        mutableState.update { it.copy(loading = it.profile == null, error = null) }
        viewModelScope.launch {
            when (val result = repository.profile(id)) {
                is Result.Success -> mutableState.update { it.copy(loading = false, profile = result.data) }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    /** `acepta_reservas`; 409 = alguien más cambió el negocio: se recarga y se avisa. */
    fun onAcceptsReservations(accepts: Boolean) {
        val profile = state.value.profile ?: return
        if (state.value.working) return
        mutableState.update { it.copy(working = true) }
        viewModelScope.launch {
            when (val result = repository.setAcceptsReservations(profile, accepts)) {
                is Result.Success -> mutableState.update {
                    it.copy(
                        working = false,
                        profile = result.data,
                        message = UiText.Res(
                            if (accepts) R.string.biz_profile_reservations_on else R.string.biz_profile_reservations_off
                        )
                    )
                }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    fun openPause() = mutableState.update {
        it.copy(pauseOpen = true, pauseLength = PauseLength.WEEK, pauseReason = "")
    }

    fun closePause() = mutableState.update { it.copy(pauseOpen = false) }

    fun onPauseLength(length: PauseLength) = mutableState.update { it.copy(pauseLength = length) }

    fun onPauseReason(text: String) = mutableState.update { it.copy(pauseReason = text.take(PAUSE_REASON_MAX)) }

    /** Pausar (hasta ≤ 90 días) o reanudar si ya está pausado. */
    fun togglePause() {
        val id = businessId
        val current = state.value
        val profile = current.profile
        if (id == null || profile == null || current.working) return
        val pausing = !profile.paused
        val today = LocalDate.now(clock)
        val until = current.pauseLength.days?.let { today.plusDays(it) }.takeIf { pausing }
        if (pausing && !isValidPauseUntil(until, today)) return
        mutableState.update { it.copy(working = true) }
        viewModelScope.launch {
            val reason = current.pauseReason.takeIf { pausing }
            when (val result = repository.pause(id, pausing, until, reason)) {
                is Result.Success -> {
                    mutableState.update {
                        it.copy(
                            working = false,
                            pauseOpen = false,
                            message = UiText.Res(
                                if (pausing) R.string.biz_profile_paused else R.string.biz_profile_resumed
                            )
                        )
                    }
                    load()
                }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    fun explore() {
        if (state.value.working) return
        mutableState.update { it.copy(working = true) }
        viewModelScope.launch {
            when (val result = repository.exploreMode()) {
                is Result.Success -> {
                    mutableState.update { it.copy(working = false) }
                    exitChannel.send(ProfileExit.EXPLORE)
                }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    /** La misma salida que Ajustes: cierra la sesión en la API y borra los tokens. */
    fun logout() {
        mutableState.update { it.copy(working = true) }
        viewModelScope.launch {
            auth.logout()
            exitChannel.send(ProfileExit.LOGGED_OUT)
        }
    }

    fun openDelete() = mutableState.update { it.copy(deleteOpen = true, deleteText = "", deleteError = null) }

    fun closeDelete() = mutableState.update { it.copy(deleteOpen = false, deleteText = "", deleteError = null) }

    fun onDeleteText(text: String) = mutableState.update { it.copy(deleteText = text, deleteError = null) }

    /** Con reservas futuras la API responde 409 `reservas_futuras`: se explica y no se borra nada. */
    fun delete() {
        val id = businessId ?: return
        val current = state.value
        if (!current.canDelete) return
        mutableState.update { it.copy(working = true, deleteError = null) }
        viewModelScope.launch {
            when (val result = repository.delete(id, current.deleteText)) {
                is Result.Success -> {
                    // Sin negocio no hay modo negocio: se vuelve a explorar (si falla, Inicio lo corrige al entrar).
                    repository.exploreMode()
                    mutableState.update { it.copy(working = false, deleteOpen = false, deleteText = "") }
                    exitChannel.send(ProfileExit.DELETED)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(
                        working = false,
                        deleteError = if (result.error is AppError.Conflict &&
                            result.error.apiMessage.isNullOrBlank()
                        ) {
                            UiText.Res(R.string.biz_profile_delete_future)
                        } else {
                            result.error.toUiText()
                        }
                    )
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }

    private fun onFailure(error: AppError) {
        mutableState.update { it.copy(working = false, message = error.toUiText()) }
        if (error is AppError.Conflict) load()
    }
}
