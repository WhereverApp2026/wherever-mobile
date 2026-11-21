package com.wherever.mobile.feature.groups.data

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.SessionCleaner
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.groups.domain.ChatMessage
import com.wherever.mobile.feature.groups.domain.Group
import com.wherever.mobile.feature.groups.domain.GroupMember
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import com.wherever.mobile.feature.groups.domain.LastMessage
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.groups.domain.MessageStatus
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val GROUPS_PAGE = 20
private const val LOCAL_PREFIX = "local:"

class GroupsRepositoryImpl @Inject constructor(
    private val api: GroupsApi,
    private val dao: GroupMessageDao,
    private val clock: Clock
) : GroupsRepository {
    override suspend fun groups(cursor: String?): Result<PageResult<Group>> = safeApiCall {
        val page = api.list(cursor, GROUPS_PAGE)
        PageResult(page.items.map { it.toGroup() }, page.nextCursor)
    }

    override suspend fun group(id: String): Result<Group> = safeApiCall { api.get(id).toGroup() }

    override suspend fun respond(id: String, accept: Boolean): Result<Group> = safeApiCall {
        val status = if (accept) MemberStatus.CONFIRMED else MemberStatus.DECLINED
        api.respond(id, MyMembershipDto(status.apiValue)).toGroup()
    }

    override fun observeMessages(groupId: String): Flow<List<ChatMessage>> =
        dao.observe(groupId).map { rows -> rows.map { it.toMessage() } }

    override suspend fun refreshMessages(groupId: String): Result<String?> = safeApiCall {
        val page = api.messages(groupId)
        dao.upsert(page.items.map { it.toEntity(groupId) })
        page.nextCursor
    }

    override suspend fun loadOlder(groupId: String, cursor: String): Result<String?> = safeApiCall {
        val page = api.messages(groupId, cursor = cursor)
        dao.upsert(page.items.map { it.toEntity(groupId) })
        page.nextCursor
    }

    override suspend fun pollNew(groupId: String): Result<Unit> {
        val last = dao.lastSent(groupId) ?: return refreshMessages(groupId).map { }
        return safeApiCall {
            val page = api.messages(groupId, after = last.id)
            dao.upsert(page.items.map { it.toEntity(groupId) })
        }
    }

    override suspend fun send(groupId: String, text: String): Result<Unit> {
        val clientId = UUID.randomUUID().toString()
        val pending = GroupMessageEntity(
            id = LOCAL_PREFIX + clientId,
            groupId = groupId,
            clientId = clientId,
            authorId = null,
            authorName = null,
            authorPhoto = null,
            system = false,
            text = text.trim(),
            deleted = false,
            sentAt = Instant.now(clock).toString(),
            status = MessageStatus.PENDING.name
        )
        dao.upsert(listOf(pending))
        return deliver(pending)
    }

    override suspend fun retry(groupId: String, clientId: String): Result<Unit> {
        val pending = dao.byClientId(groupId, clientId) ?: return Result.Success(Unit)
        if (pending.status == MessageStatus.SENT.name) return Result.Success(Unit)
        dao.upsert(listOf(pending.copy(status = MessageStatus.PENDING.name)))
        return deliver(pending)
    }

    /** El mismo `id_cliente` en el reintento: el servidor devuelve el ya guardado (200), sin duplicar. */
    private suspend fun deliver(pending: GroupMessageEntity): Result<Unit> {
        val clientId = checkNotNull(pending.clientId)
        val result = safeApiCall { api.send(pending.groupId, SendMessageDto(pending.text, clientId)) }
        return when (result) {
            is Result.Success -> {
                dao.delete(pending.id)
                dao.upsert(listOf(result.data.copy(clientId = clientId).toEntity(pending.groupId)))
                Result.Success(Unit)
            }
            is Result.Failure -> {
                dao.upsert(listOf(pending.copy(status = MessageStatus.FAILED.name)))
                result
            }
        }
    }
}

/** Mensajes del chat: dato personal que no sobrevive a la sesión (specs/nv-25 §5). */
class GroupMessagesCleaner @Inject constructor(private val dao: GroupMessageDao) : SessionCleaner {
    override suspend fun clear() = dao.clear()
}

internal fun GroupDto.toGroup() = Group(
    id = id,
    planId = planId,
    name = name?.takeIf { it.isNotBlank() },
    myStatus = MemberStatus.from(myStatus),
    iAmOrganizer = myRole == "organizador",
    confirmed = summary.confirmed,
    members = members.map { member ->
        GroupMember(
            userId = member.userId,
            name = member.person?.name.orEmpty(),
            photoUrl = member.person?.photoUrl?.takeIf { it.isNotBlank() },
            organizer = member.role == "organizador",
            status = MemberStatus.from(member.status)
        )
    },
    lastMessage = lastMessage?.let { LastMessage(it.text, it.authorId, it.date) },
    updatedAt = updatedAt
)

internal fun MessageDto.toEntity(groupId: String) = GroupMessageEntity(
    id = id,
    groupId = groupId,
    clientId = clientId,
    authorId = author?.id,
    authorName = author?.name,
    authorPhoto = author?.photoUrl?.takeIf { it.isNotBlank() },
    system = type == "sistema" || author == null,
    text = if (deleted) "" else text,
    deleted = deleted,
    sentAt = createdAt,
    status = MessageStatus.SENT.name
)

internal fun GroupMessageEntity.toMessage() = ChatMessage(
    id = id,
    clientId = clientId,
    authorId = authorId,
    authorName = authorName,
    authorPhoto = authorPhoto,
    system = system,
    text = text,
    deleted = deleted,
    sentAt = sentAt,
    status = MessageStatus.valueOf(status)
)
