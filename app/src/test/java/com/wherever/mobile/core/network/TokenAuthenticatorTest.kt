package com.wherever.mobile.core.network

import com.wherever.mobile.core.security.TokenStore
import com.wherever.mobile.core.security.Tokens
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenAuthenticatorTest {

    @Test
    fun skipsRequestWithoutAuthorizationHeader() {
        val store = FakeTokenStore(refresh = "refresh-1")
        val refresher = FakeTokenRefresher()

        val result = TokenAuthenticator(store, refresher)
            .authenticate(route = null, response = response(authorization = null))

        assertNull(result)
        assertEquals(0, refresher.calls)
    }

    @Test
    fun retriesOnceWithRefreshedToken() {
        val store = FakeTokenStore(refresh = "refresh-1")
        val refresher = FakeTokenRefresher(
            result = RefreshResult.Refreshed(
                Tokens(accessToken = "access-2", refreshToken = "refresh-2")
            )
        )

        val result = TokenAuthenticator(store, refresher)
            .authenticate(route = null, response = response(authorization = "Bearer access-1"))

        assertNotNull(result)
        assertEquals("Bearer access-2", result?.header("Authorization"))
        assertEquals(Tokens(accessToken = "access-2", refreshToken = "refresh-2"), store.saved)
    }

    @Test
    fun clearsSessionWhenRefreshFails() {
        val store = FakeTokenStore(refresh = "refresh-1")
        val refresher = FakeTokenRefresher(result = RefreshResult.Rejected)

        val result = TokenAuthenticator(store, refresher)
            .authenticate(route = null, response = response(authorization = "Bearer access-1"))

        assertNull(result)
        assertTrue(store.cleared)
    }

    @Test
    fun keepsSessionWhenRefreshIsUnavailable() {
        val store = FakeTokenStore(refresh = "refresh-1")
        val refresher = FakeTokenRefresher(result = RefreshResult.Unavailable)

        val result = TokenAuthenticator(store, refresher)
            .authenticate(route = null, response = response(authorization = "Bearer access-1"))

        assertNull(result)
        assertEquals(false, store.cleared)
    }

    @Test
    fun doesNotRetryAfterPriorAuthenticatorAttempt() {
        val store = FakeTokenStore(refresh = "refresh-1")
        val refresher = FakeTokenRefresher(
            result = RefreshResult.Refreshed(
                Tokens(accessToken = "access-2", refreshToken = "refresh-2")
            )
        )
        val prior = response(authorization = "Bearer access-1")

        val result = TokenAuthenticator(store, refresher)
            .authenticate(
                route = null,
                response = response(authorization = "Bearer access-1", prior = prior)
            )

        assertNull(result)
        assertEquals(0, refresher.calls)
    }

    private fun response(authorization: String?, prior: Response? = null): Response {
        val request = Request.Builder()
            .url("https://example.com/me")
            .apply {
                if (authorization != null) {
                    header("Authorization", authorization)
                }
            }
            .build()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .apply {
                if (prior != null) priorResponse(prior)
            }
            .build()
    }

    private class FakeTokenStore(private val refresh: String?) : TokenStore {
        var saved: Tokens? = null
        var cleared: Boolean = false

        override fun accessToken(): String? = "access-1"
        override fun refreshToken(): String? = refresh
        override fun save(tokens: Tokens) {
            saved = tokens
        }

        override fun clear() {
            cleared = true
        }

        override fun reset() {
            cleared = true
        }
    }

    private class FakeTokenRefresher(private val result: RefreshResult = RefreshResult.Unavailable) : TokenRefresher {
        var calls: Int = 0

        override fun refresh(refreshToken: String): RefreshResult {
            calls += 1
            return result
        }
    }
}
