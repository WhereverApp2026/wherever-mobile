package com.wherever.mobile.feature.feed.domain

import com.wherever.mobile.core.common.Result

/** Quien publicó el momento. El feed solo trae `id`, usuario y foto; el nombre sale de la lista de amigos. */
data class MomentAuthor(val id: String, val username: String?, val photoUrl: String?)

/** Momento de un amigo (backend nv-63): una postal con texto, lugar y plan opcionales. */
data class Moment(
    val id: String,
    val author: MomentAuthor,
    val imageUrl: String?,
    val text: String?,
    val placeId: String?,
    val planId: String?,
    val createdAt: String,
    val own: Boolean
)

data class MomentPage(val items: List<Moment>, val nextCursor: String?)

/** Feed de amigos de Inicio (specs/nv-17): `GET /momentos/feed`, `GET /momentos/{id}` y "Me apunto" a un plan. */
interface FeedRepository {
    suspend fun feed(cursor: String?): Result<MomentPage>

    suspend fun moment(id: String): Result<Moment>

    /** `POST /planes/{id}/me-apunto`: entra confirmado al grupo del plan público de un amigo. */
    suspend fun joinPlan(planId: String): Result<Unit>
}
