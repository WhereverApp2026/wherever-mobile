package com.wherever.mobile.core.common

/** Campo rechazado por la API (`campos[]`). */
data class FieldError(val field: String, val message: String)

/** Errores uniformes de la app (constitución §4, specs/nv-04 §3.3). */
sealed class AppError {
    open val apiMessage: String? = null
    open val traceId: String? = null

    /** Sin red de verdad: DNS sin respuesta, conexión rechazada o cortada (specs/nv-40). */
    data object Network : AppError()
    data object Timeout : AppError()

    /** Falla TLS o el tráfico en claro: hay red, pero la conexión no es segura (specs/nv-40). */
    data object SecureConnection : AppError()

    /** El servidor respondió 2xx con un cuerpo que no es el del contrato (specs/nv-40). */
    data object InvalidResponse : AppError()

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
