package com.wherever.mobile.core.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.PUT

const val PLATFORM_ANDROID = "android"

@Serializable
data class RegisterDeviceDto(
    @SerialName("token") val token: String,
    // Sin valor por defecto: `ApiJson` no escribe los valores por defecto y el campo se perdería.
    @SerialName("plataforma") val platform: String
)

@Serializable
data class UnregisterDeviceDto(@SerialName("token") val token: String)

/** Token de FCM del teléfono. Va siempre en el cuerpo, nunca en la ruta (specs/nv-46 §3). */
interface DevicesApi {
    @PUT("dispositivos")
    suspend fun register(@Body body: RegisterDeviceDto)

    @HTTP(method = "DELETE", path = "dispositivos", hasBody = true)
    suspend fun unregister(@Body body: UnregisterDeviceDto)
}
