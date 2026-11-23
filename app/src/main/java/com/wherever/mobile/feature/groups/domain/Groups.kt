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
    /** Versión para `If-Match` al renombrar (nv-69). */
    val version: Int = 0,
    val myStatus: MemberStatus,
    val iAmOrganizer: Boolean,
    val confirmed: Int,
    val members: List<GroupMember>,
    val lastMessage: LastMessage?,
    val updatedAt: String
)

enum class MessageStatus { SENT, PENDING, FAILED }

/** Una opción de la votación con sus votos; `placeName` falta si el lugar dejó de estar disponible. */
data class VoteOption(val placeId: String, val placeName: String?, val votes: Int)

/** Votación de una parada (backend nv-39): conteos, nunca quién votó qué. */
data class Vote(
    val id: String,
    val stopId: String,
    val options: List<VoteOption>,
    val total: Int,
    val myVote: String?,
    val closesAt: String,
    val open: Boolean,
    val winner: String?,
    /** Al cerrar: si la parada del plan cambió a la ganadora (`false` si no cupo). */
    val applied: Boolean?
)

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

/** Integrantes del grupo: crear, invitar, renombrar, salir y sacar (backend nv-37, nv-69). */
interface GroupMembersRepository {
    /** 43_CrearGrupo: un grupo nace de un plan con hasta 11 amigos (`POST /grupos`). */
    suspend fun create(planId: String, invitees: List<String>): Result<Group>

    /** Invitar a más amigos (solo el organizador). */
    suspend fun invite(groupId: String, userIds: List<String>): Result<Group>

    /** InvitarPlan: invita al grupo del plan o lo crea si el plan aún no tiene grupo. */
    suspend fun inviteToPlan(planId: String, userIds: List<String>): Result<Group>

    suspend fun rename(group: Group, name: String): Result<Group>

    /** Salir del grupo; el organizador no puede (422). */
    suspend fun leave(groupId: String): Result<Unit>

    /** Sacar a un integrante (solo el organizador). */
    suspend fun removeMember(groupId: String, userId: String): Result<Unit>
}

/** Votaciones del grupo (backend nv-39). */
interface VotesRepository {
    suspend fun votes(groupId: String): Result<List<Vote>>

    /** Un voto por persona; se cambia hasta el cierre. */
    suspend fun vote(voteId: String, placeId: String): Result<Vote>

    /** Cerrar antes de tiempo (solo el organizador). */
    suspend fun closeVote(voteId: String): Result<Vote>
}
