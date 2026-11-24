package com.wherever.mobile.feature.places.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.FallbackPoint
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.location.LocationProvider
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.places.domain.MapArea
import com.wherever.mobile.feature.places.domain.MapCell
import com.wherever.mobile.feature.places.domain.MapPin
import com.wherever.mobile.feature.places.domain.PlaceMapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Categorías del filtro de 32_ExpMapa (mismas claves que `GET /lugares`). */
val MAP_CATEGORIES = listOf("restaurante", "cafeteria", "bar", "entretenimiento")

data class ExploreMapUiState(
    /** Centro inicial: mi ubicación aproximada o el centro de respaldo (CDMX). */
    val center: GeoPoint? = null,
    val pins: List<MapPin> = emptyList(),
    val cells: List<MapCell> = emptyList(),
    val loading: Boolean = false,
    val error: UiText? = null,
    val category: String? = null,
    val selected: MapPin? = null,
    /** El mapa se movió desde la última búsqueda: aparece «Buscar en esta zona». */
    val moved: Boolean = false,
    val lastArea: MapArea? = null
) {
    val empty: Boolean
        get() = !loading && error == null && lastArea != null && pins.isEmpty() && cells.isEmpty()
}

/** 32_ExpMapa (specs/nv-21, decisión D25: osmdroid + OpenStreetMap). */
@HiltViewModel
class ExploreMapViewModel @Inject constructor(
    private val repository: PlaceMapRepository,
    private val location: LocationProvider,
    @param:FallbackPoint private val fallback: GeoPoint
) : ViewModel() {
    private val mutableState = MutableStateFlow(ExploreMapUiState())
    val state: StateFlow<ExploreMapUiState> = mutableState.asStateFlow()
    private var search: Job? = null

    /** Zona de la última búsqueda: «Buscar en esta zona» sale solo si el mapa se alejó de ella. */
    private var searchedArea: MapArea? = null

    init {
        viewModelScope.launch {
            // La ubicación solo se usa para centrar el mapa; no se guarda ni se manda sola a la API.
            val here = if (location.hasPermission()) location.current() else null
            mutableState.update { it.copy(center = here ?: fallback) }
        }
    }

    /** El mapa terminó de moverse: la primera vez busca sola; después espera «Buscar en esta zona». */
    fun onArea(area: MapArea) {
        val searched = searchedArea
        mutableState.update { it.copy(lastArea = area, moved = searched != null && area.differsFrom(searched)) }
        if (searched == null) load(area)
    }

    fun searchHere() {
        val area = state.value.lastArea ?: return
        mutableState.update { it.copy(moved = false) }
        load(area)
    }

    fun selectCategory(category: String?) {
        mutableState.update { it.copy(category = if (it.category == category) null else category, selected = null) }
        state.value.lastArea?.let(::load)
    }

    fun select(pin: MapPin?) = mutableState.update { it.copy(selected = pin) }

    fun retry() {
        state.value.lastArea?.let(::load)
    }

    private fun load(area: MapArea) {
        search?.cancel()
        searchedArea = area
        mutableState.update { it.copy(loading = true, error = null) }
        search = viewModelScope.launch {
            val result = repository.area(area, state.value.category)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(
                        loading = false,
                        pins = result.data.pins,
                        cells = result.data.cells,
                        selected = it.selected?.takeIf { s -> result.data.pins.any { p -> p.id == s.id } }
                    )
                    is Result.Failure -> it.copy(loading = false, error = result.error.toUiText())
                }
            }
        }
    }
}
