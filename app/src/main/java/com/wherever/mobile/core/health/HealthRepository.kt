package com.wherever.mobile.core.health

import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.network.AppError
import com.wherever.mobile.core.network.AppErrorMapper
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

data class HealthCheck(val healthCode: Int?, val readyCode: Int?, val error: AppError?, val durationMillis: Long)

class HealthRepository @Inject constructor(
    private val api: HealthApi,
    private val dispatchers: DispatcherProvider,
    private val clock: Clock
) {

    @Suppress("TooGenericExceptionCaught")
    suspend fun check(): HealthCheck = withContext(dispatchers.io) {
        val startedAt = clock.millis()
        try {
            val health = api.health()
            val ready = api.ready()
            val error = when {
                !health.isSuccessful -> AppErrorMapper.fromHttp(health.code())
                !ready.isSuccessful -> AppErrorMapper.fromHttp(ready.code())
                else -> null
            }
            HealthCheck(
                healthCode = health.code(),
                readyCode = ready.code(),
                error = error,
                durationMillis = clock.millis() - startedAt
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            HealthCheck(
                healthCode = null,
                readyCode = null,
                error = AppErrorMapper.fromThrowable(throwable),
                durationMillis = clock.millis() - startedAt
            )
        }
    }
}
