package com.wherever.mobile.core.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RequestIdInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = OkHttpClient.Builder()
            .addInterceptor(RequestIdInterceptor())
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun addsRequestIdWhenMissing() {
        server.enqueue(MockResponse().setResponseCode(200))
        client.newCall(Request.Builder().url(server.url("/health")).build()).execute().close()

        val recorded = server.takeRequest()
        val requestId = recorded.getHeader(RequestIdInterceptor.REQUEST_ID_HEADER)
        assertTrue(!requestId.isNullOrBlank())
    }

    @Test
    fun preservesExistingRequestId() {
        server.enqueue(MockResponse().setResponseCode(200))
        client.newCall(
            Request.Builder()
                .url(server.url("/health"))
                .header(RequestIdInterceptor.REQUEST_ID_HEADER, "request-123")
                .build()
        ).execute().close()

        assertEquals("request-123", server.takeRequest().getHeader(RequestIdInterceptor.REQUEST_ID_HEADER))
    }
}
