package com.wherever.mobile.feature.plans.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.plans.domain.RoutePoint
import com.wherever.mobile.feature.plans.domain.RouteTotals
import com.wherever.mobile.feature.plans.domain.routeTotals
import com.wherever.mobile.feature.plans.domain.routeUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlanMapUiState(
    val loading: Boolean = true,
    val full: Boolean = false,
    val plan: Plan? = null,
    val points: List<RoutePoint> = emptyList(),
    val totals: RouteTotals = RouteTotals(0, 0.0, null),
    /** Paradas sin coordenadas (la ficha no respondió): van en la lista, no en el mapa. */
    val missing: Int = 0,
    val error: UiText? = null,
    val message: UiText? = null
)

sealed interface PlanMapEvent {
    data class Directions(val lat: Double, val lng: Double, val name: String) : PlanMapEvent

    data class StartRoute(val url: String) : PlanMapEvent
}

/** 13_Mapa (`full = false`) y 14_MapaFull (`full = true`), specs/nv-30 D-30f a D-30j. Lee `id` y `full`. */
@HiltViewModel
class PlanMapViewModel @Inject constructor(
    private val plans: PlansRepository,
    private val places: PlacesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val planId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(PlanMapUiState(full = savedStateHandle["full"] ?: false))
    val state: StateFlow<PlanMapUiState> = mutableState.asStateFlow()
    private val eventChannel = Channel<PlanMapEvent>(Channel.BUFFERED)
    val events: Flow<PlanMapEvent> = eventChannel.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = it.plan == null, error = null) }
        viewModelScope.launch {
            when (val result = plans.get(planId)) {
                is Result.Success -> {
                    val plan = result.data
                    val points = locate(plan)
                    mutableState.update {
                        it.copy(
                            loading = false,
                            plan = plan,
                            points = points,
                            totals = routeTotals(plan.stops, plan.transfers),
                            missing = plan.stops.size - points.size
                        )
                    }
                }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    /** «Cómo llegar a {primera parada}» (D-30g). */
    fun directionsToFirst() {
        val first = state.value.points.firstOrNull() ?: return
        viewModelScope.launch { eventChannel.send(PlanMapEvent.Directions(first.lat, first.lng, first.stop.name)) }
    }

    /** «Iniciar ruta» con todas las paradas (D-30g). */
    fun startRoute() {
        val plan = state.value.plan ?: return
        val url = routeUrl(state.value.points, plan.mobility) ?: return
        viewModelScope.launch { eventChannel.send(PlanMapEvent.StartRoute(url)) }
    }

    /** El teléfono no tiene app de mapas. */
    fun noMapsApp() = mutableState.update { it.copy(message = UiText.Res(R.string.plan_no_maps)) }

    fun messageShown() = mutableState.update { it.copy(message = null) }

    /** Coordenadas de cada parada, una ficha por lugar y en paralelo. Las que fallan quedan fuera del mapa. */
    private suspend fun locate(plan: Plan): List<RoutePoint> {
        val details = plan.stops.map { it.placeId }.distinct().map { id ->
            viewModelScope.async { id to places.detail(id) }
        }.awaitAll().toMap()
        return plan.stops.mapIndexedNotNull { index, stop ->
            val detail = (details[stop.placeId] as? Result.Success)?.data ?: return@mapIndexedNotNull null
            RoutePoint(index + 1, stop, detail.location.lat, detail.location.lng)
        }
    }
}
