package com.wherever.mobile.core.network

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** Avisa a la raíz de la app que el servidor rechazó la sesión. El Authenticator no conoce la UI. */
@Singleton
class SessionEvents @Inject constructor() {
    private val mutable = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = mutable

    fun notifySessionExpired() {
        mutable.tryEmit(Unit)
    }
}
