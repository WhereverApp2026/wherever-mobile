package com.wherever.mobile.feature.groups.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.groups.domain.Vote
import com.wherever.mobile.feature.groups.domain.VoteOption
import com.wherever.mobile.feature.groups.domain.VotesRepository
import javax.inject.Inject

class VotesRepositoryImpl @Inject constructor(private val api: GroupsApi) : VotesRepository {
    override suspend fun votes(groupId: String): Result<List<Vote>> = safeApiCall {
        api.votes(groupId).items.map { it.toVote() }
    }

    override suspend fun vote(voteId: String, placeId: String): Result<Vote> = safeApiCall {
        api.vote(voteId, CastVoteDto(placeId)).toVote()
    }

    override suspend fun closeVote(voteId: String): Result<Vote> = safeApiCall { api.closeVote(voteId).toVote() }
}

internal fun VoteDto.toVote() = Vote(
    id = id,
    stopId = stopId,
    options = options.map { VoteOption(it.placeId, it.place?.name?.takeIf(String::isNotBlank), it.votes) },
    total = total,
    myVote = myVote?.takeIf { it.isNotBlank() },
    closesAt = closesAt,
    open = status == "abierta",
    winner = winner?.takeIf { it.isNotBlank() },
    applied = applied
)
