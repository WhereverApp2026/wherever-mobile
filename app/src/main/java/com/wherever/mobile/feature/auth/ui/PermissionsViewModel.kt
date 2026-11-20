package com.wherever.mobile.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.LocationPermissionPreferences
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PermissionsUiState(
    val saving: Boolean = false,
    val error: UiText? = null,
    /** Decisión de ubicación pendiente de guardar, para "Reintentar". */
    val pendingLocation: Boolean? = null,
    val done: Boolean = false
)

/**
 * 08_Permisos (specs/nv-11): el sistema decide los permisos; aquí solo se guarda lo que eligió la persona.
 * La ubicación se sincroniza con `PATCH /usuarios/yo/privacidad`; los avisos se marcan como decididos para que
 * Inicio no los vuelva a pedir.
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val prefs: LocationPermissionPreferences
) : ViewModel() {
    private val mutableState = MutableStateFlow(PermissionsUiState())
    val state: StateFlow<PermissionsUiState> = mutableState.asStateFlow()

    /** Resultado del diálogo del sistema (`06_PermisoSys`). */
    fun onSystemResult(location: Boolean, notifications: Boolean) {
        if (state.value.saving || state.value.done) return
        if (!location) prefs.recordDenied()
        if (!notifications) prefs.recordNotificationsDecided()
        save(location)
    }

    fun retry() {
        state.value.pendingLocation?.let(::save)
    }

    /** "Ahora no": nada se pide ni se envía; Inicio no insiste con la tarjeta ni con los avisos. */
    fun onNotNow() {
        if (state.value.saving || state.value.done) return
        prefs.dismiss()
        prefs.recordNotificationsDecided()
        mutableState.update { it.copy(done = true) }
    }

    /** Si guardar falla, la persona puede seguir: la privacidad se edita después en Ajustes. */
    fun continueWithoutSaving() = mutableState.update { it.copy(done = true, error = null) }

    private fun save(location: Boolean) {
        mutableState.update { it.copy(saving = true, error = null, pendingLocation = location) }
        viewModelScope.launch {
            when (val result = repository.updatePrivacy(PrivacyPatch(location = location))) {
                is Result.Success -> mutableState.update {
                    it.copy(saving = false, done = true, pendingLocation = null)
                }
                is Result.Failure -> mutableState.update { it.copy(saving = false, error = result.error.toUiText()) }
            }
        }
    }
}
