package com.wherever.mobile.feature.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PreferencesRequestDto(
    @SerialName("intereses")
    val interests: List<String>,
    @SerialName("ambiente")
    val vibe: String,
    @SerialName("presupuesto")
    val budget: String,
    @SerialName("compania")
    val company: String
)

@Serializable
data class FriendRequestDto(
    @SerialName("usuario_id")
    val userId: String
)
