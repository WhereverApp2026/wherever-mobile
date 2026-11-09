package com.wherever.mobile.core.security

interface TokenStore {
    suspend fun current(): SessionTokens?

    suspend fun save(tokens: SessionTokens)

    suspend fun clear()

    /** Limpia el estado entre pruebas (constitución §2.3). */
    suspend fun reset() = clear()
}
