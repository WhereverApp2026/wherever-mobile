package com.wherever.mobile.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
    @SerialName("codigo")
    val errorCode: String? = null,
    @SerialName("mensaje")
    val message: String? = null,
    @SerialName("campos")
    val fields: List<ApiErrorFieldDto> = emptyList()
)

@Serializable
data class ApiErrorFieldDto(
    @SerialName("campo")
    val field: String? = null,
    @SerialName("mensaje")
    val message: String? = null
)
