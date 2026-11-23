package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BusinessHomeRepository
import com.wherever.mobile.feature.business.domain.BusinessOnboardingRepository
import com.wherever.mobile.feature.business.domain.BusinessState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Negocio que se puede elegir arriba del Panel. */
data class BusinessChoice(val id: String, val name: String, val coverUrl: String?)

data class BusinessHomeUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val businesses: List<BusinessChoice> = emptyList(),
    val selectedId: String? = null
) {
    val selected: BusinessChoice? get() = businesses.firstOrNull { it.id == selectedId }
}

/**
 * Entrada al modo negocio (specs/nv-35): `POST /usuarios/yo/modo` y el negocio activo de `GET /negocios/mios`.
 * Sin rol comerciante (403) o sin altas, manda al alta (nv-34).
 */
@HiltViewModel
class BusinessHomeViewModel @Inject constructor(
    private val home: BusinessHomeRepository,
    private val onboarding: BusinessOnboardingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BusinessHomeUiState())
    val state: StateFlow<BusinessHomeUiState> = _state.asStateFlow()

    private val _needsOnboarding = Channel<Unit>(Channel.BUFFERED)
    val needsOnboarding: Flow<Unit> = _needsOnboarding.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val mode = home.enterBusinessMode()) {
                is Result.Failure -> {
                    if (mode.error is AppError.Forbidden) {
                        _needsOnboarding.send(Unit)
                    } else {
                        _state.update { it.copy(loading = false, error = mode.error.toUiText()) }
                    }
                }
                is Result.Success -> loadBusinesses()
            }
        }
    }

    private suspend fun loadBusinesses() {
        when (val mine = onboarding.mine()) {
            is Result.Failure -> _state.update { it.copy(loading = false, error = mine.error.toUiText()) }
            is Result.Success -> {
                // Solo negocios enviados: un borrador o uno rechazado sigue en el alta (nv-34).
                val choices = mine.data
                    .filter { it.state == BusinessState.IN_REVIEW || it.state == BusinessState.PUBLISHED }
                    .map { BusinessChoice(it.id, it.name, it.photos.firstOrNull()?.url) }
                if (choices.isEmpty()) {
                    _needsOnboarding.send(Unit)
                } else {
                    _state.update { current ->
                        current.copy(
                            loading = false,
                            businesses = choices,
                            selectedId = current.selectedId?.takeIf { id -> choices.any { it.id == id } }
                                ?: choices.first().id
                        )
                    }
                }
            }
        }
    }

    fun select(id: String) {
        _state.update { it.copy(selectedId = id) }
    }
}
