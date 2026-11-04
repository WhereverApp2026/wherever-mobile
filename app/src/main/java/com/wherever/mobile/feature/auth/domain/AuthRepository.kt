package com.wherever.mobile.feature.auth.domain

import com.wherever.mobile.core.common.Result

data class Registration(
    val name: String,
    val birthDate: String,
    val email: String,
    val password: String,
    val acceptsTerms: Boolean
)

data class ActiveSession(
    val id: String,
    val model: String,
    val system: String,
    val lastUsed: String,
    val isCurrent: Boolean
)

/** A dónde ir después de abrir sesión. */
enum class PostAuthDestination { HOME, TASTES, DELETION_PENDING }

interface AuthRepository {
    suspend fun hasSession(): Boolean

    suspend fun login(email: String, password: String): Result<PostAuthDestination>

    suspend fun isEmailAvailable(email: String): Result<Boolean>

    suspend fun register(registration: Registration): Result<Unit>

    suspend fun recoverPassword(email: String): Result<Unit>

    suspend fun resendVerification(): Result<Unit>

    suspend fun sessions(): Result<List<ActiveSession>>

    suspend fun closeSession(id: String): Result<Unit>

    /** Borra la sesión local aunque la llamada falle. */
    suspend fun logout()

    /** Solo borra la sesión local si el servidor confirmó. */
    suspend fun logoutAll(): Result<Unit>

    suspend fun clearLocalSession()

    suspend fun skipTastes()
}
