package com.wherever.mobile.core.network

import com.wherever.mobile.core.security.Tokens

sealed interface RefreshResult {
    data class Refreshed(val tokens: Tokens) : RefreshResult
    data object Rejected : RefreshResult
    data object Unavailable : RefreshResult
}

interface TokenRefresher {
    fun refresh(refreshToken: String): RefreshResult
}
