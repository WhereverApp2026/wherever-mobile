package com.wherever.mobile.feature.help.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

/** Cuerpo de `POST /errores`. Sin datos del perfil: solo el texto del usuario (specs/nv-33 §5). */
@Serializable
data class ErrorReportDto(
    @SerialName("modulo") val module: String,
    @SerialName("mensaje") val message: String,
    @SerialName("version") val version: String,
    @SerialName("traza_id") val traceId: String? = null
)

/** wherever-backend `docs/endpoints.md`: `POST /api/v1/errores` → 202 sin cuerpo. */
interface HelpApi {
    @POST("errores")
    suspend fun report(@Body body: ErrorReportDto)
}
