package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BusinessHomeRepository
import com.wherever.mobile.feature.business.domain.CONTACT_MAX
import com.wherever.mobile.feature.business.domain.Reservation
import com.wherever.mobile.feature.business.domain.ReservationCharge
import com.wherever.mobile.feature.business.domain.ReservationStatus
import com.wherever.mobile.feature.business.domain.isValidContactText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Largo máximo del código de reserva (WH-4821). */
private const val CODE_MAX = 16

data class CheckinUiState(
    val code: String = "",
    val searching: Boolean = false,
    val reservation: Reservation? = null,
    /** Cobro de la reserva; nulo si no hubo pago (404) o no hay anticipo. */
    val charge: ReservationCharge? = null,
    val notFound: Boolean = false,
    val confirming: Boolean = false,
    val arrived: Boolean = false,
    val error: UiText? = null,
    val contactOpen: Boolean = false,
    val contactText: String = "",
    val sending: Boolean = false,
    val message: UiText? = null
) {
    val canConfirm: Boolean get() = code.isNotBlank() && !confirming && !arrived

    /** Contactar solo con la reserva encontrada (reemplaza a Reprogramar, que no existe en la API). */
    val canContact: Boolean get() = reservation != null

    val canSendContact: Boolean get() = !sending && isValidContactText(contactText)
}

/** 50_NegCheckin: código escrito → `POST reservas/escanear` (specs/nv-35). El QR queda en D-35a. */
@HiltViewModel
class CheckinViewModel @Inject constructor(private val repository: BusinessHomeRepository) : ViewModel() {
    private val _state = MutableStateFlow(CheckinUiState())
    val state: StateFlow<CheckinUiState> = _state.asStateFlow()

    private var businessId: String? = null

    fun start(id: String, code: String?) {
        if (businessId == id) return
        businessId = id
        if (!code.isNullOrBlank()) {
            onCode(code)
            search()
        }
    }

    fun onCode(text: String) {
        val code = text.uppercase().filter { it.isLetterOrDigit() || it == '-' }.take(CODE_MAX)
        _state.update {
            if (code == it.code) it else CheckinUiState(code = code)
        }
    }

    /** Busca la reserva de hoy por el inicio del código (`?q=`) para mostrar al cliente antes de confirmar. */
    fun search() {
        val id = businessId ?: return
        val code = _state.value.code.takeIf { it.isNotBlank() } ?: return
        _state.update { it.copy(searching = true, error = null, notFound = false) }
        viewModelScope.launch {
            when (val result = repository.reservations(id, date = null, query = code)) {
                is Result.Success -> {
                    val match = result.data.items.firstOrNull { it.code.equals(code, ignoreCase = true) }
                    _state.update { it.copy(searching = false, reservation = match, notFound = match == null) }
                    if (match != null) loadCharge(id, match)
                }
                is Result.Failure -> _state.update { it.copy(searching = false, error = result.error.toUiText()) }
            }
        }
    }

    private suspend fun loadCharge(id: String, reservation: Reservation) {
        if (reservation.deposit <= 0) return
        val charge = (repository.charge(id, reservation.id) as? Result.Success)?.data
        _state.update { it.copy(charge = charge) }
    }

    fun confirm() {
        val id = businessId ?: return
        val current = _state.value
        if (!current.canConfirm) return
        _state.update { it.copy(confirming = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.checkIn(id, current.code)) {
                is Result.Success -> _state.update {
                    it.copy(
                        confirming = false,
                        arrived = true,
                        reservation = result.data.copy(status = ReservationStatus.ARRIVED),
                        notFound = false,
                        message = UiText.Res(R.string.business_checkin_done)
                    )
                }
                is Result.Failure -> _state.update {
                    it.copy(
                        confirming = false,
                        error = when (result.error) {
                            is AppError.Conflict -> UiText.Res(R.string.business_checkin_used)
                            is AppError.NotFound -> UiText.Res(R.string.business_checkin_not_found)
                            else -> result.error.toUiText()
                        }
                    )
                }
            }
        }
    }

    fun openContact() {
        _state.update { it.copy(contactOpen = true) }
    }

    fun closeContact() {
        _state.update { it.copy(contactOpen = false) }
    }

    fun onContactText(text: String) {
        _state.update { it.copy(contactText = text.take(CONTACT_MAX)) }
    }

    fun sendContact() {
        val id = businessId ?: return
        val current = _state.value
        val reservation = current.reservation ?: return
        if (!current.canSendContact) return
        _state.update { it.copy(sending = true) }
        viewModelScope.launch {
            when (val result = repository.contact(id, reservation.id, current.contactText)) {
                is Result.Success -> _state.update {
                    it.copy(
                        sending = false,
                        contactOpen = false,
                        contactText = "",
                        message = UiText.Res(R.string.business_contact_sent)
                    )
                }
                is Result.Failure -> _state.update { it.copy(sending = false, message = result.error.toUiText()) }
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
