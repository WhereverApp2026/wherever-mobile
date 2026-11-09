package com.wherever.mobile.core.common

import kotlin.coroutines.CoroutineContext

/** Se inyecta para no fijar dispatchers en la lógica (constitución §5). */
interface DispatcherProvider {
    val io: CoroutineContext
}
