package com.wherever.mobile.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class RefreshRequestDto(@SerialName("refresh_token") val refreshToken: String)

@Serializable
data class SessionUserDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String,
    @SerialName("correo") val email: String,
    @SerialName("correo_verificado") val emailVerified: Boolean = false,
    @SerialName("roles") val roles: List<String> = emptyList()
)

/** Forma común de login, refresh, registro y restablecer. */
@Serializable
data class AuthTokensDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expira_en") val expiresAt: String,
    @SerialName("usuario") val user: SessionUserDto
)

/** Síncrona: se llama desde el hilo del Authenticator. */
interface RefreshApi {
    @POST("auth/refresh")
    fun refresh(@Body body: RefreshRequestDto): Call<AuthTokensDto>
}
