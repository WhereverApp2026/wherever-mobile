package com.wherever.mobile.feature.plans.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.LocationProvider
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.PlanAssistantRepository
import com.wherever.mobile.feature.plans.domain.PlanDraft
import com.wherever.mobile.feature.plans.domain.PlanRequest
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val PHRASE_MAX = 300
internal const val MAX_PEOPLE = 20
internal const val AI_MIN_STOPS = 2
internal const val AI_MAX_STOPS = 4
internal const val AI_DEFAULT_STOPS = 3

/** La API acepta fechas de ahora a 30 días (`POST /planes/generar`). */
internal const val MAX_DAYS_AHEAD = 30L

/** Pasos de `Generando 1/4` a `Generando 4/4`; el último es «¡Plan listo!». */
internal const val GENERATING_STEPS = 4

/** Tiempo de cada paso de la animación mientras responde la API (la IA tarda hasta 7 s). */
internal const val STEP_MILLIS = 1_800L

/** Hora de inicio si la persona elige el día pero no la hora (09:00, como en 17_AjustaPlan). */
private const val DEFAULT_START_HOUR = 9
internal val DEFAULT_START: LocalTime = LocalTime.of(DEFAULT_START_HOUR, 0)

private const val BUDGET_LOW = 200
private const val BUDGET_MEDIUM = 500
private const val BUDGET_HIGH = 1000

/** ¿Quién va? de 17_AjustaPlan. La API lo entiende de la frase (`parametros.compania`). */
enum class Company(val phrase: String) {
    SOLO("voy solo"),
    FRIENDS("con amigos"),
    COUPLE("en pareja"),
    FAMILY("en familia")
}

/** Presupuesto por persona ($, $$, $$$) en pesos; la API lo entiende de la frase (`presupuesto_max`). */
enum class PriceLevel(val maxPerPerson: Int) { LOW(BUDGET_LOW), MEDIUM(BUDGET_MEDIUM), HIGH(BUDGET_HIGH) }

/** Ánimo con caritas (US-048). Solo se usa si la persona dio el consentimiento de ánimo (US-010). */
enum class Mood(val phrase: String) {
    HAPPY("feliz"),
    RELAXED("relajado"),
    ROMANTIC("romántico"),
    ADVENTUROUS("aventurero")
}

/** IAPalabra → 17_AjustaPlan → Generando 1/4…4/4 → plan propuesto (Guardar u Otra idea). */
enum class CreatePhase { WORD, ADJUST, GENERATING, READY, RESULT }

data class CreatePlanUiState(
    val phrase: String = "",
    val company: Company? = null,
    /** Nulo = hoy y ahora (la API toma la hora actual). */
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val people: Int = 2,
    val stops: Int = AI_DEFAULT_STOPS,
    val price: PriceLevel? = null,
    val mobility: Mobility = Mobility.WALK,
    val mood: Mood? = null,
    val moodAllowed: Boolean = false,
    /** Claves de gustos que la API usa (solo con `aprendizaje_personalizado`). */
    val tastes: List<String> = emptyList(),
    val phase: CreatePhase = CreatePhase.WORD,
    /** De qué formulario se pidió: a él se regresa al cancelar o si falla. */
    val origin: CreatePhase = CreatePhase.WORD,
    /** Paso activo de la animación (0 a 3). */
    val step: Int = 0,
    val draft: PlanDraft? = null,
    val saving: Boolean = false,
    val noLocation: Boolean = false,
    /** La hora elegida ya pasó o está a más de 30 días. */
    val invalidDateTime: Boolean = false,
    val error: UiText? = null
) {
    val canGenerate: Boolean
        get() = phrase.isNotBlank() && !invalidDateTime && (phase == CreatePhase.WORD || phase == CreatePhase.ADJUST)
}

