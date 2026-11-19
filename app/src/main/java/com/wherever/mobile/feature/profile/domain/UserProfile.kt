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
    val searchableByPhone: Boolean,
    /** Permiso expreso para guardar alergias (dato de salud). Sin él, el backend no las guarda. */
    val sensitiveData: Boolean,
    /** Que me encuentren por nombre de usuario (US-236, nv-53). */
    val searchableByUser: Boolean = true
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean,
    val photoUrl: String? = null,
    /** nv-53: nombre de usuario (3 a 20 de `[a-z0-9_]`) y código de 8 para agregar amigos. */
    val username: String? = null,
    val friendCode: String? = null,
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
    val promotions: Boolean? = null,
    /** Horario silencioso "HH:MM"; desde = hasta lo apaga (backend `inQuietHours`). */
    val quietHours: QuietHours? = null
)

data class QuietHours(val from: String, val to: String) {
    val enabled: Boolean
        get() = from != to

    companion object {
        /** La API no acepta borrar el horario; "00:00" a "00:00" nunca silencia. */
        val OFF = QuietHours("00:00", "00:00")
    }
}

data class PrivacyPatch(
    val location: Boolean? = null,
    val personalizedLearning: Boolean? = null,
    val mood: Boolean? = null,
    val usageData: Boolean? = null,
    val searchableByPhone: Boolean? = null,
    val sensitiveData: Boolean? = null,
    val searchableByUser: Boolean? = null
)
