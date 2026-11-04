package com.wherever.mobile.feature.auth.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

data class AuthTokens(val accessToken: String, val refreshToken: String)

data class ActiveSession(
    val id: String,
    val deviceName: String,
    val createdAt: String?,
    val lastUsedAt: String?,
    val isCurrent: Boolean,
    val ipAddress: String?
)

object AuthSessionParser {

    private val nestedKeys = listOf("data", "datos", "tokens", "sesion", "session", "auth", "autenticacion")
    private val sessionKeys = listOf("sesiones", "sessions", "data", "datos")

    fun parseTokens(element: JsonElement): AuthTokens? {
        val candidates = candidateObjects(element)
        for (candidate in candidates) {
            val access = candidate.firstString("access_token", "accessToken", "token", "access")
            val refresh = candidate.firstString("refresh_token", "refreshToken", "refresh")
            if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                return AuthTokens(access, refresh)
            }
        }
        return null
    }

    fun parseSessions(element: JsonElement): List<ActiveSession> {
        val array = findArray(element) ?: return emptyList()
        return array.mapNotNull { item ->
            val session = item as? JsonObject ?: return@mapNotNull null
            val id = session.firstString("id", "_id", "sesion_id", "session_id") ?: return@mapNotNull null
            val device = session.deviceName()
            ActiveSession(
                id = id,
                deviceName = device,
                createdAt = session.firstString("creado_en", "created_at", "createdAt"),
                lastUsedAt = session.firstString("ultimo_uso", "last_used_at", "lastUsedAt", "actualizado_en"),
                isCurrent = session.firstBoolean("actual", "current", "es_actual") == true,
                ipAddress = session.firstString("ip", "ip_address", "direccion_ip")
            )
        }
    }

    private fun candidateObjects(element: JsonElement): List<JsonObject> {
        val result = mutableListOf<JsonObject>()
        fun visit(current: JsonElement, depth: Int) {
            val obj = current as? JsonObject ?: return
            result += obj
            if (depth == 0) return
            nestedKeys.mapNotNull { obj[it] }.forEach { visit(it, depth - 1) }
        }
        visit(element, depth = 3)
        return result
    }

    private fun findArray(element: JsonElement): JsonArray? {
        (element as? JsonArray)?.let { return it }
        val obj = element as? JsonObject ?: return null
        return sessionKeys.firstNotNullOfOrNull { key ->
            when (val value = obj[key]) {
                is JsonArray -> value
                is JsonObject -> findArray(value)
                else -> null
            }
        }
    }

    private fun JsonObject.deviceName(): String {
        val device = this["dispositivo"] ?: this["device"]
        return when (device) {
            is JsonPrimitive -> device.contentOrNull.orEmpty()
            is JsonObject -> device.firstString("nombre", "name", "modelo", "model").orEmpty()
            else -> firstString("dispositivo_nombre", "device_name").orEmpty()
        }.ifBlank { firstString("nombre", "name").orEmpty() }
    }

    private fun JsonObject.firstString(vararg keys: String): String? = keys.firstNotNullOfOrNull { key ->
        (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.firstBoolean(vararg keys: String): Boolean? = keys.firstNotNullOfOrNull { key ->
        (this[key] as? JsonPrimitive)?.jsonPrimitive?.booleanOrNull
    }
}
