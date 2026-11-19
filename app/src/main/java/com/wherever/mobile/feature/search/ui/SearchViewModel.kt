package com.wherever.mobile.feature.search.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.plans.domain.PlanSummary
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.plans.domain.PlansTab
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Espera entre teclas antes de buscar. */
const val SEARCH_DEBOUNCE_MILLIS = 350L

/** Mínimo de letras para buscar personas (la API pide 2). */
private const val MIN_QUERY = 2

enum class SearchTab { PLACES, PEOPLE, PLANS }

data class SearchUiState(
    val query: String = "",
    val tab: SearchTab = SearchTab.PLACES,
    val recents: List<String> = emptyList(),
    val places: List<PlaceCard> = emptyList(),
    val people: List<Person> = emptyList(),
    val plans: List<PlanSummary> = emptyList(),
    val loading: Boolean = false,
    /** Ya se buscó `query` en la pestaña actual (para mostrar "sin resultados" y no el estado inicial). */
    val searched: Boolean = false,
    val sent: Set<String> = emptySet(),
    val sending: Set<String> = emptySet(),
    val error: UiText? = null
) {
    /** Personas solo se buscan por @usuario (3 a 20 de `a-z0-9_`) o teléfono (backend nv-35). */
    val peopleQueryInvalid: Boolean get() = tab == SearchTab.PEOPLE && hasQuery && peopleQuery(query) == null

    val hasQuery: Boolean get() = query.trim().length >= MIN_QUERY

    val emptyResults: Boolean
        get() = searched &&
            !loading &&
            when (tab) {
                SearchTab.PLACES -> places.isEmpty()
                SearchTab.PEOPLE -> people.isEmpty()
                SearchTab.PLANS -> plans.isEmpty()
            }
}

/**
 * 19_Buscar, 20_BuscarPersonas y 21_SinResultados (specs/nv-19). Lugares con `GET /lugares/buscar`,
 * personas con `GET /personas/buscar` y planes propios filtrados por nombre (la API no tiene búsqueda de planes).
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val places: PlacesRepository,
    private val friends: FriendsRepository,
    private val plans: PlansRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(SearchUiState(query = savedStateHandle["query"] ?: ""))
    val state: StateFlow<SearchUiState> = mutableState.asStateFlow()

    private var searchJob: Job? = null
    private var myPlans: List<PlanSummary>? = null

    init {
        loadRecents()
        if (state.value.hasQuery) search(debounce = false)
    }

    fun onQueryChange(value: String) {
        mutableState.update { it.copy(query = value, searched = false, error = null) }
        search(debounce = true)
    }

    fun onTab(tab: SearchTab) {
        mutableState.update { it.copy(tab = tab, searched = false, error = null) }
        search(debounce = false)
    }

    fun useRecent(text: String) {
        mutableState.update { it.copy(query = text, searched = false) }
        search(debounce = false)
    }

    fun clearRecents() {
        val before = state.value.recents
        mutableState.update { it.copy(recents = emptyList()) }
        viewModelScope.launch {
            val result = places.clearRecentSearches()
            if (result is Result.Failure) {
                mutableState.update {
                    it.copy(recents = before, error = result.error.toUiText())
                }
            }
        }
    }

    fun retry() = search(debounce = false)

    fun request(person: Person) {
        if (person.id in state.value.sending) return
        mutableState.update { it.copy(sending = it.sending + person.id, error = null) }
        viewModelScope.launch {
            val result = friends.request(person.id)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(sending = it.sending - person.id, sent = it.sent + person.id)
                    is Result.Failure -> it.copy(sending = it.sending - person.id, error = result.error.toUiText())
                }
            }
        }
    }

    private fun loadRecents() {
        viewModelScope.launch {
            val result = places.recentSearches()
            if (result is Result.Success) mutableState.update { s -> s.copy(recents = result.data.map { it.text }) }
        }
    }

    private fun search(debounce: Boolean) {
        searchJob?.cancel()
        val current = state.value
        if (!current.hasQuery) {
            mutableState.update {
                it.copy(loading = false, places = emptyList(), people = emptyList(), plans = emptyList())
            }
            return
        }
        if (current.peopleQueryInvalid) {
            mutableState.update { it.copy(loading = false, people = emptyList(), searched = false) }
            return
        }
        searchJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MILLIS)
            mutableState.update { it.copy(loading = true) }
            val error = runSearch(current.tab, current.query.trim())
            mutableState.update { it.copy(loading = false, searched = error == null, error = error) }
            if (current.tab == SearchTab.PLACES && error == null) loadRecents()
        }
    }

    /** Busca en la pestaña y guarda los resultados; devuelve el error, si hubo. */
    private suspend fun runSearch(tab: SearchTab, query: String): UiText? {
        val result: Result<*> = when (tab) {
            SearchTab.PLACES -> places.search(query).also { r ->
                if (r is Result.Success) mutableState.update { it.copy(places = r.data) }
            }
            SearchTab.PEOPLE -> friends.search(peopleQuery(query) ?: return null).also { r ->
                if (r is Result.Success) mutableState.update { it.copy(people = r.data) }
            }
            SearchTab.PLANS -> plansMatching(query).also { r ->
                if (r is Result.Success) mutableState.update { it.copy(plans = r.data) }
            }
        }
        return (result as? Result.Failure)?.error?.toUiText()
    }

    /** Planes propios (próximos, guardados y pasados) cuyo nombre contiene el texto, sin acentos ni mayúsculas. */
    private suspend fun plansMatching(query: String): Result<List<PlanSummary>> {
        val all = myPlans ?: run {
            val results = PlansTab.entries.map { tab -> viewModelScope.async { plans.list(tab) } }.awaitAll()
            results.firstOrNull { it is Result.Failure }?.let { return it as Result.Failure }
            results.flatMap { (it as Result.Success).data }.distinctBy { it.id }.also { myPlans = it }
        }
        val needle = query.normalized()
        return Result.Success(all.filter { needle in it.name.normalized() })
    }
}

private val USERNAME = Regex("^[a-z0-9_]{3,20}$")
private const val MIN_PHONE_DIGITS = 10

/** El texto listo para `GET /personas/buscar`, o `null` si la API lo rechazaría con 422. */
internal fun peopleQuery(raw: String): String? {
    val text = raw.trim()
    val digits = text.count(Char::isDigit)
    return when {
        text.startsWith("+") || digits >= MIN_PHONE_DIGITS -> text.filter { it.isDigit() || it == '+' }
        else -> text.removePrefix("@").lowercase().takeIf { USERNAME.matches(it) }
    }
}

internal fun String.normalized(): String =
    java.text.Normalizer.normalize(lowercase(), java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
