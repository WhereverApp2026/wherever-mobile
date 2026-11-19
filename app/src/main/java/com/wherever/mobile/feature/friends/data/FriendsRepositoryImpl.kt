package com.wherever.mobile.feature.friends.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.friends.domain.FriendSuggestion
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.Friendship
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.FriendshipPage
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.domain.PersonDetail
import com.wherever.mobile.feature.friends.domain.Relation
import javax.inject.Inject

class FriendsRepositoryImpl @Inject constructor(private val api: FriendsApi) : FriendsRepository {
    override suspend fun list(which: FriendshipList, query: String): Result<FriendshipPage> = safeApiCall {
        api.list(which.apiName, query.trim().takeIf { it.isNotEmpty() })
    }.map { dto -> FriendshipPage(dto.results.map { it.toDomain() }, dto.pending) }

    override suspend fun suggestions(): Result<List<FriendSuggestion>> = safeApiCall {
        api.suggestions().results.map { FriendSuggestion(it.person.toDomain(), it.mutualFriends) }
    }

    override suspend fun search(query: String): Result<List<Person>> = safeApiCall {
        api.search(query.trim()).results.map { it.toDomain() }
    }

    override suspend fun byCode(code: String): Result<PersonDetail> = safeApiCall {
        api.byCode(code.trim().uppercase()).toDomain()
    }

    override suspend fun person(id: String): Result<PersonDetail> = safeApiCall { api.person(id).toDomain() }

    override suspend fun request(userId: String): Result<Friendship> = safeApiCall {
        api.request(FriendRequestDto(userId)).toDomain()
    }

    override suspend fun respond(friendshipId: String, accept: Boolean): Result<Unit> = safeApiCall {
        api.respond(friendshipId, RespondDto(if (accept) "aceptar" else "rechazar"))
    }.map { }

    override suspend fun remove(friendshipId: String): Result<Unit> = safeApiCall { api.remove(friendshipId) }

    override suspend fun block(friendshipId: String): Result<Unit> = safeApiCall { api.block(friendshipId) }
}

internal fun PersonDto.toDomain() = Person(
    id = id,
    name = name,
    username = username?.takeIf { it.isNotBlank() },
    photoUrl = photoUrl?.takeIf { it.isNotBlank() }
)

internal fun PersonDetailDto.toDomain() = PersonDetail(
    person = Person(id, name, username?.takeIf { it.isNotBlank() }, photoUrl?.takeIf { it.isNotBlank() }),
    relation = Relation.from(relation),
    level = level?.takeIf { it.isNotBlank() },
    mutualFriends = mutualFriends,
    mutualGroups = mutualGroups
)

internal fun FriendshipDto.toDomain() = Friendship(
    id = id,
    person = person.toDomain(),
    received = direction == "recibida",
    accepted = status == "aceptada"
)
