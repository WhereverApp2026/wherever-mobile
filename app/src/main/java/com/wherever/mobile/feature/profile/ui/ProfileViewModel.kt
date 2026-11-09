package com.wherever.mobile.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.profile.domain.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(val profile: UserProfile? = null, val loading: Boolean = true, val error: UiText? = null)

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: ProfileRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { profile -> mutableState.update { it.copy(profile = profile) } }
        }
        refresh()
    }

    fun refresh() {
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = repository.refresh()
            mutableState.update {
                it.copy(loading = false, error = (result as? Result.Failure)?.error?.toUiText())
            }
        }
    }
}
