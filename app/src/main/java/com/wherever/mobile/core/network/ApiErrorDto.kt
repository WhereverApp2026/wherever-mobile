package com.wherever.mobile.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FieldErrorDto(@SerialName("campo") val field: String, @SerialName("mensaje") val message: String)

@Serializable
data class ApiErrorDto(
    @SerialName("codigo") val code: String,
    @SerialName("mensaje") val message: String,
    @SerialName("campos") val fields: List<FieldErrorDto> = emptyList(),
    @SerialName("traza_id") val traceId: String? = null
)
