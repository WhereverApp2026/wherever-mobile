package com.wherever.mobile.feature.status.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.status.domain.AppStatus
import com.wherever.mobile.feature.status.domain.AppStatusRepository
import javax.inject.Inject

class AppStatusRepositoryImpl @Inject constructor(private val api: StatusApi) : AppStatusRepository {
    override suspend fun status(): Result<AppStatus> = safeApiCall {
        api.configuration().let { AppStatus(it.minimumVersion, it.maintenance) }
    }
}
