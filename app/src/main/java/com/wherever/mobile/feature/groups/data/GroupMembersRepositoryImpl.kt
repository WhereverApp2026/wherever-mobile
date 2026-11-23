package com.wherever.mobile.feature.groups.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.groups.domain.Group
import com.wherever.mobile.feature.groups.domain.GroupMembersRepository
import javax.inject.Inject

/** Páginas de `GET /grupos` que se revisan para hallar el grupo de un plan (20 por página). */
private const val PLAN_LOOKUP_PAGES = 5

class GroupMembersRepositoryImpl @Inject constructor(private val api: GroupsApi, private val dao: GroupMessageDao) :
    GroupMembersRepository {
    override suspend fun create(planId: String, invitees: List<String>): Result<Group> = safeApiCall {
        api.create(CreateGroupDto(planId, invitees.distinct())).toGroup()
    }

    override suspend fun invite(groupId: String, userIds: List<String>): Result<Group> = safeApiCall {
        api.invite(groupId, InviteMembersDto(userIds.distinct())).toGroup()
    }

    override suspend fun inviteToPlan(planId: String, userIds: List<String>): Result<Group> = safeApiCall {
        val existing = groupOfPlan(planId)
        if (existing != null) {
            api.invite(existing.id, InviteMembersDto(userIds.distinct())).toGroup()
        } else {
            api.create(CreateGroupDto(planId, userIds.distinct())).toGroup()
        }
    }

    override suspend fun rename(group: Group, name: String): Result<Group> = safeApiCall {
        api.rename(group.id, "\"${group.version}\"", RenameGroupDto(name.trim())).toGroup()
    }

    override suspend fun leave(groupId: String): Result<Unit> = safeApiCall {
        api.leave(groupId)
        dao.clearGroup(groupId)
    }

    override suspend fun removeMember(groupId: String, userId: String): Result<Unit> = safeApiCall {
        api.removeMember(groupId, userId)
    }

    /** La API no busca grupos por plan: se recorren mis grupos, más recientes primero. */
    private suspend fun groupOfPlan(planId: String): GroupDto? {
        var cursor: String? = null
        var found: GroupDto? = null
        var pages = 0
        do {
            val page = api.list(cursor, GROUPS_PAGE)
            found = page.items.firstOrNull { it.planId == planId }
            cursor = page.nextCursor
            pages++
        } while (found == null && cursor != null && pages < PLAN_LOOKUP_PAGES)
        return found
    }
}
