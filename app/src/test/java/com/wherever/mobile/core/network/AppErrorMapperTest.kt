package com.wherever.mobile.core.network

import java.io.IOException
import java.net.SocketTimeoutException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppErrorMapperTest {

    @Test
    fun mapsValidationErrorsWithFields() {
        val error = AppErrorMapper.fromHttp(
            code = 400,
            error = ApiErrorDto(
                fields = listOf(
                    ApiErrorFieldDto(field = "email"),
                    ApiErrorFieldDto(field = "password")
                )
            )
        )

        assertEquals(AppError.Validation(listOf("email", "password")), error)
    }

    @Test
    fun mapsValidationErrorsWithoutBody() {
        val error = AppErrorMapper.fromHttp(code = 422)

        assertEquals(AppError.Validation(emptyList()), error)
    }

    @Test
    fun mapsUnauthorized() {
        assertEquals(AppError.Unauthorized, AppErrorMapper.fromHttp(code = 401))
    }

    @Test
    fun mapsServerErrors() {
        assertEquals(AppError.Server, AppErrorMapper.fromHttp(code = 503))
    }

    @Test
    fun mapsRateLimitWithRetryAfter() {
        assertEquals(
            AppError.RateLimited(retryAfterSeconds = 45),
            AppErrorMapper.fromHttp(code = 429, retryAfterSeconds = 45)
        )
    }

    @Test
    fun mapsRequestTimeoutStatus() {
        assertEquals(AppError.Timeout, AppErrorMapper.fromHttp(code = 408))
    }

    @Test
    fun mapsSocketTimeoutException() {
        assertEquals(AppError.Timeout, AppErrorMapper.fromThrowable(SocketTimeoutException()))
    }

    @Test
    fun mapsIOExceptionToNetwork() {
        assertEquals(AppError.Network, AppErrorMapper.fromThrowable(IOException("offline")))
    }

    @Test
    fun mapsUnexpectedThrowableToUnknown() {
        val error = AppErrorMapper.fromThrowable(IllegalStateException("boom"))

        assertTrue(error is AppError.Unknown)
        assertEquals("boom", (error as AppError.Unknown).detail)
    }
}
