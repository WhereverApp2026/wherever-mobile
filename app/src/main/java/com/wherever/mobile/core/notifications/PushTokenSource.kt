package com.wherever.mobile.core.notifications

/** Da el token de FCM del teléfono, o `null` si Firebase no está configurado o no respondió. */
fun interface PushTokenSource {
    suspend fun currentToken(): String?
}
