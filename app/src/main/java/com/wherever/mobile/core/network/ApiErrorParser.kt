package com.wherever.mobile.core.network

import javax.inject.Inject
import kotlinx.serialization.json.Json

class ApiErrorParser @Inject constructor(private val json: Json) {

    fun parse(body: String?): ApiErrorDto? {
        if (body.isNullOrBlank()) return null
        return runCatching { json.decodeFromString<ApiErrorDto>(body) }.getOrNull()
    }
}
