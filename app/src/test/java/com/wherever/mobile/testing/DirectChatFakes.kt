package com.wherever.mobile.testing

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.directchat.domain.Conversation
import com.wherever.mobile.feature.directchat.domain.DirectAttachment
import com.wherever.mobile.feature.directchat.domain.DirectChatRepository
import com.wherever.mobile.feature.directchat.domain.DirectMessage
import com.wherever.mobile.feature.groups.domain.MessageStatus

fun sampleDirect(id: String, author: String?, at: String, attachment: DirectAttachment? = null) =
    DirectMessage(id, null, author, "texto $id", attachment, false, at, MessageStatus.SENT)

class FakeDirectChatRepository : DirectChatRepository {
    var openResult: Result<Conversation> = Result.Success(Conversation("c1", "u2", "Mariana López", null))
    var pageResult: Result<PageResult<DirectMessage>> = Result.Success(PageResult(emptyList(), null))
    var newerResult: Result<List<DirectMessage>> = Result.Success(emptyList())
    var sendResult: ((text: String, clientId: String) -> Result<DirectMessage>)? = null
    val calls = mutableListOf<String>()

    override suspend fun open(userId: String): Result<Conversation> {
        calls += "open:$userId"
        return openResult
    }

    override suspend fun messages(conversationId: String, cursor: String?): Result<PageResult<DirectMessage>> {
        calls += "messages:$conversationId:${cursor.orEmpty()}"
        return pageResult
    }

    override suspend fun newer(conversationId: String, afterId: String): Result<List<DirectMessage>> {
        calls += "newer:$conversationId:$afterId"
        return newerResult
    }

    override suspend fun send(
        conversationId: String,
        text: String,
        clientId: String,
        planId: String?
    ): Result<DirectMessage> {
        calls += "send:$conversationId:$text:${planId.orEmpty()}"
        return sendResult?.invoke(text, clientId)
            ?: Result.Success(sampleDirect("s${calls.size}", "yo", "2026-11-24T18:00:00Z").copy(text = text))
    }

    override suspend fun markRead(conversationId: String): Result<Unit> {
        calls += "read:$conversationId"
        return Result.Success(Unit)
    }
}
