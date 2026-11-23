package com.wherever.mobile.security

import com.wherever.mobile.core.network.RedactedLoggingInterceptor
import com.wherever.mobile.feature.business.data.BusinessOnboardingApi
import com.wherever.mobile.feature.business.data.DepositAccountDto
import com.wherever.mobile.testing.testRetrofit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

private const val CLABE = "002010077777777771"
private const val RFC = "XAXX010101000"

/** nv-34 (seguridad): ningún log contiene RFC, CLABE ni nombres de archivo del expediente. */
class BusinessDossierSecurityTest {
    private val server = MockWebServer()
    private val logs = mutableListOf<String>()
    private lateinit var api: BusinessOnboardingApi

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().addInterceptor(RedactedLoggingInterceptor { logs += it }).build()
        api = testRetrofit(server.url("/"), client).create(BusinessOnboardingApi::class.java)
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `la CLABE y el titular no salen en el log`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body("""{"clabe_ultimos4":"7771","titular":"x"}""").build())

        api.depositAccount("n1", DepositAccountDto(CLABE, "Osmar Reséndiz $RFC"))

        server.takeRequest(5, TimeUnit.SECONDS)
        assertEquals(1, logs.size)
        logs.forEach { line ->
            assertFalse(line.contains(CLABE))
            assertFalse(line.contains(RFC))
            assertFalse(line.contains("Osmar"))
        }
    }

    @Test
    fun `el nombre del archivo del expediente no sale en el log`() = runTest {
        server.enqueue(MockResponse.Builder().code(201).body("{}").build())
        val part = MultipartBody.Part.createFormData(
            "archivo",
            "ine_juan_perez_$RFC.pdf",
            "%PDF".toByteArray().toRequestBody("application/pdf".toMediaType())
        )

        api.uploadDocument("n1", "ine_frente".toRequestBody("text/plain".toMediaType()), part)

        server.takeRequest(5, TimeUnit.SECONDS)
        logs.forEach { line ->
            assertFalse(line.contains("juan_perez"))
            assertFalse(line.contains(RFC))
            assertFalse(line.contains(".pdf"))
        }
    }
}
