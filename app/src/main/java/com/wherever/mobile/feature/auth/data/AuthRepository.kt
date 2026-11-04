package com.wherever.mobile.feature.auth.data

import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.database.OnboardingDao
import com.wherever.mobile.core.network.ApiErrorParser
import com.wherever.mobile.core.network.AppError
import com.wherever.mobile.core.network.AppErrorMapper
import com.wherever.mobile.core.security.TokenStore
import com.wherever.mobile.core.security.Tokens
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import retrofit2.Response

enum class RegistrationOutcome {
    SESSION_CREATED,
    VERIFICATION_REQUIRED
}

class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val errorParser: ApiErrorParser,
    private val tokenStore: TokenStore,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val onboardingDao: OnboardingDao,
    private val dispatchers: DispatcherProvider
) {

    fun hasSession(): Boolean = !tokenStore.accessToken().isNullOrBlank()

    suspend fun login(email: String, password: String): Result<Unit> = withContext(dispatchers.io) {
        execute { api.login(LoginRequestDto(email, password, deviceInfoProvider.device)) }
            .flatMap { body ->
                val tokens = AuthSessionParser.parseTokens(body)
                    ?: return@flatMap Result.Failure(AppError.Unknown("Respuesta de login sin tokens"))
                tokenStore.save(Tokens(tokens.accessToken, tokens.refreshToken))
                Result.Success(Unit)
            }
    }

    suspend fun register(name: String, birthDate: String, email: String, password: String): Result<RegistrationOutcome> =
        withContext(dispatchers.io) {
            execute {
                api.register(
                    RegisterRequestDto(
                        name = name,
                        birthDate = birthDate,
                        email = email,
                        password = password,
                        termsVersion = TERMS_VERSION
                    )
                )
            }.map { body ->
                val tokens = AuthSessionParser.parseTokens(body)
                if (tokens == null) {
                    RegistrationOutcome.VERIFICATION_REQUIRED
                } else {
                    tokenStore.save(Tokens(tokens.accessToken, tokens.refreshToken))
                    RegistrationOutcome.SESSION_CREATED
                }
            }
        }

    suspend fun requestRecovery(email: String): Result<Unit> = withContext(dispatchers.io) {
        executeNoContent { api.requestRecovery(RecoverRequestDto(email)) }
    }

    suspend fun verifyEmail(token: String): Result<Unit> = withContext(dispatchers.io) {
        executeNoContent { api.verifyEmail(VerifyEmailRequestDto(token)) }
    }

    suspend fun resetPassword(token: String, newPassword: String): Result<Unit> = withContext(dispatchers.io) {
        executeNoContent { api.resetPassword(ResetPasswordRequestDto(token, newPassword)) }
            .also { result ->
                if (result is Result.Success) {
                    tokenStore.clear()
                    onboardingDao.clear()
                }
            }
    }

    suspend fun sessions(): Result<List<ActiveSession>> = withContext(dispatchers.io) {
        execute { api.sessions() }.map(AuthSessionParser::parseSessions)
    }

    suspend fun logoutAll(): Result<Unit> = withContext(dispatchers.io) {
        val result = executeNoContent { api.logoutAll() }
        tokenStore.clear()
        onboardingDao.clear()
        result
    }

    suspend fun clearSession() {
        tokenStore.clear()
        onboardingDao.clear()
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun <T> execute(call: suspend () -> Response<T>): Result<T> = try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) {
                Result.Failure(AppError.Unknown("Respuesta vacía del servidor"))
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

    private fun <T> Result<T>.flatMap(transform: (T) -> Result<Unit>): Result<Unit> = when (this) {
        is Result.Success -> transform(value)
        is Result.Failure -> this
    }

    companion object {
        private const val TERMS_VERSION = "1.0"
    }
}

class RemoteTokenRefresher(private val api: RefreshApi) : com.wherever.mobile.core.network.TokenRefresher {

    override fun refresh(refreshToken: String): com.wherever.mobile.core.network.RefreshResult = try {
        val response = kotlinx.coroutines.runBlocking { api.refresh(RefreshRequestDto(refreshToken)) }
        if (!response.isSuccessful) {
            if (response.code() == 401 || response.code() == 403) {
                com.wherever.mobile.core.network.RefreshResult.Rejected
            } else {
                com.wherever.mobile.core.network.RefreshResult.Unavailable
            }
        } else {
            val body: JsonElement? = response.body()
            val tokens = body?.let(AuthSessionParser::parseTokens)
            if (tokens == null) {
                com.wherever.mobile.core.network.RefreshResult.Unavailable
            } else {
                com.wherever.mobile.core.network.RefreshResult.Refreshed(
                    Tokens(tokens.accessToken, tokens.refreshToken)
                )
            }
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Throwable) {
        com.wherever.mobile.core.network.RefreshResult.Unavailable
    }
}
