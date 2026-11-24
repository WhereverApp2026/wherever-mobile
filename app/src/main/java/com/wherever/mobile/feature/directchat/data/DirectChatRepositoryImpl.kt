package com.wherever.mobile.feature.directchat.data

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.directchat.domain.Conversation
import com.wherever.mobile.feature.directchat.domain.DirectAttachment
import com.wherever.mobile.feature.directchat.domain.DirectChatRepository
import com.wherever.mobile.feature.directchat.domain.DirectMessage
import com.wherever.mobile.feature.directchat.domain.PlanInvite
import com.wherever.mobile.feature.groups.domain.MessageStatus
import javax.inject.Inject

/** Sin Room a propósito: el chat 1 a 1 se lee con red (specs/nv-25, D43). */
class DirectChatRepositoryImpl @Inject constructor(private val api: DirectChatApi) : DirectChatRepository {
    override suspend fun open(userId: String): Result<Conversation> = safeApiCall {
        val dto = api.open(OpenConversationDto(userId))
        Conversation(dto.id, dto.other?.id, dto.other?.name, dto.other?.photoUrl?.takeIf(String::isNotBlank))
    }

    override suspend fun messages(conversationId: String, cursor: String?): Result<PageResult<DirectMessage>> =
        safeApiCall {
            val page = api.messages(conversationId, cursor = cursor)
            PageResult(page.items.map { it.toDomain() }, page.nextCursor?.takeIf(String::isNotBlank))
        }

    override suspend fun newer(conversationId: String, afterId: String): Result<List<DirectMessage>> = safeApiCall {
        api.messages(conversationId, after = afterId).items.map { it.toDomain() }
    }

    override suspend fun send(
        conversationId: String,
        text: String,
        clientId: String,
        planId: String?
    ): Result<DirectMessage> = safeApiCall {
        val attachment = planId?.let { AttachmentRequestDto(DirectAttachment.KIND_PLAN, it) }
        api.send(conversationId, DirectSendDto(text, clientId, attachment)).toDomain()
    }

    override suspend fun markRead(conversationId: String): Result<Unit> = safeApiCall { api.markRead(conversationId) }
}

internal fun DirectMessageDto.toDomain() = DirectMessage(
    id = id,
    clientId = null,
    authorId = authorId,
    text = text,
    attachment = attachment?.let {
        DirectAttachment(it.kind, it.id, it.name?.takeIf(String::isNotBlank), it.invite.toInvite())
    },
    deleted = deleted,
    sentAt = createdAt,
    status = MessageStatus.SENT
)

private fun String?.toInvite(): PlanInvite? = when (this) {
    "enviada" -> PlanInvite.SENT
    "aceptada" -> PlanInvite.ACCEPTED
    else -> null
}
