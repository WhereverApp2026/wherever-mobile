package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BUSINESS_ZONE
import com.wherever.mobile.feature.business.domain.BusinessCouponsRepository
import com.wherever.mobile.feature.business.domain.CAPACITY_MAX
import com.wherever.mobile.feature.business.domain.CapacityConfig
import com.wherever.mobile.feature.business.domain.HourSlot
import com.wherever.mobile.feature.business.domain.TURN_MAX
import com.wherever.mobile.feature.business.domain.TURN_STEP
import com.wherever.mobile.feature.business.domain.isValidCapacity
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Días que se pueden ver en 55_NegHoras: de hoy a una semana. */
internal const val CAPACITY_DAYS = 7

data class CapacityUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val saved: CapacityConfig? = null,
    val capacity: Int = 0,
    val turnMinutes: Int = 0,
    val days: List<LocalDate> = emptyList(),
    val date: LocalDate? = null,
    val slots: List<HourSlot> = emptyList(),
    val loadingSlots: Boolean = false,
    val saving: Boolean = false,
    val message: UiText? = null
) {
    val dirty: Boolean get() = saved != null && (saved.capacity != capacity || saved.turnMinutes != turnMinutes)

    val canSave: Boolean get() = dirty && !saving && isValidCapacity(capacity, turnMinutes)

    /** Ocupación de un turno: personas reservadas entre el cupo (0 a 1). */
    fun occupancy(slot: HourSlot): Float {
        val total = saved?.capacity ?: return 0f
        if (total <= 0) return 0f
        return ((total - slot.free).coerceIn(0, total)).toFloat() / total
    }
}

/**
 * 55_NegHoras (specs/nv-37). La API tiene un solo `capacidad_por_horario` para todos los turnos (D-37a): se ve la
 * ocupación de cada hora del día elegido y se edita ese cupo y la duración del turno.
 */
@HiltViewModel
class CapacityViewModel @Inject constructor(
    private val repository: BusinessCouponsRepository,
    private val clock: Clock
) : ViewModel() {
    private val today: LocalDate = LocalDate.now(clock.withZone(BUSINESS_ZONE))
    private val mutableState = MutableStateFlow(
        CapacityUiState(days = (0 until CAPACITY_DAYS).map { today.plusDays(it.toLong()) }, date = today)
    )
    val state: StateFlow<CapacityUiState> = mutableState.asStateFlow()

    private var businessId: String? = null
    private var slotsJob: Job? = null

    fun start(id: String) {
        if (businessId == id) return
        businessId = id
        load()
    }

    fun load() {
        val id = businessId ?: return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.capacity(id)) {
                is Result.Success -> {
                    mutableState.update {
                        it.copy(
                            loading = false,
                            saved = result.data,
                            capacity = result.data.capacity,
                            turnMinutes = result.data.turnMinutes
                        )
                    }
                    loadSlots()
                }
                is Result.Failure -> mutableState.update {
                    it.copy(
                        loading = false,
                        error = if (result.error is AppError.NotFound) {
                            UiText.Res(R.string.capacity_not_configured)
                        } else {
                            result.error.toUiText()
                        }
                    )
                }
            }
        }
    }

    fun onDay(date: LocalDate) {
        mutableState.update { it.copy(date = date) }
        loadSlots()
    }

    private fun loadSlots() {
        val id = businessId ?: return
        val date = state.value.date ?: return
        mutableState.update { it.copy(loadingSlots = true) }
        slotsJob?.cancel()
        slotsJob = viewModelScope.launch {
            val slots = (repository.availability(id, date) as? Result.Success)?.data.orEmpty()
            mutableState.update { it.copy(loadingSlots = false, slots = slots) }
        }
    }

    fun onCapacity(value: Int) = mutableState.update { it.copy(capacity = value.coerceIn(1, CAPACITY_MAX)) }

    fun onTurn(minutes: Int) = mutableState.update {
        it.copy(turnMinutes = (minutes / TURN_STEP * TURN_STEP).coerceIn(TURN_STEP, TURN_MAX))
    }

    /** `PUT reservas-config` con `If-Match`. 409 = otra versión (recarga); 422 trae el motivo de la API. */
    fun save() {
        val id = businessId ?: return
        val current = state.value
        val saved = current.saved ?: return
        if (!current.canSave) return
        mutableState.update { it.copy(saving = true) }
        viewModelScope.launch {
            when (
                val result = repository.saveCapacity(
                    id,
                    saved.copy(capacity = current.capacity, turnMinutes = current.turnMinutes)
                )
            ) {
                is Result.Success -> {
                    mutableState.update {
                        it.copy(saving = false, saved = result.data, message = UiText.Res(R.string.capacity_saved))
                    }
                    loadSlots()
                }
                is Result.Failure -> {
                    val conflict = result.error is AppError.Conflict
                    mutableState.update {
                        it.copy(
                            saving = false,
                            message = if (conflict) UiText.Res(R.string.capacity_changed) else result.error.toUiText()
                        )
                    }
                    if (conflict) load()
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
