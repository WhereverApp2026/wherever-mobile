package com.wherever.mobile.feature.auth.data

import com.wherever.mobile.core.network.AuthTokensDto
import com.wherever.mobile.core.network.PublicEndpoint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class DeviceDto(
    @SerialName("modelo") val model: String,
    @SerialName("sistema") val system: String,
    @SerialName("app_version") val appVersion: String
)

@Serializable
data class LoginRequestDto(
    @SerialName("correo") val email: String,
    @SerialName("password") val password: String,
    @SerialName("dispositivo") val device: DeviceDto
)

@Serializable
data class RegisterRequestDto(
    @SerialName("nombre") val name: String,
    @SerialName("fecha_nacimiento") val birthDate: String,
    @SerialName("correo") val email: String,
    @SerialName("password") val password: String,
    @SerialName("acepta_terminos") val acceptsTerms: Boolean,
    @SerialName("version_terminos") val termsVersion: String,
    @SerialName("promociones_correo") val emailPromotions: Boolean,
    @SerialName("dispositivo") val device: DeviceDto
)

@Serializable
data class RecoverRequestDto(@SerialName("correo") val email: String)

@Serializable
data class EmailAvailabilityDto(@SerialName("disponible") val available: Boolean)

@Serializable
data class SessionDto(
    @SerialName("id") val id: String,
    @SerialName("dispositivo") val device: DeviceDto,
    @SerialName("ultimo_uso") val lastUsed: String,
    @SerialName("actual") val isCurrent: Boolean = false
)

interface AuthApi {
    @PublicEndpoint
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthTokensDto

    @PublicEndpoint
    @GET("usuarios/correo-disponible")
    suspend fun emailAvailability(@Query("correo") email: String): EmailAvailabilityDto

    @PublicEndpoint
    @POST("usuarios")
    suspend fun register(@Body body: RegisterRequestDto): AuthTokensDto

    @PublicEndpoint
    @POST("auth/recuperar")
    suspend fun recover(@Body body: RecoverRequestDto)

    @POST("auth/reenviar-verificacion")
    suspend fun resendVerification()

    @GET("auth/sesiones")
    suspend fun sessions(): List<SessionDto>

    @DELETE("auth/sesiones/{id}")
    suspend fun closeSession(@Path("id") id: String)

    @POST("auth/logout")
    suspend fun logout()

    @POST("auth/logout-todos")
    suspend fun logoutAll()
}
