package com.wherever.mobile.core.network

import com.wherever.mobile.core.security.TokenStore
import com.wherever.mobile.core.security.Tokens
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthHeaderInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var tokenStore: FakeTokenStore

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        tokenStore = FakeTokenStore()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun addsBearerWhenTokenExists() {
        tokenStore.tokens = Tokens(accessToken = "access-1", refreshToken = "refresh-1")
        executeRequest()

        assertEquals("Bearer access-1", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun omitsHeaderWhenTokenIsMissing() {
        executeRequest()

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    private fun executeRequest() {
        server.enqueue(MockResponse().setResponseCode(200))
        OkHttpClient.Builder()
            .addInterceptor(AuthHeaderInterceptor(tokenStore))
            .build()
            .newCall(Request.Builder().url(server.url("/health")).build())
            .execute()
            .close()
    }

    private class FakeTokenStore : TokenStore {
        var tokens: Tokens? = null

        override fun accessToken(): String? = tokens?.accessToken
        override fun refreshToken(): String? = tokens?.refreshToken
        override fun save(tokens: Tokens) {
            this.tokens = tokens
        }

        override fun clear() {
            tokens = null
        }

        override fun reset() {
            tokens = null
        }
    }
}
