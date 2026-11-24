package com.wherever.mobile.feature.groups.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.LocationProvider
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.groups.domain.Group
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import com.wherever.mobile.feature.groups.domain.MeetingPointRepository
import com.wherever.mobile.feature.groups.domain.MeetingSuggestion
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.groups.domain.MemberTrip
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeetingPointUiState(
    val group: Group? = null,
    val plan: Plan? = null,
    val myId: String? = null,
    val loading: Boolean = true,
    val loadError: UiText? = null,
    val suggestion: MeetingSuggestion? = null,
    /** Menos de 2 personas comparten su ubicación (422 `faltan_ubicaciones`). */
    val needLocations: Boolean = false,
    val selected: String? = null,
    val trips: List<MemberTrip> = emptyList(),
    val busy: Boolean = false,
    val error: UiText? = null
) {
    val sharing: Boolean
        get() = group?.members?.firstOrNull { it.userId == myId }?.sharesLocation == true

    val sharers: Int
        get() = group?.members?.count { it.sharesLocation } ?: 0

    val confirmed: Int
        get() = group?.members?.count { it.status == MemberStatus.CONFIRMED } ?: 0

    val canUse: Boolean
        get() = selected != null && !busy && selected != group?.meetingPoint?.placeId
}

/** 47_PuntoMedio (specs/nv-25): compartir mi ubicación, lugares sugeridos, fijar el punto y tiempos de cada quien. */
// Grupo, punto, plan, perfil (quién soy), ubicación y reloj: cada uno es su propia fuente.
@Suppress("LongParameterList")
@HiltViewModel
class MeetingPointViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groups: GroupsRepository,
    private val meeting: MeetingPointRepository,
    private val plans: PlansRepository,
    private val profiles: ProfileRepository,
    private val location: LocationProvider,
    private val clock: Clock
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(MeetingPointUiState())
    val state: StateFlow<MeetingPointUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = it.group == null, loadError = null) }
        viewModelScope.launch {
            val myId = profiles.observeProfile().first()?.id
            when (val result = groups.group(groupId)) {
                is Result.Success -> {
                    mutableState.update { it.copy(loading = false, group = result.data, myId = myId) }
                    (plans.get(result.data.planId) as? Result.Success)?.let { p ->
                        mutableState.update { it.copy(plan = p.data) }
                    }
                    refresh(result.data)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loading = false, loadError = result.error.toUiText())
                }
            }
        }
    }

    private suspend fun refresh(group: Group) {
        val suggestion = meeting.suggest(groupId)
        mutableState.update {
            when (suggestion) {
                is Result.Success -> it.copy(suggestion = suggestion.data, needLocations = false)
                is Result.Failure -> if (suggestion.error is AppError.Validation) {
                    it.copy(suggestion = null, needLocations = true)
                } else {
                    it.copy(error = suggestion.error.toUiText())
                }
            }
        }
        if (group.meetingPoint != null) {
            (meeting.trips(groupId) as? Result.Success)?.let { t -> mutableState.update { it.copy(trips = t.data) } }
        }
    }

    /** Tras el permiso en contexto: sin él (o sin ubicación) la pantalla sigue y lo explica. */
    fun share(granted: Boolean) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val point = if (granted) location.current() else null
            if (point == null) {
                mutableState.update { it.copy(busy = false, error = UiText.Res(R.string.meeting_no_location)) }
                return@launch
            }
            applyGroup(meeting.shareLocation(groupId, point))
        }
    }

    fun stopSharing() {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch { applyGroup(meeting.shareLocation(groupId, null)) }
    }

    fun select(placeId: String) = mutableState.update { it.copy(selected = placeId, error = null) }

    fun usePoint() {
        val current = state.value
        val placeId = current.selected ?: return
        if (!current.canUse) return
        mutableState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch { applyGroup(meeting.setPoint(groupId, placeId, meetingTime(current))) }
    }

    private suspend fun applyGroup(result: Result<Group>) {
        when (result) {
            is Result.Success -> {
                mutableState.update { it.copy(busy = false, group = result.data) }
                refresh(result.data)
            }
            is Result.Failure -> mutableState.update { it.copy(busy = false, error = result.error.toUiText()) }
        }
    }

    /** La hora que ya tiene el punto; si no, la llegada a la primera parada; si no, las 19:00 del día del plan. */
    private fun meetingTime(state: MeetingPointUiState): String = state.group?.meetingPoint?.at
        ?: state.plan?.stops?.minOfOrNull { it.arrives }
        ?: planDay(state.plan?.date).atTime(DEFAULT_MEETING_TIME).atZone(clock.zone).toOffsetDateTime().toString()

    private fun planDay(date: String?): LocalDate = date?.let {
        try {
            LocalDate.parse(it.take(DATE_LENGTH))
        } catch (_: DateTimeParseException) {
            null
        }
    } ?: LocalDate.now(clock)

    fun errorShown() = mutableState.update { it.copy(error = null) }

    private companion object {
        const val DATE_LENGTH = 10
        val DEFAULT_MEETING_TIME: LocalTime = LocalTime.of(19, 0)
    }
}
