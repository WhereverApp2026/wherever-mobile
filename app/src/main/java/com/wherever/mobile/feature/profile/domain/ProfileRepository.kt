package com.wherever.mobile.feature.profile.domain

import com.wherever.mobile.core.common.Result
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    /** Room es la fuente de verdad (constitución §2.1). */
    fun observeProfile(): Flow<UserProfile?>

    suspend fun refresh(): Result<UserProfile>

    /** `PATCH /usuarios/yo` con `If-Match`. Un 409 llega como `AppError.Conflict`. */
    suspend fun updateProfile(name: String? = null, cityId: String? = null): Result<UserProfile>

    suspend fun updateTastes(tastes: Tastes): Result<Unit>

    suspend fun updatePreferences(patch: PreferencesPatch): Result<Unit>

    suspend fun updatePrivacy(patch: PrivacyPatch): Result<Unit>

    suspend fun searchCities(query: String): Result<List<CitySuggestion>>

    /** Devuelve `eliminar_en`. */
    suspend fun deleteAccount(confirmation: String): Result<String>

    suspend fun cancelDeletion(): Result<UserProfile>
}
