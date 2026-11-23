package com.wherever.mobile.feature.profile.domain

import com.wherever.mobile.core.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Lo de 81_Perfil que no se guarda en Room (bio, número de nivel y contadores de nv-81): se lee con
 * `GET /usuarios/yo` y vive en memoria. Así no se cambia el esquema que `nv-56` usará en la versión 6.
 */
data class ProfileExtras(
    val description: String?,
    val levelNumber: Int?,
    val plans: Int?,
    val friends: Int?,
    val reviews: Int?
)

interface ProfileRepository {
    /** Room es la fuente de verdad (constitución §2.1). */
    fun observeProfile(): Flow<UserProfile?>

    suspend fun refresh(): Result<UserProfile>

    /** `null` hasta la primera lectura con red. */
    fun observeExtras(): Flow<ProfileExtras?> = flowOf(null)

    /** `PATCH /usuarios/yo` con `If-Match`. Un 409 llega como `AppError.Conflict`. */
    suspend fun updateProfile(name: String? = null, cityId: String? = null): Result<UserProfile>

    /** nv-50: guarda la URL pública (`url_1080`) de una foto ya publicada. */
    suspend fun updatePhoto(url: String): Result<UserProfile>

    /** `PATCH /usuarios/yo {nombre_usuario}` (nv-53); repetido = 409. */
    suspend fun updateUsername(username: String): Result<UserProfile>

    /** nv-77: bio de una línea (≤ 160); vacía la borra. La moderación puede rechazarla (422). */
    suspend fun updateBio(bio: String): Result<UserProfile> =
        Result.Failure(com.wherever.mobile.core.common.AppError.Unknown())

    suspend fun updateTastes(tastes: Tastes): Result<Unit>

    suspend fun updatePreferences(patch: PreferencesPatch): Result<Unit>

    suspend fun updatePrivacy(patch: PrivacyPatch): Result<Unit>

    suspend fun searchCities(query: String): Result<List<CitySuggestion>>

    /** Devuelve `eliminar_en`. */
    suspend fun deleteAccount(confirmation: String): Result<String>

    suspend fun cancelDeletion(): Result<UserProfile>
}
