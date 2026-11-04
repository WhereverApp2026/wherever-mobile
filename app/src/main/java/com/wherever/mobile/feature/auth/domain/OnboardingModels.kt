package com.wherever.mobile.feature.auth.domain

enum class OnboardingStep {
    REGISTER,
    PREFERENCES,
    LOCATION,
    FRIENDS,
    PERMISSIONS,
    COMPLETE;

    companion object {
        fun from(value: String?): OnboardingStep = entries.firstOrNull { it.name == value } ?: REGISTER
    }
}

data class OnboardingPreferences(
    val interests: List<String> = emptyList(),
    val vibe: String = "tranquilo",
    val budget: String = "medio",
    val company: String = "amigos"
)

data class OnboardingSnapshot(
    val step: OnboardingStep = OnboardingStep.REGISTER,
    val preferences: OnboardingPreferences = OnboardingPreferences()
)

data class FriendSuggestion(val id: String, val name: String, val detail: String? = null, val mutualFriends: Int? = null)
