package com.wherever.mobile.integration

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.media.CompressedImage
import com.wherever.mobile.core.media.ImageCompressor
import com.wherever.mobile.feature.photos.data.ImagesApi
import com.wherever.mobile.feature.photos.data.PhotoRepositoryImpl
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.testing.reply
import com.wherever.mobile.testing.testRetrofit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

/**
 * nv-50 (Data): subida de fotos (backend nv-45). MockWebServer hace de API y de S3: pedir el
 * formulario, mandar el multipart sin `Authorization` y confirmar.
 */
class PhotoUploadIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    private val server = MockWebServer()
    private var compressed: CompressedImage? = CompressedImage(byteArrayOf(1, 2, 3, 4))
    private lateinit var repository: PhotoRepositoryImpl

    private val compressor = object : ImageCompressor {
        override suspend fun compress(source: String): CompressedImage? = compressed
    }

    @Before
    fun setUp() {
        server.start()
        val dispatchers = object : DispatcherProvider {
            override val io: CoroutineContext = EmptyCoroutineContext
        }
        repository = PhotoRepositoryImpl(
            testRetrofit(server.url("/api/v1/")).create(ImagesApi::class.java),
            compressor,
            OkHttpClient(),
            dispatchers
        )
    }

    @After
    fun tearDown() = server.close()

    private fun form(maxBytes: Int = 5_242_880) =
        """{"id":"img1","url":"${server.url("/s3/")}","campos":{"key":"cuarentena/img1","policy":"p",
        "Content-Type":"image/jpeg"},"vence_en":"2026-11-18T23:10:15Z","max_bytes":$maxBytes}"""

    @Test
    fun `sube a S3 con los campos antes del archivo y confirma`() = runTest {
        server.reply(201, form())
        server.reply(204)
        server.reply(
            200,
            """{"id":"img1","estado":"publicada","url_1080":"https://img/i/img1/1080.jpg","url_320":"https://img/i/img1/320.jpg"}"""
        )

        val image = (repository.upload("content://media/1") as Result.Success).data

        val request = server.takeRequest()
        assertEquals("""{"tipo":"image/jpeg"}""", request.body?.utf8())
        val s3 = server.takeRequest()
        assertEquals("/s3/", s3.url.encodedPath)
        assertNull(s3.headers["Authorization"])
        val body = s3.body?.utf8().orEmpty()
        assertTrue(body.indexOf("name=\"key\"") < body.indexOf("name=\"file\""))
        assertTrue(body.contains("cuarentena/img1"))
        assertEquals("/api/v1/imagenes/img1/confirmar", server.takeRequest().url.encodedPath)
        assertEquals(ImageState.PUBLISHED, image.state)
        assertEquals("https://img/i/img1/1080.jpg", image.url)
    }

    @Test
    fun `foto en revision no trae url`() = runTest {
        server.reply(201, form())
        server.reply(204)
        server.reply(200, """{"id":"img1","estado":"en_revision"}""")

        val image = (repository.upload("content://media/1") as Result.Success).data

        assertEquals(ImageState.IN_REVIEW, image.state)
        assertNull(image.url)
    }

    @Test
    fun `una foto ilegible no llama a la API`() = runTest {
        compressed = null

        val result = repository.upload("content://media/roto") as Result.Failure

        assertTrue(result.error is AppError.Validation)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `mas pesada que max_bytes no se sube`() = runTest {
        server.reply(201, form(maxBytes = 2))

        val result = repository.upload("content://media/1") as Result.Failure

        assertTrue(result.error is AppError.Validation)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `S3 rechaza y la foto no se confirma`() = runTest {
        server.reply(201, form())
        server.reply(403, "<Error>AccessDenied</Error>")

        val result = repository.upload("content://media/1") as Result.Failure

        assertTrue(result.error is AppError.Server)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `sin bucket la API da 503`() = runTest {
        server.reply(503, """{"codigo":"no_disponible","mensaje":"no disponible","traza_id":"t"}""")

        val result = repository.upload("content://media/1") as Result.Failure

        assertTrue(result.error is AppError.Server)
    }
}
