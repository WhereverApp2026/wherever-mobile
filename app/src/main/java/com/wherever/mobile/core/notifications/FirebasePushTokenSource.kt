package com.wherever.mobile.core.notifications

import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Lee el token de [FirebaseMessaging]. Sin `google-services.json` Firebase no se inicializa
 * y `getInstance()` lanza `IllegalStateException`: entonces no hay push y la app sigue igual.
 */
class FirebasePushTokenSource @Inject constructor() : PushTokenSource {
    override suspend fun currentToken(): String? {
        val messaging = try {
            FirebaseMessaging.getInstance()
        } catch (_: IllegalStateException) {
            return null
        }
        return suspendCancellableCoroutine { continuation ->
            messaging.token.addOnCompleteListener { task ->
                continuation.resume(if (task.isSuccessful) task.result?.takeIf { it.isNotBlank() } else null)
            }
        }
    }
}
