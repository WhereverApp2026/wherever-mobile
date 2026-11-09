package com.wherever.mobile.core.common

/** Resultado de una operación de datos. Nunca lanza errores esperados hacia la UI. */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>

    data class Failure(val error: AppError) : Result<Nothing>
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Failure -> this
}
