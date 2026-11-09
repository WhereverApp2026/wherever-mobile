package com.wherever.mobile.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.profile.domain.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class HomeViewModel @Inject constructor(repository: ProfileRepository) : ViewModel() {
    val profile: StateFlow<UserProfile?> = repository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)
}
