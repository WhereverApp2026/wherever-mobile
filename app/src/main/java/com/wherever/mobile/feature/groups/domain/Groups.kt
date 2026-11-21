package com.wherever.mobile.feature.groups.domain

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import kotlinx.coroutines.flow.Flow

/** Estado de una persona en el grupo (backend nv-37). */
enum class MemberStatus(val apiValue: String) {
    INVITED("invitado"),
    CONFIRMED("confirmado"),
    DECLINED("declinado");

    companion object {
        fun from(value: String?): MemberStatus = entries.firstOrNull { it.apiValue == value } ?: INVITED
    }
}

data class GroupMember(
    val userId: String,
    val name: String,
    val photoUrl: String?,
    val organizer: Boolean,
    val status: MemberStatus
)

data class LastMessage(val text: String, val authorId: String?, val sentAt: String)

data class Group(
    val id: String,
    val planId: String,
    /** Nombre que le puso el organizador (nv-69); sin él se muestran los integrantes. */
    val name: String?,
    val myStatus: MemberStatus,
    val iAmOrganizer: Boolean,
    val confirmed: Int,
    val members: List<GroupMember>,
    val lastMessage: LastMessage?,
    val updatedAt: String
)

enum class MessageStatus { SENT, PENDING, FAILED }

data class ChatMessage(
    /** Id del servidor, o `local:<id_cliente>` mientras no llega. */
    val id: String,
    val clientId: String?,
    val authorId: String?,
    val authorName: String?,
    val authorPhoto: String?,
    /** Aviso del sistema ("Ana se unió"): sin autor, centrado. */
    val system: Boolean,
    val text: String,
    val deleted: Boolean,
    val sentAt: String,
    val status: MessageStatus
)

/** Grupos y chat del grupo (backend nv-37, nv-57). Room guarda los mensajes para leerlos sin red. */
interface GroupsRepository {
    suspend fun groups(cursor: String? = null): Result<PageResult<Group>>

    suspend fun group(id: String): Result<Group>

    /** Aceptar o rechazar la invitación (`PATCH /grupos/{id}/integrantes/yo`). */
    suspend fun respond(id: String, accept: Boolean): Result<Group>

    fun observeMessages(groupId: String): Flow<List<ChatMessage>>

    /** Página más nueva; devuelve el cursor para pedir mensajes más viejos. */
    suspend fun refreshMessages(groupId: String): Result<String?>

    suspend fun loadOlder(groupId: String, cursor: String): Result<String?>

    /** Lo nuevo después del último mensaje que tiene el teléfono (`despues_de`). */
    suspend fun pollNew(groupId: String): Result<Unit>

    /** Envío optimista con `id_cliente`: repetirlo nunca duplica el mensaje en el servidor. */
    suspend fun send(groupId: String, text: String): Result<Unit>

    suspend fun retry(groupId: String, clientId: String): Result<Unit>
}
