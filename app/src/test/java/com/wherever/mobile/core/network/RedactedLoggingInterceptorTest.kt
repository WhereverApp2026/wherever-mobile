package com.wherever.mobile.core.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RedactedLoggingInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var logger: FakeLogger

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        logger = FakeLogger()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun logsRequestIdWithoutQuerySecrets() {
        server.enqueue(MockResponse().setResponseCode(200))
        val request = Request.Builder()
            .url(server.url("/health?access_token=secret-token&email=person@example.com"))
            .header(RequestIdInterceptor.REQUEST_ID_HEADER, "request-abc")
            .build()
        OkHttpClient.Builder()
            .addInterceptor(RedactedLoggingInterceptor(logger))
            .build()
            .newCall(request)
            .execute()
            .close()

        val output = logger.messages.joinToString(" ")
        assertTrue(output.contains("requestId=request-abc"))
        assertFalse(output.contains("secret-token"))
        assertFalse(output.contains("person@example.com"))
    }

    @Test
    fun redactsBearerAndEmailFromLogLines() {
        val redacted = HttpLogSanitizer.redact(
            "Authorization: Bearer abc.def person@example.com"
        )

        assertFalse(redacted.contains("abc.def"))
        assertFalse(redacted.contains("person@example.com"))
        assertTrue(redacted.contains("[redacted]"))
        assertTrue(redacted.contains("[redacted-email]"))
    }

    private class FakeLogger : HttpLogger {
        val messages = mutableListOf<String>()

        override fun log(message: String) {
            messages += message
        }
    }
}
