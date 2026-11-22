package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BUSINESS_ZONE
import com.wherever.mobile.feature.business.domain.BlockReason
import com.wherever.mobile.feature.business.domain.BusinessHomeRepository
import com.wherever.mobile.feature.business.domain.CONTACT_MAX
import com.wherever.mobile.feature.business.domain.Reservation
import com.wherever.mobile.feature.business.domain.ReservationStatus
import com.wherever.mobile.feature.business.domain.ScheduleBlock
import com.wherever.mobile.feature.business.domain.isValidContactText
import com.wherever.mobile.feature.business.domain.isValidTime
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pestañas de 49_NegReservas. Próximas y Pasadas recorren los días desde mañana o desde ayer. */
enum class ReservationsTab { TODAY, UPCOMING, PAST }

/** Hoja para cerrar horarios del día que se ve (`POST bloqueos`). */
data class BlockForm(val from: String = "", val to: String = "", val reason: BlockReason? = null) {
    val valid: Boolean get() = isValidTime(from) && isValidTime(to) && from < to
}

data class BusinessReservationsUiState(
    val tab: ReservationsTab = ReservationsTab.TODAY,
    val date: LocalDate? = null,
    /** Hoy en la Ciudad de México: el check-in con código solo aplica a reservas de hoy. */
    val today: LocalDate? = null,
    val loading: Boolean = true,
    val error: UiText? = null,
    val items: List<Reservation> = emptyList(),
    /** Reserva con su hoja de acciones abierta. */
    val selected: Reservation? = null,
    val contactOpen: Boolean = false,
    val contactText: String = "",
    val saving: Boolean = false,
    val blockForm: BlockForm? = null,
    /** Horarios cerrados en esta visita, para poder reabrirlos (la API no los lista; ver revision-jorge). */
    val blocks: List<ScheduleBlock> = emptyList(),
    val message: UiText? = null
) {
    val canSendContact: Boolean get() = !saving && isValidContactText(contactText)
}

