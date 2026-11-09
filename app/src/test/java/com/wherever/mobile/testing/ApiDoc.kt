package com.wherever.mobile.testing

import com.wherever.mobile.core.network.AuthHeaderInterceptor
import com.wherever.mobile.core.network.RedactedLoggingInterceptor
import com.wherever.mobile.core.network.RefreshApi
import com.wherever.mobile.core.network.RequestIdInterceptor
import com.wherever.mobile.core.network.SessionEvents
import com.wherever.mobile.core.network.TokenRefreshAuthenticator
import com.wherever.mobile.core.security.TokenStore
import java.io.File
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Respuestas exactas del contrato `docs/contexto_api_sprint1.md`, leídas del propio documento.
 * Si el contrato cambia, estas pruebas cambian con él (constitución §11: sin cambio de contrato sin spec).
 * Nunca se usa el servidor de staging.
 */
object ApiDoc {
    private val markdown: String by lazy {
        listOf("../docs/contexto_api_sprint1.md", "docs/contexto_api_sprint1.md")
            .map(::File)
            .first { it.isFile }
            .readText()
            .replace("\r\n", "\n")
    }

    private fun section(endpoint: String): String {
        val start = markdown.indexOf("#### $endpoint\n")
        require(start >= 0) { "No está en la doc: $endpoint" }
        val end = markdown.indexOf("\n#### ", start + 1).takeIf { it > 0 } ?: markdown.length
        return markdown.substring(start, end)
    }

    /**
     * Cuerpo JSON de `Response <status>` del endpoint, p. ej. `response("POST /api/v1/auth/login", 200)`.
     * Único cambio: la doc recorta el JWT de ejemplo con `…` (U+2026), que no es válido en una cabecera
     * HTTP; se escribe `...` para que el `Authorization` sea enviable.
     */
    fun response(endpoint: String, status: Int): String {
        val text = section(endpoint)
        val header = text.indexOf("Response $status")
        require(header >= 0) { "$endpoint no documenta Response $status" }
        val open = text.indexOf("```json\n", header) + "```json\n".length
        val close = text.indexOf("\n```", open)
        return text.substring(open, close).replace("…", "...")
    }

    /** `codigo` que la doc asigna a un estado HTTP (sección "Forma de los errores"). */
    fun errorCode(status: Int): String =
        Regex("^- $status `([a-z_]+)`", RegexOption.MULTILINE).find(markdown)?.groupValues?.get(1)
            ?: error("La doc no define el código del estado $status")

    /** Error con la forma `{codigo, mensaje, campos[], traza_id}` de la doc. */
    fun error(status: Int, message: String, fields: Map<String, String> = emptyMap()): String {
        val campos = fields.entries.joinToString(",") { (campo, mensaje) ->
            """{"campo":"$campo","mensaje":"$mensaje"}"""
        }
        return """{"codigo":"${errorCode(status)}","mensaje":"$message","campos":[$campos],""" +
            """"traza_id":"f6bd9485da30dcf0fbcd3eb34cfe4915"}"""
    }
}

fun MockWebServer.reply(code: Int, body: String = "", vararg headers: Pair<String, String>) {
    val builder = MockResponse.Builder().code(code).body(body)
    headers.forEach { (name, value) -> builder.addHeader(name, value) }
    enqueue(builder.build())
}

/** La misma cadena que `NetworkModule`: RequestId → AuthHeader → RedactedLogging + Authenticator aparte. */
class RealNetwork(server: MockWebServer, tokenStore: TokenStore, val sessionEvents: SessionEvents = SessionEvents()) {
    val logs = mutableListOf<String>()
    private val baseUrl = server.url("/api/v1/")
    private val refreshApi: RefreshApi = testRetrofit(
        baseUrl,
        OkHttpClient.Builder().addInterceptor(RequestIdInterceptor()).build()
    ).create(RefreshApi::class.java)

    val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(RequestIdInterceptor())
        .addInterceptor(AuthHeaderInterceptor(tokenStore))
        .addInterceptor(RedactedLoggingInterceptor { synchronized(logs) { logs += it } })
        .authenticator(TokenRefreshAuthenticator(tokenStore, refreshApi, sessionEvents))
        .build()

    val retrofit: Retrofit = testRetrofit(baseUrl, client)
}
