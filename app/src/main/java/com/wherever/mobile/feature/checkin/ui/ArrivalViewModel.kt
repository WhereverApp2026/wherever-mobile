package com.wherever.mobile.feature.checkin.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.LocationProvider
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.checkin.domain.Checkin
import com.wherever.mobile.feature.checkin.domain.CheckinRepository
import com.wherever.mobile.feature.checkin.domain.isValidCheckinCode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lo que muestra la llegada: comprobando, hace falta permiso o código, o 26_CheckinOk. */
enum class ArrivalPhase { CHECKING, NEEDS_PERMISSION, NOT_YET, DONE }

data class ArrivalUiState(
    val phase: ArrivalPhase = ArrivalPhase.CHECKING,
    val placeName: String = "",
    val nextName: String? = null,
    val nextMinutes: Int? = null,
    val checkin: Checkin? = null,
    /** Mensaje de la API («Parece que aún no llegas») o del error. */
    val message: UiText? = null,
    val alreadyToday: Boolean = false
)

/**
 * Check-in de una parada (specs/nv-30): por ubicación aproximada (solo en primer plano) o escaneando el
 * código del mostrador. La ubicación no se guarda ni se registra.
 */
@HiltViewModel
class ArrivalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val checkins: CheckinRepository,
    private val location: LocationProvider
) : ViewModel() {
    private val placeId: String = checkNotNull(savedStateHandle["placeId"])
    private val planId: String? = savedStateHandle.get<String>("planId")?.takeIf { it.isNotBlank() }
    private val mutableState = MutableStateFlow(
        ArrivalUiState(
            placeName = savedStateHandle.get<String>("placeName").orEmpty(),
            nextName = savedStateHandle.get<String>("nextName")?.takeIf { it.isNotBlank() },
            nextMinutes = savedStateHandle.get<Int>("nextMinutes")?.takeIf { it > 0 }
        )
    )
    val state: StateFlow<ArrivalUiState> = mutableState.asStateFlow()

    /** Sin permiso no se pide la ubicación: se ofrece el permiso o el código del mostrador. */
    fun start() {
        if (!location.hasPermission()) {
            mutableState.update { it.copy(phase = ArrivalPhase.NEEDS_PERMISSION) }
            return
        }
        mutableState.update { it.copy(phase = ArrivalPhase.CHECKING, message = null) }
        viewModelScope.launch {
            val point = location.current()
            if (point == null) {
                mutableState.update {
                    it.copy(phase = ArrivalPhase.NOT_YET, message = UiText.Res(R.string.arrival_no_location))
                }
                return@launch
            }
            finish(checkins.checkIn(placeId, planId, point, code = null))
        }
    }

    /** Código del mostrador leído con la cámara; lo que no parece un código no llega a la API. */
    fun onScanned(contents: String?) {
        val code = contents?.trim().orEmpty()
        if (code.isEmpty()) return
        if (!isValidCheckinCode(code)) {
            mutableState.update {
                it.copy(phase = ArrivalPhase.NOT_YET, message = UiText.Res(R.string.arrival_bad_code))
            }
            return
        }
        mutableState.update { it.copy(phase = ArrivalPhase.CHECKING, message = null) }
        viewModelScope.launch { finish(checkins.checkIn(placeId, planId, point = null, code = code)) }
    }

    private fun finish(result: Result<Checkin>) = mutableState.update {
        when (result) {
            is Result.Success -> it.copy(phase = ArrivalPhase.DONE, checkin = result.data)
            is Result.Failure -> when (val error = result.error) {
                // 409: ya hubo un check-in válido hoy aquí; la llegada cuenta.
                is AppError.Conflict -> it.copy(phase = ArrivalPhase.DONE, alreadyToday = true)
                is AppError.Validation ->
                    it.copy(phase = ArrivalPhase.NOT_YET, message = error.toUiText())
                else -> it.copy(phase = ArrivalPhase.NOT_YET, message = error.toUiText())
            }
        }
    }
}
