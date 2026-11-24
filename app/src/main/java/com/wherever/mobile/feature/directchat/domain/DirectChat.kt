package com.wherever.mobile.feature.directchat.domain

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.groups.domain.MessageStatus

/** Estado de la invitación de un plan compartido en el chat: el de quien lo recibió en el grupo del plan. */
enum class PlanInvite { SENT, ACCEPTED }

/** Plan, lugar o momento adjunto (backend nv-87). Solo el plan lleva invitación. */
data class DirectAttachment(val kind: String, val id: String, val name: String?, val invite: PlanInvite?) {
    val isPlan: Boolean
        get() = kind == KIND_PLAN

    companion object {
        const val KIND_PLAN = "plan"
    }
}

data class DirectMessage(
    /** Id del servidor, o `local:<id_cliente>` mientras no llega. */
    val id: String,
    val clientId: String?,
    /** Sin autor = mío y aún sin enviar. */
    val authorId: String?,
    val text: String,
    val attachment: DirectAttachment?,
    val deleted: Boolean,
    val sentAt: String,
    val status: MessageStatus
)

/** Conversación con un amigo; `otherId` falta si su cuenta ya no está activa. */
data class Conversation(val id: String, val otherId: String?, val otherName: String?, val otherPhoto: String?)

/** ChatAmigo: mensajes directos entre amigos (backend nv-87, `/conversaciones`). */
interface DirectChatRepository {
    /** Trae o crea la conversación con un amigo (sin amistad aceptada: 404). */
    suspend fun open(userId: String): Result<Conversation>

    /** Del más nuevo al más viejo; `cursor` sigue hacia atrás. */
    suspend fun messages(conversationId: String, cursor: String? = null): Result<PageResult<DirectMessage>>

    /** Lo nuevo después de `afterId`, del más viejo al más nuevo. */
    suspend fun newer(conversationId: String, afterId: String): Result<List<DirectMessage>>

    /** `id_cliente` hace idempotente el reenvío; `planId` adjunta un plan. */
    suspend fun send(
        conversationId: String,
        text: String,
        clientId: String,
        planId: String? = null
    ): Result<DirectMessage>

    suspend fun markRead(conversationId: String): Result<Unit>
}