/** IAPalabra, 17_AjustaPlan y Generando 1/4 a 4/4 (specs/nv-23) sobre `POST /planes/generar` (nv-48). */
@Suppress("TooManyFunctions") // Un evento por control de IAPalabra y de 17_AjustaPlan.
@HiltViewModel
class CreatePlanViewModel @Inject constructor(
    private val assistant: PlanAssistantRepository,
    private val plans: PlansRepository,
    private val profiles: ProfileRepository,
    private val location: LocationProvider,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        CreatePlanUiState(phrase = savedStateHandle.get<String>("phrase").orEmpty().take(PHRASE_MAX))
    )
    val state: StateFlow<CreatePlanUiState> = mutableState.asStateFlow()

    private val savedChannel = Channel<String>(Channel.BUFFERED)

    /** Id del plan guardado: la pantalla abre su detalle. */
    val saved = savedChannel.receiveAsFlow()

    private var job: Job? = null
    private var ticker: Job? = null
    private val proposed = linkedSetOf<String>()

    init {
        viewModelScope.launch {
            val profile = profiles.observeProfile().first()
            val learning = profile?.privacy?.personalizedLearning == true
            mutableState.update {
                it.copy(
                    mobility = Mobility.from(profile?.preferences?.mobility),
                    moodAllowed = profile?.privacy?.mood == true,
                    tastes = if (learning) profile?.tastes?.categories.orEmpty() else emptyList()
                )
            }
        }
    }

    fun onPhrase(value: String) = mutableState.update { it.copy(phrase = value.take(PHRASE_MAX), error = null) }

    /** Una palabra de ejemplo reemplaza lo escrito (US-035). */
    fun onQuickWord(word: String) = mutableState.update { it.copy(phrase = word, error = null) }

    /** «Prefiero ajustar los detalles». */
    fun adjust() = mutableState.update { it.copy(phase = CreatePhase.ADJUST, error = null) }

    /** Regresar de 17_AjustaPlan a IAPalabra sin perder lo elegido. */
    fun backToWord() = mutableState.update { it.copy(phase = CreatePhase.WORD, error = null) }

    fun onCompany(company: Company) = mutableState.update { it.copy(company = company.takeIf { c -> c != it.company }) }

    fun onDate(date: LocalDate) = updateDateTime { it.copy(date = date) }

    fun onTime(time: LocalTime) = updateDateTime { it.copy(time = time) }

    fun onPeople(people: Int) = mutableState.update { it.copy(people = people.coerceIn(1, MAX_PEOPLE)) }

    fun onStops(stops: Int) = mutableState.update { it.copy(stops = stops.coerceIn(AI_MIN_STOPS, AI_MAX_STOPS)) }

    fun onPrice(price: PriceLevel) = mutableState.update { it.copy(price = price.takeIf { p -> p != it.price }) }

    fun onMobility(mobility: Mobility) = mutableState.update { it.copy(mobility = mobility) }

    fun onMood(mood: Mood) = mutableState.update { it.copy(mood = mood.takeIf { m -> m != it.mood }) }

    private fun updateDateTime(change: (CreatePlanUiState) -> CreatePlanUiState) = mutableState.update {
        val next = change(it)
        next.copy(invalidDateTime = !isValid(next.date, next.time))
    }

    /** Fecha y hora en el futuro y a 30 días como mucho (reloj inyectado). */
    private fun isValid(date: LocalDate?, time: LocalTime?): Boolean {
        val target = targetDateTime(date, time) ?: return true
        val now = ZonedDateTime.now(clock)
        return target.isAfter(now) && !target.isAfter(now.plusDays(MAX_DAYS_AHEAD))
    }

    fun today(): LocalDate = LocalDate.now(clock)

    fun generate() {
        if (!state.value.canGenerate) return
        proposed.clear()
        run { request -> assistant.generate(request) }
    }

    /** US-044: otra propuesta sin repetir lugares. */
    fun otherIdea() {
        if (state.value.phase != CreatePhase.RESULT || proposed.isEmpty()) return
        run { request -> assistant.otherIdea(request, proposed.toList()) }
    }

    /** Cancelar: regresa al formulario con lo escrito (o al plan que ya había). */
    fun cancel() {
        job?.cancel()
        ticker?.cancel()
        mutableState.update {
            it.copy(
                phase = if (it.draft != null &&
                    proposed.isNotEmpty()
                ) {
                    CreatePhase.RESULT
                } else {
                    it.origin
                }
            )
        }
    }

    /** «Ver mi plan» de Generando 4/4. */
    fun showPlan() = mutableState.update { if (it.draft != null) it.copy(phase = CreatePhase.RESULT) else it }

    /** «Ajustar la petición» desde el plan propuesto. */
    fun edit() = mutableState.update { it.copy(phase = CreatePhase.ADJUST, draft = null) }

    fun save() {
        val draft = state.value.draft ?: return
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val result = plans.save(draft)) {
                is Result.Success -> {
                    mutableState.update { it.copy(saving = false) }
                    savedChannel.send(result.data.id)
                }
                is Result.Failure -> mutableState.update { it.copy(saving = false, error = result.error.toUiText()) }
            }
        }
    }

    private fun run(call: suspend (PlanRequest) -> Result<PlanDraft>) {
        job?.cancel()
        val origin = state.value.phase.takeIf { it == CreatePhase.WORD || it == CreatePhase.ADJUST }
            ?: state.value.origin
        mutableState.update {
            it.copy(phase = CreatePhase.GENERATING, origin = origin, step = 0, error = null, noLocation = false)
        }
        startTicker()
        job = viewModelScope.launch {
            val near = location.current()
            if (near == null) {
                ticker?.cancel()
                mutableState.update { it.copy(phase = it.origin, noLocation = true) }
                return@launch
            }
            val current = state.value
            val request = PlanRequest(
                phrase = buildPhrase(current),
                near = near,
                dateTime = dateTimeFor(current.date, current.time),
                people = current.people,
                mobility = current.mobility
            )
            val result = call(request)
            ticker?.cancel()
            when (result) {
                is Result.Success -> {
                    proposed += result.data.stops.map { it.placeId }
                    mutableState.update {
                        it.copy(phase = CreatePhase.READY, step = GENERATING_STEPS - 1, draft = result.data)
                    }
                }
                is Result.Failure -> mutableState.update { failed(it, result.error) }
            }
        }
    }

    /** Los tres primeros pasos avanzan con el tiempo; el cuarto («¡Plan listo!») solo con la respuesta. */
    private fun startTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            repeat(GENERATING_STEPS - 2) {
                delay(STEP_MILLIS)
                mutableState.update { it.copy(step = (it.step + 1).coerceAtMost(GENERATING_STEPS - 2)) }
            }
        }
    }

    /** Sin red: aviso propio. Sin resultados (422): a 17_AjustaPlan. Otro error: al formulario con lo escrito. */
    private fun failed(state: CreatePlanUiState, error: AppError): CreatePlanUiState {
        val hadPlan = state.draft != null && proposed.isNotEmpty()
        return when (error) {
            AppError.Network -> state.copy(
                phase = if (hadPlan) CreatePhase.RESULT else state.origin,
                error = UiText.Res(R.string.plan_offline)
            )
            is AppError.Validation -> state.copy(
                phase = if (hadPlan) CreatePhase.RESULT else CreatePhase.ADJUST,
                error = UiText.Res(R.string.plan_no_results)
            )
            else -> state.copy(phase = if (hadPlan) CreatePhase.RESULT else state.origin, error = error.toUiText())
        }
    }

    /**
     * La API no recibe compañía, paradas, presupuesto ni ánimo aparte: los entiende de la frase (backend nv-31).
     * Por eso 17_AjustaPlan los agrega al final de lo que escribió la persona.
     */
    internal fun buildPhrase(state: CreatePlanUiState): String = buildList {
        add(state.phrase.trim())
        state.company?.let { add(it.phrase) }
        if (state.stops != AI_DEFAULT_STOPS) add("con ${state.stops} paradas")
        state.price?.let { add("con presupuesto de hasta $${it.maxPerPerson} por persona") }
        if (state.moodAllowed) state.mood?.let { add("me siento ${it.phrase}") }
    }.joinToString(", ").take(PHRASE_MAX)

    /** `null` = ahora; la API toma la hora actual. */
    internal fun dateTimeFor(date: LocalDate?, time: LocalTime?): String? =
        targetDateTime(date, time)?.withNano(0)?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    private fun targetDateTime(date: LocalDate?, time: LocalTime?): ZonedDateTime? {
        if (date == null && time == null) return null
        val day = date ?: LocalDate.now(clock)
        return day.atTime(time ?: DEFAULT_START).atZone(clock.zone)
    }

    fun errorShown() = mutableState.update { it.copy(error = null) }
}
