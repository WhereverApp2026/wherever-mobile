package com.wherever.mobile.testing

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.friends.domain.FriendSuggestion
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.Friendship
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.FriendshipPage
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.domain.PersonDetail

/** Amigos en memoria para pruebas de ViewModel (nv-53, nv-18). */
class FakeFriendsRepository : FriendsRepository {
    val lists = mutableMapOf<FriendshipList, Result<FriendshipPage>>()
    var suggestionsResult: Result<List<FriendSuggestion>> = Result.Success(emptyList())
    var searchResult: Result<List<Person>> = Result.Success(emptyList())
    var codeResult: Result<PersonDetail> = Result.Failure(AppError.NotFound())
    var personResult: Result<PersonDetail> = Result.Failure(AppError.NotFound())
    var requestResult: Result<Friendship>? = null
    var actionResult: Result<Unit> = Result.Success(Unit)
    val calls = mutableListOf<String>()

    override suspend fun list(which: FriendshipList, query: String): Result<FriendshipPage> {
        calls += "list:${which.apiName}"
        return lists[which] ?: Result.Success(FriendshipPage(emptyList(), 0))
    }

    override suspend fun suggestions(): Result<List<FriendSuggestion>> = suggestionsResult

    override suspend fun search(query: String): Result<List<Person>> {
        calls += "search:$query"
        return searchResult
    }

    override suspend fun byCode(code: String): Result<PersonDetail> {
        calls += "code:$code"
        return codeResult
    }

    override suspend fun person(id: String): Result<PersonDetail> = personResult

    override suspend fun request(userId: String): Result<Friendship> {
        calls += "request:$userId"
        return requestResult
            ?: Result.Success(
                Friendship("f-$userId", Person(userId, "Persona", null, null), received = false, accepted = false)
            )
    }

    override suspend fun respond(friendshipId: String, accept: Boolean): Result<Unit> {
        calls += "respond:$friendshipId:$accept"
        return actionResult
    }

    override suspend fun remove(friendshipId: String): Result<Unit> {
        calls += "remove:$friendshipId"
        return actionResult
    }

    override suspend fun block(friendshipId: String): Result<Unit> {
        calls += "block:$friendshipId"
        return actionResult
    }
}
