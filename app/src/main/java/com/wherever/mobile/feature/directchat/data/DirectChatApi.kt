package com.wherever.mobile.feature.directchat.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class OpenConversationDto(@SerialName("usuario_id") val userId: String)

@Serializable
data class DirectPersonDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("foto_url") val photoUrl: String? = null
)

@Serializable
data class ConversationDto(@SerialName("id") val id: String, @SerialName("otro") val other: DirectPersonDto? = null)

@Serializable
data class AttachmentDto(
    @SerialName("tipo") val kind: String,
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String? = null,
    @SerialName("invitacion") val invite: String? = null
)

@Serializable
data class DirectMessageDto(
    @SerialName("id") val id: String,
    @SerialName("autor_id") val authorId: String? = null,
    @SerialName("texto") val text: String = "",
    @SerialName("adjunto") val attachment: AttachmentDto? = null,
    @SerialName("eliminado") val deleted: Boolean = false,
    @SerialName("creado_en") val createdAt: String
)

@Serializable
data class DirectMessagePageDto(
    @SerialName("items") val items: List<DirectMessageDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class AttachmentRequestDto(@SerialName("tipo") val kind: String, @SerialName("id") val id: String)

@Serializable
data class DirectSendDto(
    @SerialName("texto") val text: String,
    @SerialName("id_cliente") val clientId: String,
    @SerialName("adjunto") val attachment: AttachmentRequestDto? = null
)

/** wherever-backend nv-87: mensajes directos entre amigos. */
interface DirectChatApi {
    @POST("conversaciones")
    suspend fun open(@Body body: OpenConversationDto): ConversationDto

    @GET("conversaciones/{id}/mensajes")
    suspend fun messages(
        @Path("id") id: String,
        @Query("cursor") cursor: String? = null,
        @Query("despues_de") after: String? = null,
        @Query("limite") limit: Int = PAGE_SIZE
    ): DirectMessagePageDto

    @POST("conversaciones/{id}/mensajes")
    suspend fun send(@Path("id") id: String, @Body body: DirectSendDto): DirectMessageDto

    @POST("conversaciones/{id}/leido")
    suspend fun markRead(@Path("id") id: String)

    companion object {
        const val PAGE_SIZE = 30
    }
}
