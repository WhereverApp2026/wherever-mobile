package com.wherever.mobile.feature.reservations.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.reservations.domain.RefundQuote
import com.wherever.mobile.feature.reservations.domain.Reservation
import com.wherever.mobile.feature.reservations.domain.ReservationsRepository
import com.wherever.mobile.feature.reservations.domain.TimeSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationUiState(
    val reservation: Reservation? = null,
    val placeName: String? = null,
    val loading: Boolean = true,
    val loadError: UiText? = null,
    val working: Boolean = false,
    /** Cuánto se devuelve si cancela ahora; no nulo = diálogo abierto. */
    val cancelQuote: RefundQuote? = null,
    /** Turnos del mismo día para "Cambiar hora"; no nulo = hoja abierta. */
    val changeSlots: List<TimeSlot>? = null,
    val message: UiText? = null,
    val error: UiText? = null
)

/** 56_ReservaConfirmada y detalle (US-076, US-077, US-088). */
@HiltViewModel
class ReservationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ReservationsRepository,
    private val places: PlacesRepository,
    private val clock: Clock
) : ViewModel() {
    private val reservationId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(ReservationUiState())
    val state: StateFlow<ReservationUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = it.reservation == null, loadError = null) }
        viewModelScope.launch {
            when (val result = repository.get(reservationId)) {
                is Result.Success -> {
                    mutableState.update { it.copy(loading = false, reservation = result.data) }
                    if (state.value.placeName == null) {
                        (places.detail(result.data.placeId) as? Result.Success)?.let { place ->
                            mutableState.update { it.copy(placeName = place.data.name) }
                        }
                    }
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loading = false, loadError = result.error.toUiText())
                }
            }
        }
    }

    /** Antes de cancelar se muestra el reembolso exacto según la política (US-077). */
    fun askCancel() = working {
        when (val quote = repository.refundQuote(reservationId)) {
            is Result.Success -> mutableState.update { it.copy(cancelQuote = quote.data) }
            is Result.Failure -> mutableState.update { it.copy(error = quote.error.toUiText()) }
        }
    }

    fun dismissCancel() = mutableState.update { it.copy(cancelQuote = null) }

    fun confirmCancel(reason: String?) {
        mutableState.update { it.copy(cancelQuote = null) }
        working {
            when (val result = repository.cancel(reservationId, reason)) {
                is Result.Success -> mutableState.update {
                    it.copy(reservation = result.data, message = UiText.Res(R.string.reservation_cancelled))
                }
                is Result.Failure -> mutableState.update { it.copy(error = result.error.toUiText()) }
            }
        }
    }

    fun openChange() {
        val reservation = state.value.reservation ?: return
        working {
            val day = Instant.parse(reservation.dateTime).atZone(clock.zone).toLocalDate()
            val today = LocalDate.now(clock)
            when (val result = repository.availability(reservation.placeId, maxOf(day, today), reservation.people)) {
                is Result.Success -> mutableState.update {
                    it.copy(changeSlots = result.data.filter { slot -> slot.time != reservation.dateTime })
                }
                is Result.Failure -> mutableState.update { it.copy(error = result.error.toUiText()) }
            }
        }
    }

    fun dismissChange() = mutableState.update { it.copy(changeSlots = null) }

    fun changeTo(slot: TimeSlot) {
        val reservation = state.value.reservation ?: return
        if (!slot.available) return
        mutableState.update { it.copy(changeSlots = null) }
        working {
            when (val result = repository.changeTime(reservation, slot.time)) {
                is Result.Success -> mutableState.update {
                    it.copy(reservation = result.data, message = UiText.Res(R.string.reservation_changed))
                }
                // Sin cupo o versión vieja: se vuelve a leer la reserva.
                is Result.Failure -> {
                    mutableState.update {
                        it.copy(
                            error = if (result.error is AppError.Conflict) {
                                UiText.Res(R.string.reserve_slot_taken)
                            } else {
                                result.error.toUiText()
                            }
                        )
                    }
                    if (result.error is AppError.Conflict) load()
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }

    private fun working(block: suspend () -> Unit) {
        if (state.value.working) return
        mutableState.update { it.copy(working = true, error = null) }
        viewModelScope.launch {
            block()
            mutableState.update { it.copy(working = false) }
        }
    }
}
