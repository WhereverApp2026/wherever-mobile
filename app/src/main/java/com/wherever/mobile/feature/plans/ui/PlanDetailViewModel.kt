package com.wherever.mobile.feature.plans.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.shiftIso
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.plans.domain.Alternative
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlanStop
import com.wherever.mobile.feature.plans.domain.PlansRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val MOVE_STEP_MINUTES = 15L
internal const val NEW_STOP_MINUTES = 60
private const val GAP_MINUTES = 15L
private const val MIN_QUERY = 2
private const val MAX_QUERY = 60

data class RemovedStop(val stop: PlanStop)

/** Máximo de paradas al editar (spec nv-24: 2 a 4, como 17_AjustaPlan y 41_AgregarParada). */
internal const val PLAN_MAX_STOPS = 4

/** 40_Cambiar: alternativas de una parada, la elegida y la búsqueda «Buscar otro lugar». */
data class AlternativesSheet(
    val stopId: String,
    val loading: Boolean = true,
    val items: List<Alternative> = emptyList(),
    val selected: String? = null,
    val searchOpen: Boolean = false,
    val query: String = ""
)

enum class AddStopTab { SUGGESTED, SEARCH }

/** 41_AgregarParada: sugeridas cerca de la última parada y búsqueda por nombre. */
data class AddStopSheet(
    val tab: AddStopTab = AddStopTab.SUGGESTED,
    val suggestions: List<Alternative> = emptyList(),
    val loadingSuggestions: Boolean = true,
    val query: String = "",
    val results: List<PlaceCard> = emptyList(),
    val searching: Boolean = false
)

data class PlanDetailUiState(
    val loading: Boolean = true,
    val plan: Plan? = null,
    val busy: Boolean = false,
    val error: UiText? = null,
    val message: UiText? = null,
    val removed: RemovedStop? = null,
    val alternatives: AlternativesSheet? = null,
    val addStop: AddStopSheet? = null,
    val confirmDelete: Boolean = false,
    val renaming: String? = null
)

sealed interface PlanEvent {
    data class Share(val link: String) : PlanEvent

    data class Open(val planId: String) : PlanEvent

    data object Deleted : PlanEvent
}

