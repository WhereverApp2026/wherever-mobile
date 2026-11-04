package com.wherever.mobile.feature.auth.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.ApiErrorParser
import com.wherever.mobile.core.network.AppError
import com.wherever.mobile.core.network.AppErrorMapper
import com.wherever.mobile.feature.auth.domain.FriendSuggestion
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import retrofit2.Response

class OnboardingRemote @Inject constructor(private val api: OnboardingApi, private val errorParser: ApiErrorParser) {

    suspend fun me(): Result<JsonElement> = executeJson { api.me() }

    suspend fun savePreferences(request: PreferencesRequestDto): Result<Unit> = executeNoContent { api.savePreferences(request) }

    suspend fun complete(): Result<Unit> = executeNoContent { api.complete() }

    suspend fun suggestions(): Result<List<FriendSuggestion>> = executeJson { api.suggestions(SUGGESTIONS_LIMIT) }
        .map(OnboardingParser::suggestions)

    suspend fun sendFriendRequest(request: FriendRequestDto): Result<Unit> = executeNoContent { api.sendFriendRequest(request) }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun <T> executeJson(call: suspend () -> Response<T>): Result<T> = try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) {
                Result.Failure(AppError.Unknown("Respuesta vacia del servidor"))
            } else {
                Result.Success(body)
            }
        } else {
            Result.Failure(response.toError())
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Result.Failure(AppErrorMapper.fromThrowable(throwable))
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun executeNoContent(call: suspend () -> Response<ResponseBody>): Result<Unit> = try {
        val response = call()
        response.body()?.close()
        if (response.isSuccessful) {
            Result.Success(Unit)
        } else {
            Result.Failure(response.toError())
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Result.Failure(AppErrorMapper.fromThrowable(throwable))
    }

    private fun <T> Response<T>.toError(): AppError {
        val retryAfter = headers()["Retry-After"]?.toLongOrNull()
        return AppErrorMapper.fromHttp(
            code = code(),
            error = errorParser.parse(errorBody()?.string()),
            retryAfterSeconds = retryAfter
        )
    }

    companion object {
        private const val SUGGESTIONS_LIMIT = 20
    }
}
