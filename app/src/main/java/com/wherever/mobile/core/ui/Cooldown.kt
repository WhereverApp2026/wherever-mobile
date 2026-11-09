package com.wherever.mobile.core.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Espera si la API no manda `Retry-After`. */
const val DEFAULT_RETRY_SECONDS = 60L

private const val SECONDS_PER_MINUTE = 60L

fun CoroutineScope.launchCountdown(seconds: Long, onTick: (Long) -> Unit): Job = launch {
    countdown(seconds).collect(onTick)
}

/** 0:45 */
fun formatCountdown(seconds: Long): String =
    "%d:%02d".format(seconds / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE)
