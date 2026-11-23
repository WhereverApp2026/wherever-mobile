package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BusinessDataChange
import com.wherever.mobile.feature.business.domain.BusinessProfile
import com.wherever.mobile.feature.business.domain.BusinessProfileRepository
import com.wherever.mobile.feature.business.domain.DAYS_IN_WEEK
import com.wherever.mobile.feature.business.domain.DESCRIPTION_MAX
import com.wherever.mobile.feature.business.domain.DaySchedule
import com.wherever.mobile.feature.business.domain.PHONE_LENGTH
import com.wherever.mobile.feature.business.domain.isValidPhone
import com.wherever.mobile.feature.business.domain.isValidRange
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BusinessDataUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val profile: BusinessProfile? = null,
    val description: String = "",
    val phone: String = "",
    val schedules: List<DaySchedule> = emptyList(),
    val saving: Boolean = false,
    val saveError: UiText? = null
) {
    val canSave: Boolean
        get() = profile != null &&
            !saving &&
            description.trim().isNotEmpty() &&
            isValidPhone(phone) &&
            schedules.all { it.closed || it.isValidRange() }
}

/** «Datos y horario» de 58_NegPerfil (specs/nv-38): `PATCH /negocios/{id}` con `If-Match`. */
@HiltViewModel
class BusinessDataViewModel @Inject constructor(private val repository: BusinessProfileRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(BusinessDataUiState())
    val state: StateFlow<BusinessDataUiState> = mutableState.asStateFlow()

    private val savedChannel = Channel<Unit>(Channel.BUFFERED)
    val saved = savedChannel.receiveAsFlow()

    private var businessId: String? = null

    fun start(id: String) {
        if (businessId == id) return
        businessId = id
        load()
    }

    fun load() {
        val id = businessId ?: return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.profile(id)) {
                is Result.Success -> mutableState.update { it.fill(result.data) }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    fun onDescription(text: String) = mutableState.update {
        it.copy(description = text.take(DESCRIPTION_MAX), saveError = null)
    }

    fun onPhone(text: String) =
        mutableState.update { it.copy(phone = text.filter { c -> c.isDigit() }.take(PHONE_LENGTH), saveError = null) }

    fun onSchedule(schedule: DaySchedule) = mutableState.update { state ->
        state.copy(schedules = state.schedules.map { if (it.day == schedule.day) schedule else it }, saveError = null)
    }

    /** 409: alguien más guardó antes; se cargan sus datos y se avisa (lo escrito se pierde a propósito). */
    fun save() {
        val current = state.value
        val profile = current.profile ?: return
        if (!current.canSave) return
        mutableState.update { it.copy(saving = true, saveError = null) }
        viewModelScope.launch {
            val change = BusinessDataChange(current.description, current.phone, current.schedules)
            when (val result = repository.updateData(profile, change)) {
                is Result.Success -> {
                    mutableState.update { it.fill(result.data).copy(saving = false) }
                    savedChannel.send(Unit)
                }
                is Result.Failure -> if (result.error is AppError.Conflict) {
                    mutableState.update { it.copy(saving = false, saveError = UiText.Res(R.string.biz_data_conflict)) }
                    reloadKeepingError()
                } else {
                    mutableState.update { it.copy(saving = false, saveError = result.error.toUiText()) }
                }
            }
        }
    }

    private suspend fun reloadKeepingError() {
        val id = businessId ?: return
        val result = repository.profile(id)
        if (result is Result.Success) mutableState.update { it.fill(result.data).copy(saveError = it.saveError) }
    }
}

/** Los 7 días, aunque la API mande menos (un día sin fila = cerrado). */
private fun BusinessDataUiState.fill(profile: BusinessProfile) = copy(
    loading = false,
    profile = profile,
    description = profile.description,
    phone = profile.phone.filter { it.isDigit() }.takeLast(PHONE_LENGTH),
    schedules = (0 until DAYS_IN_WEEK).map { day ->
        profile.schedules.firstOrNull { it.day == day } ?: DaySchedule(day)
    }
)
