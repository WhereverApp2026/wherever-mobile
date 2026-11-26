package com.wherever.mobile.security

import com.wherever.mobile.core.network.RedactedLoggingInterceptor
import com.wherever.mobile.feature.coupons.data.CouponRepositoryImpl
import com.wherever.mobile.feature.coupons.data.CouponsApi
import com.wherever.mobile.testing.testRetrofit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** nv-29 (seguridad): el código de canje es personal; no llega a los logs ni a la ruta de la petición. */
class CouponsSecurityTest {
    private val server = MockWebServer()
    private val logs = mutableListOf<String>()

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.close()

    @Test
    fun `el codigo de canje no aparece en los logs`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(200)
                .body("""{"codigo":"K7Q2MX","qr":"wherever://canje/K7Q2MX","expira_en":"2026-11-25T18:05:00Z"}""")
                .build()
        )
        val client = OkHttpClient.Builder().addInterceptor(RedactedLoggingInterceptor { logs += it }).build()
        val repository = CouponRepositoryImpl(testRetrofit(server.url("/"), client).create(CouponsApi::class.java))

        repository.redeem("w1")

        assertTrue("se registró la llamada", logs.any { "canjear" in it })
        logs.forEach { assertFalse(it, "K7Q2MX" in it) }
    }
}
