package com.wherever.mobile.feature.reservations.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.reservations.domain.NewReservation
import com.wherever.mobile.feature.reservations.domain.ReservationsRepository
import com.wherever.mobile.feature.reservations.domain.TimeSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** La API acepta de hoy a 60 días; la app ofrece las próximas dos semanas. */
internal const val RESERVE_DAYS = 14
internal const val MAX_PEOPLE_RESERVE = 20
internal const val NOTE_MAX = 140
private const val DEFAULT_PEOPLE = 2

data class ReserveUiState(
    val placeName: String = "",
    val dates: List<LocalDate> = emptyList(),
    val date: LocalDate? = null,
    val people: Int = DEFAULT_PEOPLE,
    val slots: List<TimeSlot> = emptyList(),
    val loadingSlots: Boolean = true,
    val slotsError: UiText? = null,
    val slot: TimeSlot? = null,
    val note: String = "",
    val submitting: Boolean = false,
    val error: UiText? = null
) {
    val canSubmit: Boolean
        get() = slot != null && !submitting
}

/** 55_Reservar (US-075, US-082): día, personas, turno con cupo, nota y resumen antes de confirmar. */
@HiltViewModel
class ReserveViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ReservationsRepository,
    clock: Clock
) : ViewModel() {
    private val placeId: String = checkNotNull(savedStateHandle["placeId"])
    private val mutableState = MutableStateFlow(
        LocalDate.now(clock).let { today ->
            val dates = (0 until RESERVE_DAYS).map { today.plusDays(it.toLong()) }
            ReserveUiState(placeName = savedStateHandle["placeName"] ?: "", dates = dates, date = today)
        }
    )
    val state: StateFlow<ReserveUiState> = mutableState.asStateFlow()

    private val createdChannel = Channel<String>(Channel.BUFFERED)

    /** Id de la reserva creada: la pantalla abre su confirmación. */
    val created = createdChannel.receiveAsFlow()

    /** Se repite en los reintentos del mismo intento y cambia con el turno (misma llave = misma reserva). */
    private var idempotencyKey: String? = null
    private var slotsJob: Job? = null

    init {
        loadSlots()
    }

    fun onDate(date: LocalDate) {
        if (date == state.value.date) return
        mutableState.update { it.copy(date = date, slot = null, error = null) }
        loadSlots()
    }

    fun onPeople(people: Int) {
        val value = people.coerceIn(1, MAX_PEOPLE_RESERVE)
        if (value == state.value.people) return
        mutableState.update { it.copy(people = value, slot = null, error = null) }
        loadSlots()
    }

    fun onSlot(slot: TimeSlot) {
        if (!slot.available) return
        idempotencyKey = null
        mutableState.update { it.copy(slot = slot, error = null) }
    }

    fun onNote(value: String) = mutableState.update { it.copy(note = value.take(NOTE_MAX)) }

    fun loadSlots() {
        val current = state.value
        val date = current.date ?: return
        slotsJob?.cancel()
        mutableState.update { it.copy(loadingSlots = true, slotsError = null) }
        slotsJob = viewModelScope.launch {
            val result = repository.availability(placeId, date, current.people)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(loadingSlots = false, slots = result.data)
                    is Result.Failure -> it.copy(
                        loadingSlots = false,
                        slots = emptyList(),
                        slotsError = result.error.toUiText()
                    )
                }
            }
        }
    }

    fun submit() {
        val current = state.value
        val slot = current.slot ?: return
        if (current.submitting) return
        val key = idempotencyKey ?: UUID.randomUUID().toString().also { idempotencyKey = it }
        mutableState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            val request = NewReservation(placeId, slot.time, current.people, current.note, key)
            when (val result = repository.create(request)) {
                is Result.Success -> {
                    mutableState.update { it.copy(submitting = false) }
                    createdChannel.send(result.data.id)
                }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    private fun onFailure(error: AppError) {
        when (error) {
            // Sin cupo: otro se llevó el turno. Se recargan los horarios y el siguiente intento lleva otra llave.
            is AppError.Conflict -> {
                idempotencyKey = null
                mutableState.update {
                    it.copy(submitting = false, slot = null, error = UiText.Res(R.string.reserve_slot_taken))
                }
                loadSlots()
            }
            is AppError.Forbidden -> mutableState.update {
                it.copy(submitting = false, error = UiText.Res(R.string.reserve_verify_email))
            }
            else -> mutableState.update { it.copy(submitting = false, error = error.toUiText()) }
        }
    }
}
