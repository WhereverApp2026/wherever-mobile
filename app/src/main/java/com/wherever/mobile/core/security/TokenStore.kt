package com.wherever.mobile.core.security

interface TokenStore {
    fun accessToken(): String?
    fun refreshToken(): String?
    fun save(tokens: Tokens)
    fun clear()
    fun reset()
}
