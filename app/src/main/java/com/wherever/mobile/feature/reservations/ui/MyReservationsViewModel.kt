package com.wherever.mobile.feature.reservations.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.reservations.domain.Reservation
import com.wherever.mobile.feature.reservations.domain.ReservationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyReservationsUiState(
    val upcoming: Boolean = true,
    val items: List<Reservation> = emptyList(),
    /** Nombre de cada negocio: la reserva solo trae `negocio_id`. */
    val placeNames: Map<String, String> = emptyMap(),
    val nextCursor: String? = null,
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val loadError: UiText? = null,
    val error: UiText? = null
)

/** Mis reservas: próximas y pasadas (US-077). */
@HiltViewModel
class MyReservationsViewModel @Inject constructor(
    private val repository: ReservationsRepository,
    private val places: PlacesRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(MyReservationsUiState())
    val state: StateFlow<MyReservationsUiState> = mutableState.asStateFlow()

    private var loadJob: Job? = null

    /** Al volver de una reserva: pudo cancelarse o cambiar de hora. */
    fun load() {
        val upcoming = state.value.upcoming
        loadJob?.cancel()
        mutableState.update { it.copy(loading = it.items.isEmpty(), loadError = null) }
        loadJob = viewModelScope.launch {
            when (val result = repository.list(upcoming)) {
                is Result.Success -> {
                    mutableState.update {
                        it.copy(loading = false, items = result.data.items, nextCursor = result.data.nextCursor)
                    }
                    resolveNames(result.data.items)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loading = false, loadError = result.error.toUiText())
                }
            }
        }
    }

    fun onUpcoming(upcoming: Boolean) {
        if (upcoming == state.value.upcoming) return
        mutableState.update { it.copy(upcoming = upcoming, items = emptyList(), nextCursor = null) }
        load()
    }

    fun loadMore() {
        val current = state.value
        val cursor = current.nextCursor ?: return
        if (current.loadingMore) return
        mutableState.update { it.copy(loadingMore = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.list(current.upcoming, cursor)) {
                is Result.Success -> {
                    mutableState.update { state ->
                        val known = state.items.map { it.id }.toSet()
                        state.copy(
                            loadingMore = false,
                            items = state.items + result.data.items.filterNot { it.id in known },
                            nextCursor = result.data.nextCursor
                        )
                    }
                    resolveNames(result.data.items)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loadingMore = false, error = result.error.toUiText())
                }
            }
        }
    }

    /** Un negocio que ya no se puede leer se queda sin nombre (la fila muestra uno genérico). */
    private suspend fun resolveNames(items: List<Reservation>) {
        items.map { it.placeId }.distinct().filterNot { it in state.value.placeNames }.forEach { id ->
            (places.detail(id) as? Result.Success)?.let { place ->
                mutableState.update { it.copy(placeNames = it.placeNames + (id to place.data.name)) }
            }
        }
    }
}
