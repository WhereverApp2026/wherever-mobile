package com.wherever.mobile.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.profile.domain.PreferencesPatch
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.profile.domain.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val profile: UserProfile? = null,
    val saving: Boolean = false,
    val loggingOut: Boolean = false,
    val error: UiText? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<Unit>(Channel.BUFFERED)
    val loggedOut = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            profileRepository.observeProfile().collect { profile ->
                mutableState.update { it.copy(profile = profile) }
            }
        }
    }

    fun setTheme(theme: String) = patchPreferences(PreferencesPatch(theme = theme))

    fun setGroups(enabled: Boolean) = patchPreferences(PreferencesPatch(groups = enabled))

    fun setCoupons(enabled: Boolean) = patchPreferences(PreferencesPatch(coupons = enabled))

    fun setReviews(enabled: Boolean) = patchPreferences(PreferencesPatch(reviews = enabled))

    fun setPromotions(enabled: Boolean) = patchPreferences(PreferencesPatch(promotions = enabled))

    fun setUseLocation(enabled: Boolean) = save { profileRepository.updatePrivacy(PrivacyPatch(location = enabled)) }

    private fun patchPreferences(patch: PreferencesPatch) = save { profileRepository.updatePreferences(patch) }

    /** El interruptor sale de Room: si falla, sigue mostrando el valor anterior. */
    private fun save(call: suspend () -> Result<Unit>) {
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val result = call()
            mutableState.update {
                it.copy(saving = false, error = (result as? Result.Failure)?.error?.toUiText())
            }
        }
    }

    fun logout() {
        mutableState.update { it.copy(loggingOut = true) }
        viewModelScope.launch {
            authRepository.logout()
            eventChannel.send(Unit)
        }
    }
}
