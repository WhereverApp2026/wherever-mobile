package com.wherever.mobile.core.network

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
import com.wherever.mobile.core.common.Result
import java.io.IOException
import java.io.InterruptedIOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private const val HTTP_CONFLICT = 409
private const val HTTP_GONE = 410
private const val HTTP_UNPROCESSABLE = 422
private const val HTTP_LOCKED = 423
private const val HTTP_TOO_MANY = 429
private const val HTTP_SERVER_MIN = 500

/** Único punto que convierte excepciones de red en [AppError]. */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    Result.Failure(e.toAppError())
} catch (_: InterruptedIOException) {
    Result.Failure(AppError.Timeout)
} catch (_: IOException) {
    Result.Failure(AppError.Network)
} catch (_: SerializationException) {
    Result.Failure(AppError.Unknown())
}

private fun HttpException.toAppError(): AppError {
    val response = response()
    val body = runCatching {
        response?.errorBody()?.string()?.let { ApiJson.decodeFromString<ApiErrorDto>(it) }
    }.getOrNull()
    val retryAfter = response?.headers()?.get("Retry-After")?.trim()?.toLongOrNull()
    return mapApiError(code(), body, retryAfter)
}

/** El `codigo` decide; el estado HTTP solo se usa si el cuerpo no se pudo leer. */
fun mapApiError(status: Int, body: ApiErrorDto?, retryAfterSeconds: Long?): AppError {
    val message = body?.message
    val trace = body?.traceId
    return when (body?.code ?: codeForStatus(status)) {
        "no_autenticado" -> AppError.Unauthorized(message, trace)
        "prohibido" -> AppError.Forbidden(message, trace)
        "no_encontrado" -> AppError.NotFound(message, trace)
        "conflicto" -> AppError.Conflict(message, trace)
        "vencido" -> AppError.Gone(message, trace)
        "validacion" -> AppError.Validation(
            fields = body?.fields.orEmpty().map { FieldError(it.field, it.message) },
            apiMessage = message,
            traceId = trace
        )
        "bloqueado" -> AppError.Locked(message, trace)
        "demasiadas_peticiones" -> AppError.RateLimited(retryAfterSeconds, message, trace)
        "no_disponible" -> AppError.Server(message, trace)
        else -> if (status >= HTTP_SERVER_MIN) AppError.Server(message, trace) else AppError.Unknown(message, trace)
    }
}

private fun codeForStatus(status: Int): String? = when (status) {
    HTTP_UNAUTHORIZED -> "no_autenticado"
    HTTP_FORBIDDEN -> "prohibido"
    HTTP_NOT_FOUND -> "no_encontrado"
    HTTP_CONFLICT -> "conflicto"
    HTTP_GONE -> "vencido"
    HTTP_UNPROCESSABLE -> "validacion"
    HTTP_LOCKED -> "bloqueado"
    HTTP_TOO_MANY -> "demasiadas_peticiones"
    else -> null
}
