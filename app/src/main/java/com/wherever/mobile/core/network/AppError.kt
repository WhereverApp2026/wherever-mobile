package com.wherever.mobile.core.network

sealed interface AppError {
    data object Network : AppError
    data object Timeout : AppError
    data object Unauthorized : AppError
    data class Validation(val fields: List<String> = emptyList()) : AppError
    data class RateLimited(val retryAfterSeconds: Long? = null) : AppError
    data object Server : AppError
    data class Unknown(val detail: String? = null) : AppError
}
