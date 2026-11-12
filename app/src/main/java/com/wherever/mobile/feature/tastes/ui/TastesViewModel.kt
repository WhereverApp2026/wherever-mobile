package com.wherever.mobile.feature.tastes.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.profile.domain.CitySuggestion
import com.wherever.mobile.feature.profile.domain.PreferencesPatch
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.profile.domain.Tastes
import com.wherever.mobile.feature.profile.domain.UserProfile
import com.wherever.mobile.feature.profile.ui.CitySearchController
import com.wherever.mobile.feature.profile.ui.CitySearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val TASTES_MIN = 3
const val TASTES_MAX = 10
const val ALLERGIES_MAX = 200
const val BUDGET_LIMIT = 1000
private const val BUDGET_MIN_DEFAULT = 150
private const val BUDGET_MAX_DEFAULT = 300

data class TastesUiState(
    val fromSettings: Boolean = false,
    val categories: Set<String> = emptySet(),
    val budgetMin: Int = BUDGET_MIN_DEFAULT,
    val budgetMax: Int = BUDGET_MAX_DEFAULT,
    val mobility: String? = null,
    val restrictions: Set<String> = emptySet(),
    val allergiesEnabled: Boolean = false,
    val allergies: String = "",
    /** Casilla de consentimiento expreso para guardar alergias (nv-43). */
    val allergiesConsent: Boolean = false,
    /** El perfil ya tiene `datos_sensibles = true`: no hace falta volver a enviarlo. */
    val consentSaved: Boolean = false,
    val consentError: UiText? = null,
    val city: CitySearchState = CitySearchState(),
    val saving: Boolean = false,
    val error: UiText? = null
) {
    val canSubmit: Boolean
        get() = categories.size in TASTES_MIN..TASTES_MAX && !saving

    /** Alergias que de verdad se van a enviar: vacías si el chip está apagado. */
    val allergiesToSave: String
        get() = if (allergiesEnabled) allergies.trim() else ""
}

@Suppress("TooManyFunctions") // Un evento de UI por control del formulario de Gustos.
@HiltViewModel
class TastesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        TastesUiState(fromSettings = savedStateHandle["fromSettings"] ?: false)
    )
    val state: StateFlow<TastesUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<Unit>(Channel.BUFFERED)
    val finished = eventChannel.receiveAsFlow()

    private val citySearch = CitySearchController(viewModelScope, profileRepository) { change ->
        mutableState.update { it.copy(city = change(it.city)) }
    }

    init {
        viewModelScope.launch {
            profileRepository.observeProfile().firstOrNull()?.let(::prefill)
        }
    }

    private fun prefill(profile: UserProfile) = mutableState.update { current ->
        val tastes = profile.tastes
        current.copy(
            categories = tastes?.categories?.toSet() ?: current.categories,
            budgetMin = tastes?.budgetMin ?: current.budgetMin,
            budgetMax = tastes?.budgetMax ?: current.budgetMax,
            restrictions = tastes?.restrictions?.toSet() ?: current.restrictions,
            allergies = tastes?.allergies ?: current.allergies,
            allergiesEnabled = !tastes?.allergies.isNullOrBlank(),
            allergiesConsent = profile.privacy.sensitiveData,
            consentSaved = profile.privacy.sensitiveData,
            mobility = profile.preferences.mobility.takeIf { tastes != null },
            city = profile.city?.let { CitySearchState(query = it.name, selectedId = it.id) } ?: current.city
        )
    }

    /** Sin máximo visible: al llegar a 10 ya no se agregan más. */
    fun toggleCategory(key: String) = mutableState.update {
        when {
            key in it.categories -> it.copy(categories = it.categories - key)
            it.categories.size >= TASTES_MAX -> it
            else -> it.copy(categories = it.categories + key)
        }
    }

    fun toggleRestriction(key: String) = mutableState.update {
        it.copy(restrictions = if (key in it.restrictions) it.restrictions - key else it.restrictions + key)
    }

    fun onBudgetChange(min: Int, max: Int) = mutableState.update { it.copy(budgetMin = min, budgetMax = max) }

    fun onMobilityChange(key: String) = mutableState.update { it.copy(mobility = key) }

    fun onAllergiesEnabled(enabled: Boolean) = mutableState.update { it.copy(allergiesEnabled = enabled) }

    fun onAllergiesChange(value: String) = mutableState.update { it.copy(allergies = value.take(ALLERGIES_MAX)) }

    fun onAllergiesConsentChange(checked: Boolean) =
        mutableState.update { it.copy(allergiesConsent = checked, consentError = null) }

    fun onCityQueryChange(query: String) = citySearch.onQueryChange(query)

    fun onCitySelect(city: CitySuggestion) = citySearch.onSelect(city)

    fun skip() {
        viewModelScope.launch {
            authRepository.skipTastes()
            eventChannel.send(Unit)
        }
    }

    fun submit() {
        val current = state.value
        if (!current.canSubmit) return
        if (current.allergiesToSave.isNotEmpty() && !current.allergiesConsent) {
            mutableState.update { it.copy(consentError = UiText.Res(R.string.tastes_allergies_consent_required)) }
            return
        }
        mutableState.update { it.copy(saving = true, error = null, consentError = null) }
        viewModelScope.launch {
            when (val failure = save(current)) {
                null -> {
                    mutableState.update { it.copy(saving = false) }
                    eventChannel.send(Unit)
                }
                else -> mutableState.update { it.copy(saving = false, error = failure.toUiText()) }
            }
        }
    }

    /**
     * Hasta cuatro rutas en orden. Son idempotentes: reintentar tras un fallo no duplica nada.
     * El permiso de datos sensibles va antes que los gustos, porque sin él el backend rechaza `alergias` (422).
     */
    private suspend fun save(state: TastesUiState): AppError? {
        val tastes = Tastes(
            categories = state.categories.toList(),
            budgetMin = state.budgetMin,
            budgetMax = state.budgetMax,
            restrictions = state.restrictions.toList(),
            allergies = state.allergiesToSave
        )
        val consentError = if (tastes.allergies.isNotEmpty() && !state.consentSaved) {
            (profileRepository.updatePrivacy(PrivacyPatch(sensitiveData = true)) as? Result.Failure)?.error
        } else {
            null
        }
        if (consentError == null && tastes.allergies.isNotEmpty()) mutableState.update { it.copy(consentSaved = true) }
        val tasteError = consentError ?: (profileRepository.updateTastes(tastes) as? Result.Failure)?.error
        val prefsError = tasteError ?: state.mobility?.let { mobility ->
            (profileRepository.updatePreferences(PreferencesPatch(mobility = mobility)) as? Result.Failure)?.error
        }
        val cityError = prefsError ?: state.city.selectedId?.let { cityId ->
            (profileRepository.updateProfile(cityId = cityId) as? Result.Failure)?.error
        }
        return cityError
    }
}
