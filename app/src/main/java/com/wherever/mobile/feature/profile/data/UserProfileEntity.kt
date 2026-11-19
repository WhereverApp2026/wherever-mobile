package com.wherever.mobile.feature.profile.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

data class CityEntity(val id: String, val name: String)

data class TastesEntity(
    val categories: List<String>,
    val budgetMin: Int,
    val budgetMax: Int,
    val restrictions: List<String>,
    val allergies: String
)

data class PreferencesEntity(
    val mobility: String,
    val theme: String,
    val units: String,
    val quietFrom: String?,
    val quietTo: String?,
    val notifReservations: Boolean,
    val notifCoupons: Boolean,
    val notifGroups: Boolean,
    val notifReviews: Boolean,
    val notifPromotions: Boolean
)

data class PrivacyEntity(
    val location: Boolean,
    val personalizedLearning: Boolean,
    val mood: Boolean,
    val usageData: Boolean,
    val searchableByPhone: Boolean,
    @ColumnInfo(defaultValue = "0") val sensitiveData: Boolean,
    /** nv-53: la API lo crea encendido (`buscable_por_usuario`). */
    @ColumnInfo(defaultValue = "1") val searchableByUser: Boolean = true
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean,
    /** nv-50: columna nueva en la versión 3 de Room, sin valor por defecto (null). */
    val photoUrl: String? = null,
    /** nv-53: columnas nuevas en la versión 4 de Room (null hasta leer el perfil). */
    val username: String? = null,
    val friendCode: String? = null,
    val mode: String,
    @Embedded(prefix = "city_") val city: CityEntity?,
    @Embedded(prefix = "tastes_") val tastes: TastesEntity?,
    @Embedded(prefix = "pref_") val preferences: PreferencesEntity,
    @Embedded(prefix = "priv_") val privacy: PrivacyEntity,
    val points: Int,
    val level: String,
    val weekStreak: Int,
    val deleteAt: String?,
    val version: Long
)
