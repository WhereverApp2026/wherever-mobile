package com.wherever.mobile.feature.profile.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.profile.domain.CitySuggestion
import com.wherever.mobile.feature.profile.domain.PreferencesPatch
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.ProfileExtras
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.profile.domain.Tastes
import com.wherever.mobile.feature.profile.domain.UserProfile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

private const val CITY_SUGGESTIONS = 8

@Suppress("TooManyFunctions") // Una función por endpoint de la API (docs/contexto_api_sprint1.md).
/** Único en la app: guarda en memoria los extras del perfil (bio y contadores). */
@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: UsersApi,
    // Room es la fuente de verdad del perfil (constitución §2.1).
    private val dao: UserProfileDao
) : ProfileRepository {
    private val extras = MutableStateFlow<ProfileExtras?>(null)

    override fun observeProfile(): Flow<UserProfile?> = dao.observe().map { it?.toDomain() }

    override fun observeExtras(): Flow<ProfileExtras?> = extras.asStateFlow()

    override suspend fun refresh(): Result<UserProfile> = store(
        safeApiCall {
            api.getMe().also { dto ->
                extras.value = ProfileExtras(
                    description = dto.description?.takeIf { it.isNotBlank() },
                    levelNumber = dto.levelNumber,
                    plans = dto.counters?.plans,
                    friends = dto.counters?.friends,
                    reviews = dto.counters?.reviews
                )
            }
        }
    )

    override suspend fun updateProfile(name: String?, cityId: String?): Result<UserProfile> {
        val version = dao.get()?.version ?: when (val fetched = refresh()) {
            is Result.Success -> fetched.data.version
            is Result.Failure -> return fetched
        }
        return store(safeApiCall { api.patchMe("\"$version\"", ProfilePatchDto(name, cityId)) })
    }

    override suspend fun updatePhoto(url: String): Result<UserProfile> = patch(ProfilePatchDto(photoUrl = url))

    override suspend fun updateUsername(username: String): Result<UserProfile> =
        patch(ProfilePatchDto(username = username.trim().removePrefix("@").lowercase()))

    override suspend fun updateBio(bio: String): Result<UserProfile> {
        val clean = bio.trim().replace(Regex("\\s+"), " ")
        val result = patch(ProfilePatchDto(description = clean))
        if (result is Result.Success) {
            val current = extras.value
            extras.value = current?.copy(description = clean.ifEmpty { null })
                ?: ProfileExtras(clean.ifEmpty { null }, null, null, null, null)
        }
        return result
    }

    /** `PATCH /usuarios/yo` con la versión guardada en Room (`If-Match`). */
    private suspend fun patch(body: ProfilePatchDto): Result<UserProfile> {
        val version = dao.get()?.version ?: when (val fetched = refresh()) {
            is Result.Success -> fetched.data.version
            is Result.Failure -> return fetched
        }
        return store(safeApiCall { api.patchMe("\"$version\"", body) })
    }

    override suspend fun updateTastes(tastes: Tastes): Result<Unit> = safeApiCall {
        api.putTastes(tastes.toDto())
    }.applied { saved -> copy(tastes = saved.toEntity()) }

    override suspend fun updatePreferences(patch: PreferencesPatch): Result<Unit> = safeApiCall {
        api.patchPreferences(patch.toDto())
    }.applied { saved -> copy(preferences = saved.toEntity()) }

    override suspend fun updatePrivacy(patch: PrivacyPatch): Result<Unit> = safeApiCall {
        api.patchPrivacy(patch.toDto())
    }.applied { saved -> copy(privacy = saved.toEntity()) }

    override suspend fun searchCities(query: String): Result<List<CitySuggestion>> = safeApiCall {
        api.autocompleteCities(query, CITY_SUGGESTIONS).map { it.toDomain() }
    }

    override suspend fun deleteAccount(confirmation: String): Result<String> = safeApiCall {
        api.deleteMe(DeleteAccountDto(confirmation)).deleteAt
    }

    override suspend fun cancelDeletion(): Result<UserProfile> = store(safeApiCall { api.cancelDeletion() })

    private suspend fun store(result: Result<UserProfileDto>): Result<UserProfile> = result.map { dto ->
        val entity = dto.toEntity()
        dao.replace(entity)
        entity.toDomain()
    }

    /**
     * Estas rutas no devuelven `version`, y pueden haberla cambiado: se actualiza Room con la respuesta y
     * luego se pide el perfil, para que el siguiente `If-Match` no use una versión vieja.
     */
    private suspend fun <T> Result<T>.applied(change: UserProfileEntity.(T) -> UserProfileEntity): Result<Unit> =
        map { saved ->
            dao.get()?.let { dao.upsert(it.change(saved)) }
            refresh()
        }
}
