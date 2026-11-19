package com.wherever.mobile.feature.auth.domain

import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * El enlace de correo abierto, solo en memoria. El token no viaja en la ruta de navegación porque
 * las rutas se guardan en el estado de la pila (`SourceSecurityTest`). Si el proceso muere, se
 * vuelve a abrir el enlace.
 */
@Singleton
class PendingEmailLink @Inject constructor() {
    private val current = AtomicReference<EmailLink?>(null)

    fun put(link: EmailLink) = current.set(link)

    fun resetToken(): String? = (current.get() as? EmailLink.ResetPassword)?.token

    fun verifyToken(): String? = (current.get() as? EmailLink.VerifyEmail)?.token

    /** Tras usarlo: el enlace es de un solo uso. */
    fun clear() = current.set(null)
}
