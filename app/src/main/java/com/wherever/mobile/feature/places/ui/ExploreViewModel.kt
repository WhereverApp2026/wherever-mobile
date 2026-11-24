package com.wherever.mobile.feature.places.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.location.LocationProvider
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.places.domain.Category
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.places.domain.PlaceFilters
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.places.domain.RecentSearch
import com.wherever.mobile.feature.places.domain.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val SEARCH_DEBOUNCE_MILLIS = 400L
internal const val MIN_QUERY_LENGTH = 2
internal const val NEARBY_RADIUS_METERS = 2_000

/** Filtros de sí o no de Explorar. */
enum class FilterFlag { OPEN_NOW, SEAL }

data class ExploreUiState(
    val query: String = "",
    val filters: PlaceFilters = PlaceFilters(),
    val categories: List<Category> = emptyList(),
    val results: List<PlaceCard> = emptyList(),
    val nextCursor: String? = null,
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val error: UiText? = null,
    val recentSearches: List<RecentSearch> = emptyList(),
    val recentlyViewed: List<PlaceCard> = emptyList(),
    val hasLocation: Boolean = false
) {
    val searching: Boolean get() = query.trim().length >= MIN_QUERY_LENGTH

    /** US-187: sin resultados con algo escrito o con filtros puestos. */
    val showEmpty: Boolean get() = !loading && error == null && results.isEmpty()
}

/** Explorar: lista con filtros (US-028), búsqueda (US-027), recientes (US-034) y vacío útil (US-187). */
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: PlacesRepository,
    private val location: LocationProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        ExploreUiState(
            query = savedStateHandle.get<String>("query").orEmpty(),
            filters = PlaceFilters(
                category = savedStateHandle.get<String>("category")?.takeIf { it.isNotBlank() },
                seal = savedStateHandle.get<Boolean>("seal") == true
            )
        )
    )
    val state: StateFlow<ExploreUiState> = mutableState.asStateFlow()

    private var near: GeoPoint? = null
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            near = location.current()
            mutableState.update { it.copy(hasLocation = near != null) }
            reload(debounce = false)
        }
        viewModelScope.launch {
            (repository.categories() as? Result.Success)?.let { r ->
                mutableState.update { it.copy(categories = r.data) }
            }
        }
        loadRecents()
    }

    fun onQueryChange(query: String) {
        mutableState.update { it.copy(query = query) }
        reload(debounce = true)
    }

    fun onRecentSearch(text: String) {
        mutableState.update { it.copy(query = text) }
        reload(debounce = false)
    }

    fun onCategory(id: String?) = updateFilters { it.copy(category = id.takeIf { c -> c != it.category }) }

    fun onMaxPrice(level: Int?) = updateFilters { it.copy(maxPriceLevel = level.takeIf { l -> l != it.maxPriceLevel }) }

    /** «Abierto ahora» y «Con Sello»: se prenden y apagan igual. */
    fun onToggle(flag: FilterFlag) = updateFilters {
        when (flag) {
            FilterFlag.OPEN_NOW -> it.copy(openNow = !it.openNow)
            FilterFlag.SEAL -> it.copy(seal = !it.seal)
        }
    }

    fun onNearby() = updateFilters {
        it.copy(radiusMeters = NEARBY_RADIUS_METERS.takeIf { _ -> it.radiusMeters == null })
    }

    fun onOrder(order: SortOrder?) = updateFilters { it.copy(order = order.takeIf { o -> o != it.order }) }

    fun clearFilters() {
        mutableState.update { it.copy(query = "", filters = PlaceFilters()) }
        reload(debounce = false)
    }

    fun clearRecentSearches() {
        mutableState.update { it.copy(recentSearches = emptyList()) }
        viewModelScope.launch { repository.clearRecentSearches() }
    }

    fun retry() = reload(debounce = false)

    fun loadMore() {
        val current = state.value
        val cursor = current.nextCursor ?: return
        if (current.loadingMore || current.searching) return
        mutableState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            when (val result = repository.list(current.filters, near, cursor)) {
                is Result.Success -> mutableState.update {
                    it.copy(
                        loadingMore = false,
                        results = it.results + result.data.items,
                        nextCursor = result.data.nextCursor
                    )
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loadingMore = false, error = result.error.toUiText())
                }
            }
        }
    }

    private fun updateFilters(change: (PlaceFilters) -> PlaceFilters) {
        mutableState.update { it.copy(filters = change(it.filters)) }
        reload(debounce = false)
    }

    private fun loadRecents() {
        viewModelScope.launch {
            (repository.recentSearches() as? Result.Success)?.let { r ->
                mutableState.update { it.copy(recentSearches = r.data) }
            }
        }
        viewModelScope.launch {
            (repository.recentlyViewed() as? Result.Success)?.let { r ->
                mutableState.update { it.copy(recentlyViewed = r.data) }
            }
        }
    }

    private fun reload(debounce: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MILLIS)
            mutableState.update { it.copy(loading = true, error = null) }
            val current = state.value
            if (current.searching) {
                when (val result = repository.search(current.query)) {
                    is Result.Success -> {
                        val filtered = result.data.filter { current.filters.matches(it) }
                        mutableState.update { it.copy(loading = false, results = filtered, nextCursor = null) }
                    }
                    is Result.Failure -> fail(result)
                }
            } else {
                when (val result = repository.list(current.filters, near, cursor = null)) {
                    is Result.Success -> mutableState.update {
                        it.copy(loading = false, results = result.data.items, nextCursor = result.data.nextCursor)
                    }
                    is Result.Failure -> fail(result)
                }
            }
        }
    }

    private fun fail(result: Result.Failure) {
        mutableState.update { it.copy(loading = false, results = emptyList(), error = result.error.toUiText()) }
    }
}

/** `GET /lugares/buscar` no recibe filtros: se aplican sobre la página que llega. */
internal fun PlaceFilters.matches(place: PlaceCard): Boolean = (category == null || place.category == category) &&
    (maxPriceLevel == null || place.priceLevel <= maxPriceLevel) &&
    (!openNow || place.openNow) &&
    (!seal || place.seal)
