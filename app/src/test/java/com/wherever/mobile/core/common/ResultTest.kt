package com.wherever.mobile.core.common

import com.wherever.mobile.core.network.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class ResultTest {

    @Test
    fun mapsSuccessfulValue() {
        val result: Result<Int> = Result.Success(2)

        assertEquals(Result.Success(4), result.map { it * 2 })
    }

    @Test
    fun keepsFailureWhenMapping() {
        val failure: Result<Int> = Result.Failure(AppError.Server)

        assertEquals(failure, failure.map { it * 2 })
    }

    @Test
    fun exposesValueOrError() {
        val success = Result.Success("value")
        val failure = Result.Failure(AppError.Timeout)

        assertEquals("value", success.getOrNull())
        assertNull(success.errorOrNull())
        assertNull(failure.getOrNull())
        assertSame(AppError.Timeout, failure.errorOrNull())
    }

    @Test
    fun foldsBothBranches() {
        val success = Result.Success(3).fold({ it + 1 }, { -1 })
        val failure = Result.Failure(AppError.Network).fold({ 0 }, { -1 })

        assertEquals(4, success)
        assertEquals(-1, failure)
    }
}
