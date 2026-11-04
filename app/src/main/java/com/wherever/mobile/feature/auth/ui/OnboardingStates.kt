package com.wherever.mobile.feature.auth.ui

import com.wherever.mobile.core.network.AppError
import com.wherever.mobile.feature.auth.domain.FriendSuggestion
import com.wherever.mobile.feature.auth.domain.OnboardingStep

sealed interface OnboardingStartUiState {
    data object Loading : OnboardingStartUiState
    data object Ready : OnboardingStartUiState
}

data class PreferencesUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val interests: Set<String> = emptySet(),
    val vibe: String = "tranquilo",
    val budget: String = "medio",
    val company: String = "amigos",
    val interestsMissing: Boolean = false,
    val error: AppError? = null
)

data class LocationUiState(val isSaving: Boolean = false)

data class FriendsUiState(
    val isLoading: Boolean = true,
    val suggestions: List<FriendSuggestion> = emptyList(),
    val sentIds: Set<String> = emptySet(),
    val sendingIds: Set<String> = emptySet(),
    val error: AppError? = null,
    val isContinuing: Boolean = false
)

data class PermissionsUiState(val isCompleting: Boolean = false, val error: AppError? = null)

sealed interface OnboardingStartEvent {
    data class Navigate(val step: OnboardingStep) : OnboardingStartEvent
}

sealed interface PreferencesEvent {
    data object Continue : PreferencesEvent
}

sealed interface LocationEvent {
    data object Continue : LocationEvent
}

sealed interface FriendsEvent {
    data object Continue : FriendsEvent
}

sealed interface PermissionsEvent {
    data object Complete : PermissionsEvent
}
