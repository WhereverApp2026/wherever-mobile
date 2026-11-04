package com.wherever.mobile.core.network

import java.io.IOException
import java.net.SocketTimeoutException

object AppErrorMapper {

    fun fromHttp(code: Int, error: ApiErrorDto? = null, retryAfterSeconds: Long? = null): AppError = when {
        code == 401 || code == 403 -> AppError.Unauthorized
        code == 408 -> AppError.Timeout
        code == 429 -> AppError.RateLimited(retryAfterSeconds)
        code in 400..499 -> AppError.Validation(error?.fields.orEmpty().mapNotNull { it.field })
        code in 500..599 -> AppError.Server
        else -> AppError.Unknown("HTTP $code")
    }

    fun fromThrowable(throwable: Throwable): AppError = when (throwable) {
        is SocketTimeoutException -> AppError.Timeout
        is IOException -> AppError.Network
        else -> AppError.Unknown(throwable.message)
    }
}
