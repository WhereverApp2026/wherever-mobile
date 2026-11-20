package com.wherever.mobile.feature.status.data

import com.wherever.mobile.core.network.PublicEndpoint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class ConfigurationDto(
    @SerialName("version_minima_app") val minimumVersion: String = "0.0.0",
    @SerialName("mantenimiento") val maintenance: Boolean = false
)

interface StatusApi {
    @PublicEndpoint
    @GET("configuracion")
    suspend fun configuration(): ConfigurationDto
}
