package com.wherever.mobile.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.FallbackPoint
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.location.LocationPermissionPreferences
import com.wherever.mobile.core.location.LocationProvider
import com.wherever.mobile.core.location.MAX_LOCATION_DENIALS
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.home.domain.Company
import com.wherever.mobile.feature.home.domain.Event
import com.wherever.mobile.feature.home.domain.HomeRepository
import com.wherever.mobile.feature.home.domain.MoodChip
import com.wherever.mobile.feature.home.domain.Weather
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.plans.domain.PlanAssistantRepository
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.plans.domain.PopularPlan
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Qué aviso de ubicación se muestra (US-004, US-009). */
enum class LocationPrompt { NONE, ASK, SETTINGS }

data class HomeUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: UiText? = null,
    val firstName: String? = null,
    val city: String? = null,
    val greeting: String = "",
    val weather: Weather? = null,
    val chips: List<MoodChip> = emptyList(),
    val recommendations: List<PlaceCard> = emptyList(),
    val events: List<Event> = emptyList(),
    val popular: List<PopularPlan> = emptyList(),
    val company: Company? = null,
    val locationPrompt: LocationPrompt = LocationPrompt.NONE,
    val usingLocation: Boolean = false,
    /** Último descarte, para "Deshacer" (US-053). */
    val dismissed: PlaceCard? = null,
    /** Pedir avisos al llegar (nv-46) solo si no se decidió ya en `08_Permisos` (nv-11). */
    val askNotifications: Boolean = false
)

/** 10_Inicio (US-025, US-032, US-037, US-047, US-053, US-062, US-030). */
@Suppress("LongParameterList") // Hilt inyecta cada fuente de Inicio por separado; agruparlas solo movería el problema.
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val profiles: ProfileRepository,
    private val location: LocationProvider,
    private val permissionPrefs: LocationPermissionPreferences,
    @FallbackPoint private val fallback: GeoPoint,
    private val assistant: PlanAssistantRepository,
    private val plans: PlansRepository,
    private val clock: Clock
) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()

    private var loadJob: Job? = null
    private val openPlanChannel = Channel<String>(Channel.BUFFERED)

    /** Id del plan copiado (US-033): la pantalla lo abre. */
    val openPlan = openPlanChannel.receiveAsFlow()
    private var dismissedIndex = 0

    init {
        viewModelScope.launch {
            profiles.observeProfile().collect { profile ->
                mutableState.update {
                    it.copy(firstName = profile?.name?.substringBefore(' '), city = profile?.city?.name)
                }
            }
        }
        mutableState.update { it.copy(askNotifications = !permissionPrefs.notificationsDecided) }
        updatePrompt()
        load(refresh = false)
    }

    /** Al volver de Ajustes del sistema el permiso pudo cambiar. */
    fun onResume() {
        val hadLocation = state.value.usingLocation
        updatePrompt()
        if (!hadLocation && location.hasPermission()) load(refresh = false)
    }

    fun refresh() = load(refresh = true)

    fun onCompany(company: Company) {
        mutableState.update { it.copy(company = company.takeIf { c -> c != it.company }) }
        load(refresh = false)
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) load(refresh = false) else permissionPrefs.recordDenied()
        updatePrompt()
    }

    fun onLocationNotNow() {
        permissionPrefs.dismiss()
        updatePrompt()
    }

    fun notInterested(place: PlaceCard) {
        val current = state.value.recommendations
        dismissedIndex = current.indexOfFirst { it.id == place.id }.coerceAtLeast(0)
        mutableState.update { it.copy(recommendations = current - place, dismissed = place) }
        viewModelScope.launch {
            val result = repository.notInterested(place.id)
            if (result is Result.Failure) {
                mutableState.update {
                    it.copy(recommendations = current, dismissed = null, error = result.error.toUiText())
                }
            }
        }
    }

    fun undoNotInterested() {
        val place = state.value.dismissed ?: return
        mutableState.update {
            val restored = it.recommendations.toMutableList().apply { add(dismissedIndex.coerceAtMost(size), place) }
            it.copy(recommendations = restored, dismissed = null)
        }
        viewModelScope.launch { repository.undoNotInterested(place.id) }
    }

    fun undoExpired() = mutableState.update { it.copy(dismissed = null) }

    /** US-033: copia un plan popular para mañana con un toque. */
    fun copyPopular(plan: PopularPlan) {
        viewModelScope.launch {
            when (val result = plans.clone(plan.id, LocalDate.now(clock).plusDays(1).toString())) {
                is Result.Success -> openPlanChannel.send(result.data.id)
                is Result.Failure -> mutableState.update { it.copy(error = result.error.toUiText()) }
            }
        }
    }

    private fun updatePrompt() {
        val prompt = when {
            location.hasPermission() -> LocationPrompt.NONE
            permissionPrefs.deniedCount >= MAX_LOCATION_DENIALS -> if (permissionPrefs.dismissed) {
                LocationPrompt.NONE
            } else {
                LocationPrompt.SETTINGS
            }
            permissionPrefs.dismissed -> LocationPrompt.NONE
            else -> LocationPrompt.ASK
        }
        mutableState.update { it.copy(locationPrompt = prompt) }
    }

    private fun load(refresh: Boolean) {
        loadJob?.cancel()
        mutableState.update {
            it.copy(loading = !refresh && it.recommendations.isEmpty(), refreshing = refresh, error = null)
        }
        loadJob = viewModelScope.launch {
            val near: GeoPoint? = location.current()
            val company = state.value.company
            // Sin ubicación, la zona de respaldo: `GET /inicio` da 422 sin lat y lng.
            when (val result = repository.feed(near ?: fallback, company)) {
                is Result.Success -> mutableState.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        usingLocation = near != null,
                        greeting = result.data.greeting,
                        weather = result.data.weather,
                        chips = result.data.chips,
                        recommendations = result.data.recommendations
                    )
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loading = false, refreshing = false, error = result.error.toUiText())
                }
            }
            val events = near?.let { (repository.events(it) as? Result.Success)?.data }.orEmpty()
            val popular = near?.let { (assistant.popular(it) as? Result.Success)?.data }.orEmpty()
            mutableState.update { it.copy(events = events, popular = popular) }
        }
    }
}
