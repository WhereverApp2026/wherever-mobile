package com.wherever.mobile.feature.account.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT

@Serializable
data class ChangePasswordDto(
    @SerialName("password_actual") val current: String,
    @SerialName("password_nuevo") val new: String
)

@Serializable
data class ChangeEmailDto(
    @SerialName("correo_nuevo") val newEmail: String,
    @SerialName("password") val password: String
)

/** wherever-backend `docs/endpoints.md`, nv-04 y nv-05 (J1, A1). */
interface AccountApi {
    @PUT("auth/password")
    suspend fun changePassword(@Body body: ChangePasswordDto)

    /** Responde el perfil; la app lo vuelve a leer con `GET /usuarios/yo` para guardar `version`. */
    @PUT("usuarios/yo/correo")
    suspend fun changeEmail(@Body body: ChangeEmailDto)

    @POST("usuarios/yo/exportar")
    suspend fun requestExport()

    @DELETE("usuarios/yo/historial")
    suspend fun clearHistory()
}
