package com.wherever.mobile.core.common

/** Campo rechazado por la API (`campos[]`). */
data class FieldError(val field: String, val message: String)

/** Errores uniformes de la app (constitución §4, specs/000-arquitectura §3.3). */
sealed class AppError {
    open val apiMessage: String? = null
    open val traceId: String? = null

    data object Network : AppError()
    data object Timeout : AppError()

    data class Unauthorized(override val apiMessage: String? = null, override val traceId: String? = null) : AppError()
    data class Forbidden(override val apiMessage: String? = null, override val traceId: String? = null) : AppError()
    data class NotFound(override val apiMessage: String? = null, override val traceId: String? = null) : AppError()
    data class Conflict(override val apiMessage: String? = null, override val traceId: String? = null) : AppError()
    data class Gone(override val apiMessage: String? = null, override val traceId: String? = null) : AppError()
    data class Locked(override val apiMessage: String? = null, override val traceId: String? = null) : AppError()
    data class Server(override val apiMessage: String? = null, override val traceId: String? = null) : AppError()
    data class Unknown(override val apiMessage: String? = null, override val traceId: String? = null) : AppError()

    data class Validation(
        val fields: List<FieldError> = emptyList(),
        override val apiMessage: String? = null,
        override val traceId: String? = null
    ) : AppError()

    data class RateLimited(
        val retryAfterSeconds: Long? = null,
        override val apiMessage: String? = null,
        override val traceId: String? = null
    ) : AppError()
}
