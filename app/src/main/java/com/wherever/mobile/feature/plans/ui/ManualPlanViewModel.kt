package com.wherever.mobile.feature.plans.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.plans.domain.ManualPlan
import com.wherever.mobile.feature.plans.domain.ManualStop
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Nombre del plan: hasta 40 caracteres (CrearPlan, «15/40»). */
internal const val PLAN_NAME_MAX = 40

/** Minutos de cada parada y de traslado entre una y otra al armar a mano; la API recalcula los traslados. */
internal const val MANUAL_STOP_MINUTES = 60
private const val MANUAL_GAP_MINUTES = 15L
private const val MIN_SEARCH = 2

/** Personas según «¿Quién va?»: la API pide `personas` y CrearPlan no tiene contador (D-24d). */
internal fun Company?.people(): Int = when (this) {
    Company.SOLO -> PEOPLE_SOLO
    Company.COUPLE, null -> PEOPLE_COUPLE
    Company.FRIENDS -> PEOPLE_FRIENDS
    Company.FAMILY -> PEOPLE_FAMILY
}

private const val PEOPLE_SOLO = 1
private const val PEOPLE_COUPLE = 2
private const val PEOPLE_FRIENDS = 3
private const val PEOPLE_FAMILY = 4

/** Lugar elegido para una parada, con lo necesario para pintarla. */
data class ManualPlace(val id: String, val name: String, val category: String, val photoUrl: String?, val price: Int)

data class PlaceSearch(
    val query: String = "",
    val results: List<PlaceCard> = emptyList(),
    val searching: Boolean = false
)

data class ManualPlanUiState(
    val name: String = "",
    val company: Company? = null,
    val date: LocalDate? = null,
    val time: LocalTime = DEFAULT_START,
    val price: PriceLevel? = null,
    val mobility: Mobility = Mobility.WALK,
    val stops: List<ManualPlace> = emptyList(),
    val reordering: Boolean = false,
    val search: PlaceSearch? = null,
    val isPublic: Boolean = false,
    val invalidDateTime: Boolean = false,
    val saving: Boolean = false,
    val error: UiText? = null
) {
    val canAddStop: Boolean get() = stops.size < AI_MAX_STOPS

    /** Nombre de 1 a 40, paradas de 2 a 4 y fecha en el futuro (spec nv-24 §2). */
    val canCreate: Boolean
        get() = !saving &&
            name.isNotBlank() &&
            name.length <= PLAN_NAME_MAX &&
            stops.size in AI_MIN_STOPS..AI_MAX_STOPS &&
            !invalidDateTime &&
            date != null
}

