package com.wherever.mobile.feature.feed.domain

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result

/** Estado de un comentario (backend nv-83): solo la autora ve los suyos en moderación o rechazados. */
enum class CommentState(val apiValue: String) {
    PUBLISHED("publicado"),
    IN_MODERATION("en_moderacion"),
    REJECTED("rechazado"),
    DELETED("eliminado");

    companion object {
        fun from(value: String?): CommentState = entries.firstOrNull { it.apiValue == value } ?: PUBLISHED
    }
}

data class CommentAuthor(val id: String, val name: String, val username: String?, val photoUrl: String?)

/** Comentario de primer nivel con sus respuestas (un solo nivel). `author` es nulo si está eliminado. */
data class Comment(
    val id: String,
    val author: CommentAuthor?,
    val parentId: String?,
    val text: String,
    val state: CommentState,
    val own: Boolean,
    val canDelete: Boolean,
    val createdAt: String,
    val replies: List<Comment> = emptyList()
)

/** Pestañas de 13_Reacciones. En los momentos «Van» siempre viene vacía. */
enum class ReactionTab(val apiValue: String) { ALL("todos"), LIKES("me_gusta"), GOING("van") }

data class ReactionPerson(
    val id: String,
    val name: String,
    val username: String?,
    val photoUrl: String?,
    /** `yo`, `amigo`, `solicitud_enviada`, `solicitud_recibida` o `ninguna`. */
    val relation: String,
    val mutualFriends: Int,
    val liked: Boolean,
    val going: Boolean
) {
    val isMe: Boolean get() = relation == "yo"
}

data class Reactions(val all: Int, val likes: Int, val going: Int, val people: List<ReactionPerson>) {
    /** Si yo di «me gusta»: la API me marca con la relación `yo`. */
    val likedByMe: Boolean get() = people.any { it.isMe && it.liked }
}

data class StoryMoment(
    val id: String,
    val imageUrl: String?,
    val placeName: String?,
    /** La autora hizo check-in en ese lugar ese día. */
    val verified: Boolean,
    val createdAt: String
)

/** Historias de una persona (últimas 24 h), de la más vieja a la más nueva. */
data class StoryPerson(
    val authorId: String,
    val username: String?,
    val photoUrl: String?,
    val moments: List<StoryMoment>
)

/** Me gusta, comentarios, reacciones e historias de los momentos (backend nv-83, nv-84). */
interface MomentSocialRepository {
    /** `PUT` o `DELETE /momentos/{id}/me-gusta`: idempotente, repetirlo no suma dos. */
    suspend fun setLiked(momentId: String, liked: Boolean): Result<Unit>

    suspend fun reactions(momentId: String, tab: ReactionTab = ReactionTab.ALL): Result<Reactions>

    suspend fun comments(momentId: String, cursor: String? = null): Result<PageResult<Comment>>

    /** El texto pasa por moderación: rechazado = error 422 sin guardar; dudoso = queda `en_moderacion`. */
    suspend fun addComment(momentId: String, text: String, parentId: String? = null): Result<Comment>

    suspend fun deleteComment(commentId: String): Result<Unit>

    suspend fun stories(): Result<List<StoryPerson>>
}
