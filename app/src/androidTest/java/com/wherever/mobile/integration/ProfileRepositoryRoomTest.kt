package com.wherever.mobile.integration

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.database.WhereverDatabase
import com.wherever.mobile.core.network.ApiJson
import com.wherever.mobile.feature.profile.data.ProfileRepositoryImpl
import com.wherever.mobile.feature.profile.data.UserProfileDao
import com.wherever.mobile.feature.profile.data.UsersApi
import java.net.InetAddress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Perfil de `GET /api/v1/usuarios/yo` copiado de `docs/contexto_api_sprint1.md` (el dispositivo no ve `docs/`). */
private val PROFILE_200 = """
    {"id":"6728f0c1a4b2c3d4e5f60718","nombre":"Ana López","correo":"ana@ejemplo.com","correo_verificado":true,
    "foto_url":"","codigo_amigo":"K7M2Q9XA","roles":["consumidor"],"modo_actual":"explorar",
    "ciudad":{"id":"66f1a2b3c4d5e6f708192a3b","nombre":"Guadalajara"},
    "gustos":{"categorias":["cafes","museos","parques"],"presupuesto_min":100,"presupuesto_max":500,
    "restricciones":["vegetariano"],"alergias":"nueces"},
    "preferencias":{"movilidad_default":"a_pie","tema":"sistema","unidades":"km",
    "horario_silencioso":{"desde":"22:00","hasta":"07:00"},
    "notificaciones":{"reservas":true,"cupones":true,"grupos":true,"resenas":true,"promociones":false}},
    "privacidad":{"ubicacion":true,"aprendizaje_personalizado":true,"estado_animo":false,"datos_uso":true,
    "buscable_por_telefono":false,"version_terminos":"2026-10","fecha_aceptacion":"2026-11-03T18:20:00Z",
    "promociones_correo":false},
    "telefono_registrado":false,"puntos":50,"nivel":"Explorador","racha_semanas":0,"estado":"activo",
    "eliminar_en":null,"version":3,"creado_en":"2026-11-03T18:20:00Z","actualizado_en":"2026-11-03T19:02:11Z"}
""".trimIndent()

private const val CONFLICT_409 =
    """{"codigo":"conflicto","mensaje":"la versión cambió","campos":[],"traza_id":"f6bd9485da30dcf0fbcd3eb34cfe4915"}"""

/**
 * Integración con Room real en el dispositivo (constitución §2.7) + MockWebServer por HTTPS local.
 * El certificado es de prueba y solo lo confía este cliente: la app sigue con solo CA del sistema.
 * Specs nv-04, nv-14 y nv-17. Nunca usa staging.
 */
@RunWith(AndroidJUnit4::class)
class ProfileRepositoryRoomTest {
    private val localhost = InetAddress.getByName("localhost").canonicalHostName
    private val certificate = HeldCertificate.Builder().addSubjectAlternativeName(localhost).build()
    private val server = MockWebServer()
    private lateinit var database: WhereverDatabase
    private lateinit var dao: UserProfileDao
    private lateinit var repository: ProfileRepositoryImpl

    @Before
    fun setUp() {
        val serverTls = HandshakeCertificates.Builder().heldCertificate(certificate).build()
        server.useHttps(serverTls.sslSocketFactory())
        server.start()
        val clientTls = HandshakeCertificates.Builder().addTrustedCertificate(certificate.certificate).build()
        val client = OkHttpClient.Builder()
            .sslSocketFactory(clientTls.sslSocketFactory(), clientTls.trustManager)
            .build()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .client(client)
            .addConverterFactory(ApiJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UsersApi::class.java)
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            WhereverDatabase::class.java
        ).build()
        dao = database.userProfileDao()
        repository = ProfileRepositoryImpl(api, dao)
    }

    @After
    fun tearDown() {
        database.close()
        server.close()
    }

    private fun reply(code: Int, body: String) {
        server.enqueue(MockResponse.Builder().code(code).body(body).build())
    }

    @Test
    fun perfilDeLaApiQuedaEnRoomYLaUiLeeDeRoom() = runTest {
        reply(200, PROFILE_200)

        assertTrue(repository.refresh() is Result.Success)

        val observed = repository.observeProfile().first()!!
        assertEquals("Guadalajara", observed.city?.name)
        assertEquals(3L, dao.get()?.version)
        assertEquals(listOf("vegetariano"), dao.get()?.tastes?.restrictions)
    }

    @Test
    fun conflicto409NoTocaRoom() = runTest {
        reply(200, PROFILE_200)
        repository.refresh()
        reply(409, CONFLICT_409)

        val error = (repository.updateProfile(name = "Otro") as Result.Failure).error

        assertTrue(error is AppError.Conflict)
        assertEquals("Ana López", dao.get()?.name)
        assertEquals(
            "\"3\"",
            server.run {
                takeRequest()
                takeRequest()
            }.headers["If-Match"]
        )
    }

    @Test
    fun sinRedRoomConservaElUltimoPerfil() = runTest {
        reply(200, PROFILE_200)
        repository.refresh()
        server.close()

        val error = (repository.refresh() as Result.Failure).error

        assertTrue(error is AppError.Network || error is AppError.Timeout)
        assertEquals("Ana López", repository.observeProfile().first()?.name)
    }

    @Test
    fun limpiarSesionVaciaRoom() = runTest {
        reply(200, PROFILE_200)
        repository.refresh()

        dao.clear()

        assertNull(repository.observeProfile().first())
    }
}
