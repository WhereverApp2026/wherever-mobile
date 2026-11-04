package com.wherever.mobile.core.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val ONE_SECOND_MILLIS = 1_000L

/** Emite los segundos restantes hasta llegar a 0. */
fun countdown(seconds: Long): Flow<Long> = flow {
    for (remaining in seconds downTo 1) {
        emit(remaining)
        delay(ONE_SECOND_MILLIS)
    }
    emit(0)
}
