package com.wherever.mobile.feature.profile.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.profile.domain.CitySuggestion
import com.wherever.mobile.feature.profile.domain.PreferencesPatch
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.profile.domain.Tastes
import com.wherever.mobile.feature.profile.domain.UserProfile
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val CITY_SUGGESTIONS = 8

class ProfileRepositoryImpl @Inject constructor(private val api: UsersApi, private val dao: UserProfileDao) :
    ProfileRepository {
    override fun observeProfile(): Flow<UserProfile?> = dao.observe().map { it?.toDomain() }

    override suspend fun refresh(): Result<UserProfile> = store(safeApiCall { api.getMe() })

    override suspend fun updateProfile(name: String?, cityId: String?): Result<UserProfile> {
        val version = dao.get()?.version ?: when (val fetched = refresh()) {
            is Result.Success -> fetched.data.version
            is Result.Failure -> return fetched
        }
        return store(safeApiCall { api.patchMe("\"$version\"", ProfilePatchDto(name, cityId)) })
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
