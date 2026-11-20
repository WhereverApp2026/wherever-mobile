package com.wherever.mobile.core.common

import kotlinx.coroutines.flow.Flow

/**
 * Avisa si hay red con salida a internet (specs/nv-16). Solo informa a la UI: nunca se usa para bloquear una
 * petición antes de intentarla (nv-40, `ConnectionSecurityTest`); el resultado de la llamada manda.
 */
interface ConnectivityMonitor {
    val online: Flow<Boolean>
}
