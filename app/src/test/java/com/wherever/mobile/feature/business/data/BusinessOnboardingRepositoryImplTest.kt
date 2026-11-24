package com.wherever.mobile.feature.business.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.network.ApiJson
import com.wherever.mobile.feature.business.domain.Address
import com.wherever.mobile.feature.business.domain.DOCUMENT_MAX_BYTES
import com.wherever.mobile.feature.business.domain.DocumentType
import com.wherever.mobile.testing.sampleBusinessDraft
import com.wherever.mobile.testing.testRetrofit
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private fun businessJson(step: Int = 1, version: Int = 1, refresh: Boolean = false) = """
    {"id":"n1","nombre":"Café Raíz","categoria":"cafeteria","descripcion":"Café","horarios":[],"fotos":[],
    "estado":"borrador","paso_alta":$step,"revision":{},"documentos":{"ine":false,"comprobante":false},
    "verificado":false,"version":$version,"creado_en":"x","requiere_refresh":$refresh}
""".trimIndent()

/** nv-34 (datos): MockWebServer real con el contrato del alta. */
class BusinessOnboardingRepositoryImplTest {
    private val server = MockWebServer()
    private val resolver = mockk<ContentResolver>()
    private val context = mockk<Context> { every { contentResolver } returns resolver }
    private val dispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val io: CoroutineContext = dispatcher
    }
    private lateinit var repository: BusinessOnboardingRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk()
        repository = BusinessOnboardingRepositoryImpl(
            testRetrofit(server.url("/")).create(BusinessOnboardingApi::class.java),
            context,
            dispatchers
        )
    }

    @After
    fun tearDown() = server.close()

    private fun respond(code: Int, body: String = "") =
        server.enqueue(MockResponse.Builder().code(code).body(body).build())

    private fun take() = server.takeRequest(5, TimeUnit.SECONDS)!!

    @Test
    fun `POST negocios con requiere_refresh lo avisa al dominio`() = runTest(dispatcher) {
        respond(201, businessJson(refresh = true))

        val draft = (repository.create(" Café Raíz ", "cafeteria", "Café") as Result.Success).data

        assertTrue(draft.requiresRefresh)
        assertEquals(1, draft.step)
        val body = ApiJson.decodeFromString(JsonObject.serializer(), take().body!!.utf8())
        assertEquals("Café Raíz", body["nombre"]!!.jsonPrimitive.content)
    }

    @Test
    fun `PATCH alta paso 2 manda If-Match, direccion y punto GeoJSON`() = runTest(dispatcher) {
        respond(200, businessJson(step = 2, version = 4))

        val updated = repository.saveLocation(
            sampleBusinessDraft(version = 3),
            Address(street = "Hidalgo 23", neighborhood = "Centro", city = "Tula", zip = "42800"),
            GeoPoint(20.05, -99.34)
        ) as Result.Success

        val request = take()
        assertEquals("/negocios/n1/alta/2", request.url.encodedPath)
        assertEquals("\"3\"", request.headers["If-Match"])
        val body = ApiJson.decodeFromString(JsonObject.serializer(), request.body!!.utf8())
        assertEquals("Point", body["ubicacion"]!!.jsonObject["type"]!!.jsonPrimitive.content)
        assertEquals(
            -99.34,
            body["ubicacion"]!!.jsonObject["coordinates"]!!.jsonArray[0].jsonPrimitive.content.toDouble(),
            0.0
        )
        assertEquals("42800", body["direccion"]!!.jsonObject["cp"]!!.jsonPrimitive.content)
        assertEquals(4L, updated.data.version)
    }

    @Test
    fun `consentimiento datos_negocio con las versiones vigentes de configuracion`() = runTest(dispatcher) {
        respond(200, """{"version_terminos":"2026-10","version_aviso_privacidad":"2026-11","mantenimiento":false}""")
        respond(201, """{"datos_negocio":true,"consentimiento_vigente":true}""")

        assertTrue(repository.acceptBusinessDataConsent() is Result.Success)

        assertEquals("/configuracion", take().url.encodedPath)
        val request = take()
        assertEquals("/usuarios/yo/consentimientos", request.url.encodedPath)
        val body = ApiJson.decodeFromString(JsonObject.serializer(), request.body!!.utf8())
        assertEquals("2026-10", body["version_terminos"]!!.jsonPrimitive.content)
        assertEquals("2026-11", body["version_aviso_privacidad"]!!.jsonPrimitive.content)
        assertEquals("true", body["datos_negocio"]!!.jsonPrimitive.content)
    }

    @Test
    fun `paso 4 sin cuerpo manda un objeto vacio`() = runTest(dispatcher) {
        respond(200, businessJson(step = 4))

        repository.confirmPhotos(sampleBusinessDraft(step = 3))

        assertEquals("{}", take().body!!.utf8())
    }

    @Test
    fun `multipart de documentos con tipo y archivo sin el nombre original`() = runTest(dispatcher) {
        every { resolver.getType(any()) } returns "application/pdf"
        every { resolver.openInputStream(any()) } returns ByteArrayInputStream("%PDF-1.4".toByteArray())
        respond(201, """{"tipo":"ine_frente","content_type":"application/pdf","tamano":8,"estado":"recibido"}""")

        assertTrue(
            repository.uploadDocument(
                sampleBusinessDraft(),
                DocumentType.INE_FRONT,
                "content://mi-ine-juan-perez.pdf"
            ) is Result.Success
        )

        val body = take().body!!.utf8()
        assertTrue(body.contains("name=\"tipo\""))
        assertTrue(body.contains("ine_frente"))
        assertTrue(body.contains("name=\"archivo\"; filename=\"ine_frente.pdf\""))
        assertTrue(!body.contains("juan-perez"))
    }

    @Test
    fun `documento de mas de 5 MB o de otro tipo no se sube`() = runTest(dispatcher) {
        every { resolver.getType(any()) } returns "image/jpeg"
        every { resolver.openInputStream(any()) } returns
            ByteArrayInputStream(ByteArray((DOCUMENT_MAX_BYTES + 1).toInt()))
        assertTrue(
            (
                repository.uploadDocument(
                    sampleBusinessDraft(),
                    DocumentType.INE_BACK,
                    "content://x"
                ) as Result.Failure
                ).error is AppError.Validation
        )

        every { resolver.getType(any()) } returns "image/heic"
        assertTrue(
            repository.uploadDocument(sampleBusinessDraft(), DocumentType.INE_BACK, "content://x") is Result.Failure
        )
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `sin rol comerciante no hay altas`() = runTest(dispatcher) {
        respond(403, """{"codigo":"prohibido","mensaje":"sin rol","campos":[]}""")

        assertEquals(emptyList<Any>(), (repository.mine() as Result.Success).data)
    }

    @Test
    fun `enviar-revision con 422 trae la lista de faltantes`() = runTest(dispatcher) {
        respond(
            422,
            """{"codigo":"validacion","mensaje":"falta","campos":[{"campo":"fotos","mensaje":"Faltan 2 fotos"}]}"""
        )

        val error = (repository.submit(sampleBusinessDraft(step = 6)) as Result.Failure).error as AppError.Validation

        assertEquals("Faltan 2 fotos", error.fields.single().message)
    }
}
