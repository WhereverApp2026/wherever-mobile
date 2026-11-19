package com.wherever.mobile.feature.notifications.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class NotificationDto(
    @SerialName("id") val id: String,
    @SerialName("tipo") val type: String,
    @SerialName("datos") val data: JsonObject = JsonObject(emptyMap()),
    @SerialName("leida") val read: Boolean = false,
    @SerialName("creada_en") val createdAt: String
)

@Serializable
data class NotificationPageDto(
    @SerialName("items") val items: List<NotificationDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class UnreadCountDto(@SerialName("total") val total: Int)

@Serializable
data class MarkReadDto(@SerialName("leida") val read: Boolean)

/** wherever-backend `docs/endpoints.md`, nv-40 (A6). */
interface NotificationsApi {
    @GET("notificaciones")
    suspend fun page(@Query("cursor") cursor: String?, @Query("limite") limit: Int): NotificationPageDto

    @GET("notificaciones/no-leidas")
    suspend fun unread(): UnreadCountDto

    @PATCH("notificaciones/{id}")
    suspend fun mark(@Path("id") id: String, @Body body: MarkReadDto): NotificationDto

    @POST("notificaciones/leer-todas")
    suspend fun markAll()

    @DELETE("notificaciones/{id}")
    suspend fun delete(@Path("id") id: String)
}
