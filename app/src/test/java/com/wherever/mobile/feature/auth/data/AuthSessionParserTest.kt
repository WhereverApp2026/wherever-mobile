package com.wherever.mobile.feature.auth.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSessionParserTest {

    private val json = Json

    @Test
    fun parsesFlatTokens() {
        val tokens = AuthSessionParser.parseTokens(
            json.parseToJsonElement("""{"access_token":"a","refresh_token":"r"}""")
        )

        assertEquals(AuthTokens("a", "r"), tokens)
    }

    @Test
    fun parsesNestedTokens() {
        val tokens = AuthSessionParser.parseTokens(
            json.parseToJsonElement("""{"data":{"auth":{"accessToken":"a2","refreshToken":"r2"}}}""")
        )

        assertEquals(AuthTokens("a2", "r2"), tokens)
    }

    @Test
    fun ignoresIncompleteTokens() {
        assertNull(
            AuthSessionParser.parseTokens(
                json.parseToJsonElement("""{"access_token":"a"}""")
            )
        )
    }

    @Test
    fun parsesSessionsWithSpanishKeys() {
        val sessions = AuthSessionParser.parseSessions(
            json.parseToJsonElement(
                """
                {"datos":{"sesiones":[
                  {"id":"1","dispositivo":"Pixel","creado_en":"ayer","actual":true},
                  {"id":"2","device":{"name":"Tablet"},"ip_address":"192.0.2.2"}
                ]}}
                """.trimIndent()
            )
        )

        assertEquals(2, sessions.size)
        assertEquals("Pixel", sessions[0].deviceName)
        assertTrue(sessions[0].isCurrent)
        assertEquals("Tablet", sessions[1].deviceName)
        assertEquals("192.0.2.2", sessions[1].ipAddress)
        assertFalse(sessions[1].isCurrent)
    }

    @Test
    fun returnsEmptyListWhenNoSessionsExist() {
        assertTrue(
            AuthSessionParser.parseSessions(
                json.parseToJsonElement("""{"data":{}}""")
            ).isEmpty()
        )
    }
}
