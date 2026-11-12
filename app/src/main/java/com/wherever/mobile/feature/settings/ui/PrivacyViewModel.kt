package com.wherever.mobile.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.profile.domain.Privacy
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrivacyUiState(
    val privacy: Privacy? = null,
    /** Hay alergias guardadas: retirar el permiso de datos sensibles las borra (nv-43). */
    val hasAllergies: Boolean = false,
    val confirmSensitiveOff: Boolean = false,
    val saving: Boolean = false,
    val error: UiText? = null
)

@HiltViewModel
class PrivacyViewModel @Inject constructor(private val repository: ProfileRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(PrivacyUiState())
    val state: StateFlow<PrivacyUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { profile ->
                mutableState.update {
                    it.copy(privacy = profile?.privacy, hasAllergies = !profile?.tastes?.allergies.isNullOrBlank())
                }
            }
        }
    }

    /** Apagar con alergias guardadas pide confirmación antes de enviar, porque el backend las borra. */
    fun onSensitiveDataChange(enabled: Boolean) {
        if (!enabled && state.value.hasAllergies) {
            mutableState.update { it.copy(confirmSensitiveOff = true) }
        } else {
            update(PrivacyPatch(sensitiveData = enabled))
        }
    }

    fun confirmSensitiveOff() {
        mutableState.update { it.copy(confirmSensitiveOff = false) }
        update(PrivacyPatch(sensitiveData = false))
    }

    fun dismissSensitiveOff() = mutableState.update { it.copy(confirmSensitiveOff = false) }

    fun update(patch: PrivacyPatch) {
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val result = repository.updatePrivacy(patch)
            mutableState.update {
                it.copy(saving = false, error = (result as? Result.Failure)?.error?.toUiText())
            }
        }
    }
}
