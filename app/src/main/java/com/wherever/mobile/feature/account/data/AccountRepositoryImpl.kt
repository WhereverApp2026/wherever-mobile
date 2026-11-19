package com.wherever.mobile.feature.account.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.account.domain.AccountRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val api: AccountApi,
    private val profileRepository: ProfileRepository
) : AccountRepository {
    override suspend fun changePassword(current: String, new: String): Result<Unit> = safeApiCall {
        api.changePassword(ChangePasswordDto(current, new))
    }

    override suspend fun changeEmail(newEmail: String, password: String): Result<Unit> = safeApiCall {
        api.changeEmail(ChangeEmailDto(newEmail.trim(), password))
    }.also { result ->
        // Room es la fuente de verdad: el aviso "Aún no verificas tu correo" sale del perfil.
        if (result is Result.Success) profileRepository.refresh()
    }

    override suspend fun requestDataExport(): Result<Unit> = safeApiCall { api.requestExport() }

    override suspend fun clearHistory(): Result<Unit> = safeApiCall { api.clearHistory() }
}
