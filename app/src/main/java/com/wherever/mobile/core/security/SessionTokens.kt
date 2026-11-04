package com.wherever.mobile.core.security

import kotlinx.serialization.Serializable

@Serializable
data class SessionTokens(val accessToken: String, val refreshToken: String, val expiresAt: String)
