package com.wherever.mobile.feature.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(
    @SerialName("id")
    val id: String,
    @SerialName("nombre")
    val name: String,
    @SerialName("plataforma")
    val platform: String = "android",
    @SerialName("version_app")
    val appVersion: String = ""
)

@Serializable
data class LoginRequestDto(
    @SerialName("correo")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("dispositivo")
    val device: DeviceDto
)

@Serializable
data class RegisterRequestDto(
    @SerialName("nombre")
    val name: String,
    @SerialName("fecha_nacimiento")
    val birthDate: String,
    @SerialName("correo")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("version_terminos")
    val termsVersion: String
)

@Serializable
data class RefreshRequestDto(
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class RecoverRequestDto(
    @SerialName("correo")
    val email: String
)

@Serializable
data class VerifyEmailRequestDto(
    @SerialName("token")
    val token: String
)

@Serializable
data class ResetPasswordRequestDto(
    @SerialName("token")
    val token: String,
    @SerialName("password_nuevo")
    val newPassword: String
)
