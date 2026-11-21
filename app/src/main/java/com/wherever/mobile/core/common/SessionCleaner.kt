package com.wherever.mobile.core.common

/**
 * Datos personales guardados en el teléfono que se borran al cerrar sesión, al vencer la sesión o al
 * eliminar la cuenta (constitución §6). Cada feature con caché propia aporta uno (multibinding de Hilt).
 */
fun interface SessionCleaner {
    suspend fun clear()
}