/** 49_NegReservas: lista del día, confirmar, llegó/no llegó, contactar y cerrar horarios (specs/nv-35). */
@Suppress("TooManyFunctions") // Un evento por control de la pantalla y de sus dos hojas.
@HiltViewModel
class BusinessReservationsViewModel @Inject constructor(
    private val repository: BusinessHomeRepository,
    private val clock: Clock
) : ViewModel() {
    private val _state = MutableStateFlow(BusinessReservationsUiState())
    val state: StateFlow<BusinessReservationsUiState> = _state.asStateFlow()

    private var businessId: String? = null
    private var loadJob: Job? = null

    private fun today(): LocalDate = LocalDate.now(clock.withZone(BUSINESS_ZONE))

    fun start(id: String) {
        if (businessId == id) return
        businessId = id
        _state.update { it.copy(date = today(), today = today()) }
        load()
    }

    fun selectTab(tab: ReservationsTab) {
        val date = when (tab) {
            ReservationsTab.TODAY -> today()
            ReservationsTab.UPCOMING -> today().plusDays(1)
            ReservationsTab.PAST -> today().minusDays(1)
        }
        _state.update { it.copy(tab = tab, date = date, items = emptyList()) }
        load()
    }

    /** Un día más lejos de hoy en Próximas (adelante) o en Pasadas (atrás). */
    fun nextDay() = shiftDay(farther = true)

    /** Un día más cerca de hoy, sin pasar de mañana o de ayer. */
    fun previousDay() = shiftDay(farther = false)

    private fun shiftDay(farther: Boolean) {
        val current = _state.value
        val date = current.date
        // Hoy no se recorre; Próximas no baja de mañana y Pasadas no sube de ayer.
        val next = when (current.tab) {
            ReservationsTab.TODAY -> null
            ReservationsTab.UPCOMING -> date?.plusDays(if (farther) 1L else -1L)?.takeIf { it.isAfter(today()) }
            ReservationsTab.PAST -> date?.plusDays(if (farther) -1L else 1L)?.takeIf { it.isBefore(today()) }
        } ?: return
        _state.update { it.copy(date = next, items = emptyList()) }
        load()
    }

    fun load() {
        val id = businessId ?: return
        val date = _state.value.date ?: return
        _state.update { it.copy(loading = true, error = null) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            when (val result = repository.reservations(id, date)) {
                is Result.Success -> _state.update {
                    it.copy(loading = false, items = result.data.items.sortedBy { r -> r.dateTime })
                }
                is Result.Failure -> _state.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    fun open(reservation: Reservation) {
        _state.update { it.copy(selected = reservation, contactOpen = false, contactText = "") }
    }

    fun dismiss() {
        _state.update { it.copy(selected = null, contactOpen = false, contactText = "") }
    }

    fun confirm() = changeStatus(ReservationStatus.CONFIRMED)

    fun markArrived() = changeStatus(ReservationStatus.ARRIVED)

    fun markNoShow() = changeStatus(ReservationStatus.NO_SHOW)

    private fun changeStatus(status: ReservationStatus) {
        val id = businessId ?: return
        val reservation = _state.value.selected ?: return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            when (val result = repository.updateReservation(id, reservation.id, status)) {
                is Result.Success -> _state.update { current ->
                    current.copy(
                        saving = false,
                        selected = null,
                        items = current.items.map { if (it.id == result.data.id) result.data else it },
                        message = UiText.Res(R.string.business_reservation_updated)
                    )
                }
                is Result.Failure -> {
                    val conflict = result.error is AppError.Conflict
                    _state.update {
                        it.copy(
                            saving = false,
                            selected = null,
                            message = if (conflict) {
                                UiText.Res(R.string.business_reservation_changed)
                            } else {
                                result.error.toUiText()
                            }
                        )
                    }
                    // 409: la reserva cambió en otro lado; se vuelve a pedir el día.
                    if (conflict) load()
                }
            }
        }
    }

    fun openContact() {
        _state.update { it.copy(contactOpen = true) }
    }

    fun onContactText(text: String) {
        _state.update { it.copy(contactText = text.take(CONTACT_MAX)) }
    }

    fun sendContact() {
        val id = businessId ?: return
        val current = _state.value
        val reservation = current.selected ?: return
        if (!current.canSendContact) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            when (val result = repository.contact(id, reservation.id, current.contactText)) {
                is Result.Success -> _state.update {
                    it.copy(
                        saving = false,
                        selected = null,
                        contactOpen = false,
                        contactText = "",
                        message = UiText.Res(R.string.business_contact_sent)
                    )
                }
                // Lo escrito no se pierde: la hoja sigue abierta con el texto.
                is Result.Failure -> _state.update { it.copy(saving = false, message = result.error.toUiText()) }
            }
        }
    }

    fun openBlock() {
        _state.update { it.copy(blockForm = BlockForm()) }
    }

    fun closeBlock() {
        _state.update { it.copy(blockForm = null) }
    }

    fun onBlockForm(form: BlockForm) {
        _state.update { it.copy(blockForm = form) }
    }

    fun saveBlock() {
        val id = businessId ?: return
        val current = _state.value
        val form = current.blockForm?.takeIf { it.valid } ?: return
        val date = current.date ?: return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val from = date.atTime(LocalTime.parse(form.from))
            val to = date.atTime(LocalTime.parse(form.to))
            when (val result = repository.block(id, from, to, form.reason)) {
                is Result.Success -> _state.update {
                    it.copy(
                        saving = false,
                        blockForm = null,
                        blocks = it.blocks + result.data,
                        message = UiText.Res(R.string.business_block_saved)
                    )
                }
                is Result.Failure -> _state.update { it.copy(saving = false, message = result.error.toUiText()) }
            }
        }
    }

    fun unblock(block: ScheduleBlock) {
        val id = businessId ?: return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            when (val result = repository.unblock(id, block.id)) {
                is Result.Success -> _state.update {
                    it.copy(
                        saving = false,
                        blocks = it.blocks - block,
                        message = UiText.Res(R.string.business_block_removed)
                    )
                }
                is Result.Failure -> _state.update { it.copy(saving = false, message = result.error.toUiText()) }
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
