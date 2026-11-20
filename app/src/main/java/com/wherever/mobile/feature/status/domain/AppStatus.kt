package com.wherever.mobile.feature.status.domain

import com.wherever.mobile.core.common.Result

/** `GET /configuracion`: responde siempre, también en mantenimiento. */
data class AppStatus(val minimumVersion: String, val maintenance: Boolean)

interface AppStatusRepository {
    suspend fun status(): Result<AppStatus>
}

/** Compara versiones "1.2.3" por partes numéricas; ignora sufijos como "-debug". */
fun isOlder(current: String, minimum: String): Boolean {
    val a = parts(current)
    val b = parts(minimum)
    for (i in 0 until maxOf(a.size, b.size)) {
        val diff = a.getOrElse(i) { 0 } - b.getOrElse(i) { 0 }
        if (diff != 0) return diff < 0
    }
    return false
}

private fun parts(version: String): List<Int> =
    version.substringBefore('-').split('.').map { it.trim().toIntOrNull() ?: 0 }
