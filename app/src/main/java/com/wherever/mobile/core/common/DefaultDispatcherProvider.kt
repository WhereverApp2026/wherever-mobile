package com.wherever.mobile.core.common

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

/** Único sitio que nombra `Dispatchers.IO`; el resto recibe [DispatcherProvider] inyectado (constitución §5). */
class DefaultDispatcherProvider : DispatcherProvider {
    override val io: CoroutineContext = Dispatchers.IO
}
