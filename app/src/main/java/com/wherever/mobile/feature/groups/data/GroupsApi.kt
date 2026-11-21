package com.wherever.mobile.feature.groups.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class GroupPersonDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String,
    @SerialName("foto_url") val photoUrl: String? = null
)

@Serializable
data class GroupMemberDto(
    @SerialName("usuario_id") val userId: String,
    @SerialName("persona") val person: GroupPersonDto? = null,
    @SerialName("rol") val role: String = "integrante",
    @SerialName("estado") val status: String = "invitado"
)

@Serializable
data class GroupSummaryDto(
    @SerialName("confirmados") val confirmed: Int = 0,
    @SerialName("invitados") val invited: Int = 0
)

@Serializable
data class LastMessageDto(
    @SerialName("mensaje_id") val id: String,
    @SerialName("texto") val text: String = "",
    @SerialName("autor_id") val authorId: String? = null,
    @SerialName("fecha") val date: String
)

@Serializable
data class GroupDto(
    @SerialName("id") val id: String,
    @SerialName("plan_id") val planId: String,
    @SerialName("nombre") val name: String? = null,
    @SerialName("mi_rol") val myRole: String = "integrante",
    @SerialName("mi_estado") val myStatus: String = "invitado",
    @SerialName("resumen") val summary: GroupSummaryDto = GroupSummaryDto(),
    @SerialName("integrantes") val members: List<GroupMemberDto> = emptyList(),
    @SerialName("ultimo_mensaje") val lastMessage: LastMessageDto? = null,
    @SerialName("actualizado_en") val updatedAt: String = ""
)

@Serializable
data class GroupPageDto(
    @SerialName("items") val items: List<GroupDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class MessageAuthorDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String,
    @SerialName("foto_url") val photoUrl: String? = null
)

@Serializable
data class MessageDto(
    @SerialName("id") val id: String,
    @SerialName("autor") val author: MessageAuthorDto? = null,
    @SerialName("tipo") val type: String = "texto",
    @SerialName("texto") val text: String = "",
    @SerialName("eliminado") val deleted: Boolean = false,
    @SerialName("creado_en") val createdAt: String,
    @SerialName("id_cliente") val clientId: String? = null
)

@Serializable
data class MessagePageDto(
    @SerialName("items") val items: List<MessageDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class SendMessageDto(@SerialName("texto") val text: String, @SerialName("id_cliente") val clientId: String)

@Serializable
data class MyMembershipDto(@SerialName("estado") val status: String)

/** wherever-backend `docs/endpoints.md`: grupos (nv-37) y chat del grupo (nv-57). */
interface GroupsApi {
    @GET("grupos")
    suspend fun list(@Query("cursor") cursor: String?, @Query("limite") limit: Int): GroupPageDto

    @GET("grupos/{id}")
    suspend fun get(@Path("id") id: String): GroupDto

    @PATCH("grupos/{id}/integrantes/yo")
    suspend fun respond(@Path("id") id: String, @Body body: MyMembershipDto): GroupDto

    @GET("grupos/{id}/mensajes")
    suspend fun messages(
        @Path("id") id: String,
        @Query("cursor") cursor: String? = null,
        @Query("despues_de") after: String? = null,
        @Query("limite") limit: Int = PAGE_SIZE
    ): MessagePageDto

    @POST("grupos/{id}/mensajes")
    suspend fun send(@Path("id") id: String, @Body body: SendMessageDto): MessageDto

    companion object {
        const val PAGE_SIZE = 30
    }
}