/** 39_Plan, 40_Cambiar y 41_AgregarParada (specs/nv-24; US-055 a US-058, US-061, US-063 a US-066). */
@HiltViewModel
@Suppress("TooManyFunctions")
class PlanDetailViewModel @Inject constructor(
    private val plans: PlansRepository,
    private val places: PlacesRepository,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val planId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(PlanDetailUiState())
    val state: StateFlow<PlanDetailUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<PlanEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = it.plan == null, error = null) }
        viewModelScope.launch {
            when (val result = plans.get(planId)) {
                is Result.Success -> mutableState.update { it.copy(loading = false, plan = result.data) }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    /** US-063: quitar con deshacer. La última parada no se puede quitar (la API da 422). */
    fun removeStop(stop: PlanStop) {
        val plan = state.value.plan ?: return
        if (plan.stops.size <= 1) {
            mutableState.update { it.copy(message = UiText.Res(R.string.plan_last_stop)) }
            return
        }
        edit({ plans.removeStop(plan, stop.id) }) { it.copy(removed = RemovedStop(stop)) }
    }

    fun undoRemove() {
        val removed = state.value.removed ?: return
        val plan = state.value.plan ?: return
        mutableState.update { it.copy(removed = null) }
        edit({ plans.addStop(plan, removed.stop.placeId, removed.stop.arrives, removed.stop.minutes) })
    }

    fun undoExpired() = mutableState.update { it.copy(removed = null) }

    /** US-065: subir o bajar una parada. La API reparte las mismas horas en el orden nuevo. */
    fun moveUp(stop: PlanStop) = reorder(stop, -1)

    fun moveDown(stop: PlanStop) = reorder(stop, 1)

    private fun reorder(stop: PlanStop, delta: Int) {
        val plan = state.value.plan ?: return
        val ids = plan.stops.map { it.id }.toMutableList()
        val from = ids.indexOf(stop.id)
        val to = from + delta
        if (from < 0 || to !in ids.indices) return
        ids.add(to, ids.removeAt(from))
        edit({ plans.reorder(plan, ids) })
    }

    /** US-064: mover la hora de llegada en pasos de 15 min; la API recalcula traslados y costos (US-066). */
    fun shiftTime(stop: PlanStop, forward: Boolean) {
        val plan = state.value.plan ?: return
        val minutes = if (forward) MOVE_STEP_MINUTES else -MOVE_STEP_MINUTES
        edit({ plans.moveStop(plan, stop.id, shiftIso(stop.arrives, minutes)) })
    }

    /** 40_Cambiar (US-056): alternativas para cambiar una parada. */
    fun openAlternatives(stop: PlanStop) {
        mutableState.update { it.copy(alternatives = AlternativesSheet(stop.id)) }
        loadAlternatives(stop.id, query = null)
    }

    private fun loadAlternatives(stopId: String, query: String?) {
        val plan = state.value.plan ?: return
        viewModelScope.launch {
            val result = plans.alternatives(plan, stopId, query)
            mutableState.update {
                val sheet = it.alternatives?.takeIf { s -> s.stopId == stopId && (query == null || s.query == query) }
                    ?: return@update it
                it.copy(
                    alternatives = sheet.copy(
                        loading = false,
                        items = (result as? Result.Success)?.data.orEmpty(),
                        selected = null
                    ),
                    message = (result as? Result.Failure)?.error?.toUiText()
                )
            }
        }
    }

    fun closeAlternatives() = mutableState.update { it.copy(alternatives = null) }

    fun selectAlternative(alternative: Alternative) =
        mutableState.update { it.copy(alternatives = it.alternatives?.copy(selected = alternative.placeId)) }

    /** «Buscar otro lugar»: la API busca por nombre cerca de la parada (`?q=`, 2 a 60). */
    fun openAlternativeSearch() =
        mutableState.update { it.copy(alternatives = it.alternatives?.copy(searchOpen = true)) }

    fun onAlternativeQuery(query: String) {
        val sheet = state.value.alternatives ?: return
        val text = query.take(MAX_QUERY)
        mutableState.update { it.copy(alternatives = sheet.copy(query = text)) }
        if (text.trim().length < MIN_QUERY) return
        mutableState.update { it.copy(alternatives = it.alternatives?.copy(loading = true)) }
        loadAlternatives(sheet.stopId, text)
    }

    /** «Cambiar parada» con la alternativa elegida. */
    fun replaceSelected() {
        val plan = state.value.plan ?: return
        val sheet = state.value.alternatives ?: return
        val placeId = sheet.selected ?: return
        mutableState.update { it.copy(alternatives = null) }
        edit({ plans.replaceStop(plan, sheet.stopId, placeId) })
    }

    /** «Quitar esta parada» desde 40_Cambiar. */
    fun removeFromSheet() {
        val stop = state.value.plan?.stops?.firstOrNull { it.id == state.value.alternatives?.stopId } ?: return
        mutableState.update { it.copy(alternatives = null) }
        removeStop(stop)
    }

    /** 41_AgregarParada (US-057): sugeridas = alternativas de la última parada (cerca y abiertas a esa hora). */
    fun openAddStop() {
        val plan = state.value.plan ?: return
        if (plan.stops.size >= PLAN_MAX_STOPS) return
        mutableState.update { it.copy(addStop = AddStopSheet()) }
        val last = plan.stops.maxByOrNull { it.arrives } ?: run {
            mutableState.update { it.copy(addStop = it.addStop?.copy(loadingSuggestions = false)) }
            return
        }
        viewModelScope.launch {
            val found = (plans.alternatives(plan, last.id, null) as? Result.Success)?.data.orEmpty()
            mutableState.update { it.copy(addStop = it.addStop?.copy(suggestions = found, loadingSuggestions = false)) }
        }
    }

    fun closeAddStop() = mutableState.update { it.copy(addStop = null) }

    fun onAddTab(tab: AddStopTab) = mutableState.update { it.copy(addStop = it.addStop?.copy(tab = tab)) }

    fun onAddQuery(query: String) {
        mutableState.update { it.copy(addStop = it.addStop?.copy(query = query)) }
        if (query.trim().length < MIN_QUERY) return
        mutableState.update { it.copy(addStop = it.addStop?.copy(searching = true)) }
        viewModelScope.launch {
            val found = (places.search(query) as? Result.Success)?.data.orEmpty()
            mutableState.update {
                val sheet = it.addStop ?: return@update it
                if (sheet.query == query) it.copy(addStop = sheet.copy(results = found, searching = false)) else it
            }
        }
    }

    fun addPlace(place: PlaceCard) = addAt(place.id)

    fun addSuggestion(alternative: Alternative) = addAt(alternative.placeId)

    /** La parada nueva va al final, 15 min después de que termina la última. */
    private fun addAt(placeId: String) {
        val plan = state.value.plan ?: return
        val arrives = nextArrival(plan) ?: return
        mutableState.update { it.copy(addStop = null) }
        edit({ plans.addStop(plan, placeId, arrives, NEW_STOP_MINUTES) })
    }

    /** Hora de llegada de la parada que se agregaría. */
    fun nextArrival(plan: Plan): String? {
        val last = plan.stops.maxByOrNull { it.arrives } ?: return null
        return shiftIso(last.arrives, last.minutes + GAP_MINUTES)
    }

    /** US-061: enlace para compartir (vence en 7 días). */
    fun share() {
        val plan = state.value.plan ?: return
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            when (val result = plans.share(plan)) {
                is Result.Success -> {
                    mutableState.update { it.copy(busy = false) }
                    eventChannel.send(PlanEvent.Share(result.data.link))
                }
                is Result.Failure -> mutableState.update { it.copy(busy = false, message = result.error.toUiText()) }
            }
        }
    }

    /** US-058: repetir el plan mañana con las mismas horas. */
    fun repeatTomorrow() {
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            when (val result = plans.clone(planId, LocalDate.now(clock).plusDays(1).toString())) {
                is Result.Success -> {
                    mutableState.update { it.copy(busy = false) }
                    eventChannel.send(PlanEvent.Open(result.data.id))
                }
                is Result.Failure -> mutableState.update { it.copy(busy = false, message = result.error.toUiText()) }
            }
        }
    }

    fun startRename() = mutableState.update { it.copy(renaming = it.plan?.name.orEmpty()) }

    fun onRename(name: String) = mutableState.update { it.copy(renaming = name.take(NAME_MAX)) }

    fun cancelRename() = mutableState.update { it.copy(renaming = null) }

    fun confirmRename() {
        val plan = state.value.plan ?: return
        val name = state.value.renaming?.trim().orEmpty()
        mutableState.update { it.copy(renaming = null) }
        if (name.isEmpty() || name == plan.name) return
        edit({ plans.rename(plan, name) })
    }

    fun askDelete() = mutableState.update { it.copy(confirmDelete = true) }

    fun cancelDelete() = mutableState.update { it.copy(confirmDelete = false) }

    fun delete() {
        val plan = state.value.plan ?: return
        mutableState.update { it.copy(confirmDelete = false, busy = true) }
        viewModelScope.launch {
            when (val result = plans.delete(plan)) {
                is Result.Success -> eventChannel.send(PlanEvent.Deleted)
                is Result.Failure -> mutableState.update { it.copy(busy = false, message = result.error.toUiText()) }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }

    /**
     * Toda edición devuelve el plan recalculado. Con 409 (otro dispositivo cambió el plan) se recarga y se
     * avisa, sin reintentar a ciegas.
     */
    private fun edit(call: suspend () -> Result<Plan>, onSuccess: (PlanDetailUiState) -> PlanDetailUiState = { it }) {
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            when (val result = call()) {
                is Result.Success -> mutableState.update { onSuccess(it.copy(busy = false, plan = result.data)) }
                is Result.Failure -> {
                    val conflict = result.error is AppError.Conflict
                    mutableState.update {
                        it.copy(
                            busy = false,
                            message = if (conflict) UiText.Res(R.string.plan_conflict) else result.error.toUiText()
                        )
                    }
                    if (conflict) load()
                }
            }
        }
    }

    private companion object {
        const val NAME_MAX = 80
    }
}