/** CrearPlan (specs/nv-24): plan a mano con `POST /planes`. */
@Suppress("TooManyFunctions") // Un evento por control de CrearPlan y de su buscador de lugares.
@HiltViewModel
class ManualPlanViewModel @Inject constructor(
    private val plans: PlansRepository,
    private val places: PlacesRepository,
    private val profiles: ProfileRepository,
    private val clock: Clock
) : ViewModel() {
    private val mutableState = MutableStateFlow(ManualPlanUiState(date = LocalDate.now(clock).plusDays(1)))
    val state: StateFlow<ManualPlanUiState> = mutableState.asStateFlow()

    private val createdChannel = Channel<String>(Channel.BUFFERED)

    /** Id del plan creado: la pantalla abre 39_Plan. */
    val created = createdChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val profile = profiles.observeProfile().first()
            mutableState.update { it.copy(mobility = Mobility.from(profile?.preferences?.mobility)) }
        }
    }

    fun today(): LocalDate = LocalDate.now(clock)

    fun onName(value: String) = mutableState.update { it.copy(name = value.take(PLAN_NAME_MAX), error = null) }

    fun onCompany(company: Company) = mutableState.update { it.copy(company = company.takeIf { c -> c != it.company }) }

    fun onDate(date: LocalDate) = updateDateTime { it.copy(date = date) }

    fun onTime(time: LocalTime) = updateDateTime { it.copy(time = time) }

    fun onPrice(price: PriceLevel) = mutableState.update { it.copy(price = price.takeIf { p -> p != it.price }) }

    fun onPublic(value: Boolean) = mutableState.update { it.copy(isPublic = value) }

    fun toggleReorder() = mutableState.update { it.copy(reordering = !it.reordering) }

    fun moveUp(index: Int) = move(index, index - 1)

    fun moveDown(index: Int) = move(index, index + 1)

    private fun move(from: Int, to: Int) = mutableState.update {
        if (from !in it.stops.indices || to !in it.stops.indices) return@update it
        val list = it.stops.toMutableList()
        list.add(to, list.removeAt(from))
        it.copy(stops = list)
    }

    fun removeStop(index: Int) = mutableState.update {
        if (index !in it.stops.indices) it else it.copy(stops = it.stops.filterIndexed { i, _ -> i != index })
    }

    fun openSearch() = mutableState.update { if (it.canAddStop) it.copy(search = PlaceSearch()) else it }

    fun closeSearch() = mutableState.update { it.copy(search = null) }

    fun onSearch(query: String) {
        mutableState.update { it.copy(search = it.search?.copy(query = query)) }
        if (query.trim().length < MIN_SEARCH) return
        mutableState.update { it.copy(search = it.search?.copy(searching = true)) }
        viewModelScope.launch {
            val found = (places.search(query) as? Result.Success)?.data.orEmpty()
            mutableState.update {
                val search = it.search ?: return@update it
                if (search.query == query) it.copy(search = search.copy(results = found, searching = false)) else it
            }
        }
    }

    /** Un lugar se agrega una sola vez y hasta 4 paradas. */
    fun addPlace(place: PlaceCard) = mutableState.update {
        if (!it.canAddStop || it.stops.any { s -> s.id == place.id }) {
            it.copy(search = null)
        } else {
            it.copy(
                stops = it.stops + ManualPlace(
                    place.id,
                    place.name,
                    place.category,
                    place.coverUrl,
                    place.averagePrice
                ),
                search = null
            )
        }
    }

    private fun updateDateTime(change: (ManualPlanUiState) -> ManualPlanUiState) = mutableState.update {
        val next = change(it)
        val start = next.date?.atTime(next.time)?.atZone(clock.zone)
        next.copy(invalidDateTime = start == null || !start.isAfter(ZonedDateTime.now(clock)))
    }

    /** Hora de llegada (ISO UTC) de cada parada: una tras otra desde la hora de inicio. */
    internal fun arrivals(state: ManualPlanUiState): List<String> {
        val date = state.date ?: return emptyList()
        val start = date.atTime(state.time).atZone(clock.zone)
        return state.stops.indices.map { index ->
            start.plusMinutes(index * (MANUAL_STOP_MINUTES + MANUAL_GAP_MINUTES)).toInstant().toString()
        }
    }

    fun create() {
        val current = state.value
        if (!current.canCreate) return
        val date = current.date ?: return
        val times = arrivals(current)
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val plan = ManualPlan(
                name = current.name.trim(),
                date = date.toString(),
                people = current.company.people(),
                budget = current.price?.maxPerPerson ?: 0,
                mobility = current.mobility,
                stops = current.stops.mapIndexed { index, place ->
                    ManualStop(
                        place.id,
                        place.name,
                        place.category,
                        place.photoUrl,
                        times[index],
                        MANUAL_STOP_MINUTES,
                        place.price
                    )
                },
                isPublic = current.isPublic
            )
            when (val result = plans.create(plan)) {
                is Result.Success -> {
                    mutableState.update { it.copy(saving = false) }
                    createdChannel.send(result.data.id)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(
                        saving = false,
                        error = result.error.stopErrorText(
                            current.stops.map { p ->
                                p.id to p.name
                            }
                        )
                    )
                }
            }
        }
    }
}
