package com.wherever.mobile.feature.profile.domain

data class CityRef(val id: String, val name: String)

data class CitySuggestion(val id: String, val name: String, val state: String)

data class Tastes(
    val categories: List<String>,
    val budgetMin: Int,
    val budgetMax: Int,
    val restrictions: List<String>,
    val allergies: String
)

data class NotificationSettings(
    val reservations: Boolean,
    val coupons: Boolean,
    val groups: Boolean,
    val reviews: Boolean,
    val promotions: Boolean
)

data class Preferences(
    val mobility: String,
    val theme: String,
    val units: String,
    val quietFrom: String?,
    val quietTo: String?,
    val notifications: NotificationSettings
)

data class Privacy(
    val location: Boolean,
    val personalizedLearning: Boolean,
    val mood: Boolean,
    val usageData: Boolean,
    val searchableByPhone: Boolean
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean,
    val mode: String,
    val city: CityRef?,
    val tastes: Tastes?,
    val preferences: Preferences,
    val privacy: Privacy,
    val points: Int,
    val level: String,
    val weekStreak: Int,
    val deleteAt: String?,
    val version: Long
)

/** Cambios parciales: lo que queda en null no se envía. */
data class PreferencesPatch(
    val theme: String? = null,
    val mobility: String? = null,
    val coupons: Boolean? = null,
    val groups: Boolean? = null,
    val reviews: Boolean? = null,
    val promotions: Boolean? = null
)

data class PrivacyPatch(
    val location: Boolean? = null,
    val personalizedLearning: Boolean? = null,
    val mood: Boolean? = null,
    val usageData: Boolean? = null,
    val searchableByPhone: Boolean? = null
)
