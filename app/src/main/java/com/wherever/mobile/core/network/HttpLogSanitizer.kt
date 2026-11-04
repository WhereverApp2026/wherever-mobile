package com.wherever.mobile.core.network

import okhttp3.HttpUrl

object HttpLogSanitizer {

    private val bearerToken = Regex("(?i)bearer\\s+\\S+")
    private val email = Regex("[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}")

    fun sanitize(url: HttpUrl): String = url.newBuilder()
        .query(null)
        .fragment(null)
        .build()
        .toString()

    fun redact(value: String): String = value
        .replace(bearerToken, "Bearer [redacted]")
        .replace(email, "[redacted-email]")
}
