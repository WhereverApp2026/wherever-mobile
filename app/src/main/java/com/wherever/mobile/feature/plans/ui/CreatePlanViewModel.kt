package com.wherever.mobile.feature.plans.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val PHRASE_MAX = 300
internal const val MAX_PEOPLE = 20
private const val HOURS_LATER = 2L
private val TONIGHT: LocalTime = LocalTime.of(20, 0)
private val TOMORROW_AFTERNOON: LocalTime = LocalTime.of(14, 0)

/** Cuándo sale la persona (US-050). */
enum class PlanWhen { NOW, LATER, TONIGHT, TOMORROW }

/** Ánimo con caritas (US-048). Solo se usa si la persona dio el consentimiento de ánimo (US-010). */
enum class Mood(val phrase: String) {
    HAPPY("feliz"),
    RELAXED("relajado"),
    ROMANTIC("romántico"),
    ADVENTUROUS("aventurero")
}

enum class CreatePhase { EDITING, GENERATING, RESULT }

data class CreatePlanUiState(
    val phrase: String = "",
    val people: Int = 2,
    val mobility: Mobility = Mobility.WALK,
    val budget: Int? = null,
    val mood: Mood? = null,
    val moodAllowed: Boolean = false,
    val whenOption: PlanWhen = PlanWhen.NOW,
    val phase: CreatePhase = CreatePhase.EDITING,
    val draft: PlanDraft? = null,
    val saving: Boolean = false,
    val noLocation: Boolean = false,
    val error: UiText? = null
) {
    val canGenerate: Boolean get() = phrase.isNotBlank() && phase == CreatePhase.EDITING
}

/** 15_Asistente y 15b_HojaRapida (US-035, US-039 a US-046, US-048 a US-050). */
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
    private val proposed = linkedSetOf<String>()

    init {
        viewModelScope.launch {
            val profile = profiles.observeProfile().first()
            mutableState.update {
                it.copy(
                    mobility = Mobility.from(profile?.preferences?.mobility),
                    moodAllowed = profile?.privacy?.mood == true
                )
            }
        }
    }

    fun onPhrase(value: String) = mutableState.update { it.copy(phrase = value.take(PHRASE_MAX), error = null) }

    /** Una sola palabra rápida reemplaza la frase (US-035). */
    fun onQuickWord(word: String) = mutableState.update { it.copy(phrase = word, error = null) }

    fun onPeople(people: Int) = mutableState.update { it.copy(people = people.coerceIn(1, MAX_PEOPLE)) }

    fun onMobility(mobility: Mobility) = mutableState.update { it.copy(mobility = mobility) }

    fun onBudget(budget: Int?) = mutableState.update { it.copy(budget = budget.takeIf { b -> b != it.budget }) }

    fun onMood(mood: Mood) = mutableState.update { it.copy(mood = mood.takeIf { m -> m != it.mood }) }

    fun onWhen(option: PlanWhen) = mutableState.update { it.copy(whenOption = option) }

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

    /** US-039: la persona no se queda esperando; puede cancelar y ajustar. */
    fun cancel() {
        job?.cancel()
        mutableState.update { it.copy(phase = if (it.draft != null) CreatePhase.RESULT else CreatePhase.EDITING) }
    }

    fun edit() = mutableState.update { it.copy(phase = CreatePhase.EDITING) }

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
        mutableState.update { it.copy(phase = CreatePhase.GENERATING, error = null, noLocation = false) }
        job = viewModelScope.launch {
            val near = location.current()
            if (near == null) {
                mutableState.update { it.copy(phase = CreatePhase.EDITING, noLocation = true) }
                return@launch
            }
            val current = state.value
            val request = PlanRequest(
                phrase = buildPhrase(current),
                near = near,
                dateTime = dateTimeFor(current.whenOption),
                people = current.people,
                mobility = current.mobility
            )
            when (val result = call(request)) {
                is Result.Success -> {
                    proposed += result.data.stops.map { it.placeId }
                    mutableState.update { it.copy(phase = CreatePhase.RESULT, draft = result.data) }
                }
                is Result.Failure -> mutableState.update {
                    it.copy(
                        phase = if (it.draft !=
                            null
                        ) {
                            CreatePhase.RESULT
                        } else {
                            CreatePhase.EDITING
                        },
                        error = result.error.toUiText()
                    )
                }
            }
        }
    }

    /**
     * La API no recibe presupuesto ni ánimo aparte: los entiende de la frase (backend nv-31).
     * Por eso la hoja rápida los agrega al final de lo que escribió la persona.
     */
    internal fun buildPhrase(state: CreatePlanUiState): String = buildList {
        add(state.phrase.trim())
        state.budget?.let { add("con presupuesto de hasta $$it por persona") }
        if (state.moodAllowed) state.mood?.let { add("me siento ${it.phrase}") }
    }.joinToString(", ").take(PHRASE_MAX)

    /** `null` = ahora; la API toma la hora actual. */
    internal fun dateTimeFor(option: PlanWhen): String? {
        val now = ZonedDateTime.now(clock)
        val target = when (option) {
            PlanWhen.NOW -> return null
            PlanWhen.LATER -> now.plusHours(HOURS_LATER)
            PlanWhen.TONIGHT -> now.with(TONIGHT).let { if (it.isBefore(now)) now.plusHours(HOURS_LATER) else it }
            PlanWhen.TOMORROW -> LocalDate.now(clock).plusDays(1).atTime(TOMORROW_AFTERNOON).atZone(clock.zone)
        }
        return target.withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    fun errorShown() = mutableState.update { it.copy(error = null) }
}
