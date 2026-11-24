package com.wherever.mobile.feature.places.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.LocationProvider
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.places.domain.PlaceFilters
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.places.domain.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Cuántos negocios con Sello se muestran en 34_Sello; el resto está en Explorar con el filtro. */
internal const val SEAL_NEARBY_LIMIT = 3

data class SealUiState(
    val loading: Boolean = true,
    val places: List<PlaceCard> = emptyList(),
    val error: UiText? = null
)

/** 34_Sello (specs/nv-21): qué es el Sello, su regla (nv-56) y los negocios con Sello cerca (`con_sello`). */
@HiltViewModel
class SealViewModel @Inject constructor(
    private val repository: PlacesRepository,
    private val location: LocationProvider
) : ViewModel() {
    private val mutableState = MutableStateFlow(SealUiState())
    val state: StateFlow<SealUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val near = location.current()
            // Sin ubicación se ordenan por calificación (la API ordena por distancia solo con ella).
            val filters = PlaceFilters(seal = true, order = SortOrder.RATING.takeIf { near == null })
            val result = repository.list(filters, near, cursor = null)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(loading = false, places = result.data.items.take(SEAL_NEARBY_LIMIT))
                    is Result.Failure -> it.copy(loading = false, error = result.error.toUiText())
                }
            }
        }
    }
}
