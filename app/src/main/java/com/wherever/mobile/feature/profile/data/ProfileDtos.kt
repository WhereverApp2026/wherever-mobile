package com.wherever.mobile.feature.profile.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityRefDto(@SerialName("id") val id: String, @SerialName("nombre") val name: String)

@Serializable
data class CitySuggestionDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String,
    @SerialName("estado") val state: String = ""
)

@Serializable
data class TastesDto(
    @SerialName("categorias") val categories: List<String> = emptyList(),
    @SerialName("presupuesto_min") val budgetMin: Int = 0,
    @SerialName("presupuesto_max") val budgetMax: Int = 0,
    @SerialName("restricciones") val restrictions: List<String> = emptyList(),
    @SerialName("alergias") val allergies: String = ""
)

@Serializable
data class QuietHoursDto(@SerialName("desde") val from: String, @SerialName("hasta") val to: String)

@Serializable
data class NotificationsDto(
    @SerialName("reservas") val reservations: Boolean = true,
    @SerialName("cupones") val coupons: Boolean = true,
    @SerialName("grupos") val groups: Boolean = true,
    @SerialName("resenas") val reviews: Boolean = true,
    @SerialName("promociones") val promotions: Boolean = false
)

@Serializable
data class PreferencesDto(
    @SerialName("movilidad_default") val mobility: String = "a_pie",
    @SerialName("tema") val theme: String = "sistema",
    @SerialName("unidades") val units: String = "km",
    @SerialName("horario_silencioso") val quietHours: QuietHoursDto? = null,
    @SerialName("notificaciones") val notifications: NotificationsDto = NotificationsDto()
)

@Serializable
data class PrivacyDto(
    @SerialName("ubicacion") val location: Boolean = false,
    @SerialName("aprendizaje_personalizado") val personalizedLearning: Boolean = false,
    @SerialName("estado_animo") val mood: Boolean = false,
    @SerialName("datos_uso") val usageData: Boolean = false,
    @SerialName("buscable_por_telefono") val searchableByPhone: Boolean = false
)

@Serializable
data class UserProfileDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String,
    @SerialName("correo") val email: String,
    @SerialName("correo_verificado") val emailVerified: Boolean = false,
    @SerialName("modo_actual") val mode: String = "explorar",
    @SerialName("ciudad") val city: CityRefDto? = null,
    @SerialName("gustos") val tastes: TastesDto? = null,
    @SerialName("preferencias") val preferences: PreferencesDto = PreferencesDto(),
    @SerialName("privacidad") val privacy: PrivacyDto = PrivacyDto(),
    @SerialName("puntos") val points: Int = 0,
    @SerialName("nivel") val level: String = "",
    @SerialName("racha_semanas") val weekStreak: Int = 0,
    @SerialName("eliminar_en") val deleteAt: String? = null,
    @SerialName("version") val version: Long
)

@Serializable
data class ProfilePatchDto(
    @SerialName("nombre") val name: String? = null,
    @SerialName("ciudad_id") val cityId: String? = null
)

@Serializable
data class NotificationsPatchDto(
    @SerialName("cupones") val coupons: Boolean? = null,
    @SerialName("grupos") val groups: Boolean? = null,
    @SerialName("resenas") val reviews: Boolean? = null,
    @SerialName("promociones") val promotions: Boolean? = null
)

@Serializable
data class PreferencesPatchDto(
    @SerialName("tema") val theme: String? = null,
    @SerialName("movilidad_default") val mobility: String? = null,
    @SerialName("notificaciones") val notifications: NotificationsPatchDto? = null
)

@Serializable
data class PrivacyPatchDto(
    @SerialName("ubicacion") val location: Boolean? = null,
    @SerialName("aprendizaje_personalizado") val personalizedLearning: Boolean? = null,
    @SerialName("estado_animo") val mood: Boolean? = null,
    @SerialName("datos_uso") val usageData: Boolean? = null,
    @SerialName("buscable_por_telefono") val searchableByPhone: Boolean? = null
)

@Serializable
data class DeleteAccountDto(@SerialName("confirmacion") val confirmation: String)

@Serializable
data class DeleteAccountResponseDto(@SerialName("eliminar_en") val deleteAt: String)
