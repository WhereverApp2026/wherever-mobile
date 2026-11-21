package com.wherever.mobile.testing

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.groups.data.GroupMessageDao
import com.wherever.mobile.feature.groups.data.GroupMessageEntity
import com.wherever.mobile.feature.groups.domain.ChatMessage
import com.wherever.mobile.feature.groups.domain.Group
import com.wherever.mobile.feature.groups.domain.GroupMember
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.groups.domain.MessageStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

fun sampleGroup(id: String = "g1", status: MemberStatus = MemberStatus.CONFIRMED, name: String? = "Tacos") = Group(
    id = id,
    planId = "p1",
    name = name,
    myStatus = status,
    iAmOrganizer = false,
    confirmed = 2,
    members = listOf(
        GroupMember("u1", "Ana López", null, true, MemberStatus.CONFIRMED),
        GroupMember("u2", "Luis Pérez", null, false, MemberStatus.CONFIRMED)
    ),
    lastMessage = null,
    updatedAt = "2026-11-20T18:00:00Z"
)

fun sampleMessage(
    id: String,
    author: String? = "u2",
    status: MessageStatus = MessageStatus.SENT,
    clientId: String? = null
) = ChatMessage(id, clientId, author, "Luis", null, false, "hola $id", false, "2026-11-20T18:0$id:00Z", status)

class FakeGroupsRepository : GroupsRepository {
    var groupsResult: Result<PageResult<Group>> = Result.Success(PageResult(listOf(sampleGroup()), null))
    var groupResult: Result<Group> = Result.Success(sampleGroup())
    var respondResult: Result<Group>? = null
    var refreshResult: Result<String?> = Result.Success(null)
    var olderResult: Result<String?> = Result.Success(null)
    var pollResult: Result<Unit> = Result.Success(Unit)
    var sendResult: Result<Unit> = Result.Success(Unit)
    val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val cursors = mutableListOf<String?>()
    val sent = mutableListOf<String>()
    val retried = mutableListOf<String>()
    var polls = 0

    override suspend fun groups(cursor: String?): Result<PageResult<Group>> {
        cursors += cursor
        return groupsResult
    }

    override suspend fun group(id: String): Result<Group> = groupResult

    override suspend fun respond(id: String, accept: Boolean): Result<Group> = respondResult
        ?: Result.Success(sampleGroup(id, if (accept) MemberStatus.CONFIRMED else MemberStatus.DECLINED))

    override fun observeMessages(groupId: String): Flow<List<ChatMessage>> = messages

    override suspend fun refreshMessages(groupId: String): Result<String?> = refreshResult

    override suspend fun loadOlder(groupId: String, cursor: String): Result<String?> {
        cursors += cursor
        return olderResult
    }

    override suspend fun pollNew(groupId: String): Result<Unit> {
        polls++
        return pollResult
    }

    override suspend fun send(groupId: String, text: String): Result<Unit> {
        sent += text
        return sendResult
    }

    override suspend fun retry(groupId: String, clientId: String): Result<Unit> {
        retried += clientId
        return sendResult
    }
}

class FakeGroupMessageDao : GroupMessageDao {
    val rows = MutableStateFlow<Map<String, GroupMessageEntity>>(emptyMap())

    override fun observe(groupId: String): Flow<List<GroupMessageEntity>> = rows.map { all ->
        all.values.filter {
            it.groupId == groupId
        }.sortedWith(compareByDescending<GroupMessageEntity> { it.sentAt }.thenByDescending { it.id })
    }

    override suspend fun lastSent(groupId: String): GroupMessageEntity? = rows.value.values
        .filter { it.groupId == groupId && it.status == MessageStatus.SENT.name }
        .maxWithOrNull(compareBy<GroupMessageEntity> { it.sentAt }.thenBy { it.id })

    override suspend fun byClientId(groupId: String, clientId: String): GroupMessageEntity? =
        rows.value.values.firstOrNull { it.groupId == groupId && it.clientId == clientId }

    override suspend fun upsert(messages: List<GroupMessageEntity>) {
        rows.value = rows.value + messages.associateBy { it.id }
    }

    override suspend fun delete(id: String) {
        rows.value = rows.value - id
    }

    override suspend fun clear() {
        rows.value = emptyMap()
    }
}

val GONE: AppError = AppError.NotFound()
