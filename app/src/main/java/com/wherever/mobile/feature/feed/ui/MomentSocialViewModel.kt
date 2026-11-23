package com.wherever.mobile.feature.feed.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.feed.domain.Comment
import com.wherever.mobile.feature.feed.domain.CommentState
import com.wherever.mobile.feature.feed.domain.MomentSocialRepository
import com.wherever.mobile.feature.feed.domain.ReactionTab
import com.wherever.mobile.feature.feed.domain.Reactions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Límite de la API para un comentario (backend nv-83). */
const val MAX_COMMENT = 300

/** La hoja 13_Reacciones abierta en una pestaña. */
data class ReactionsSheet(
    val tab: ReactionTab,
    val loading: Boolean = true,
    val data: Reactions? = null,
    val error: UiText? = null
)

data class MomentSocialUiState(
    val liked: Boolean = false,
    /** Conteo de me gusta; arranca con el del momento y se ajusta al tocar el corazón. */
    val likes: Int? = null,
    val liking: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val commentsLoading: Boolean = true,
    val commentsCursor: String? = null,
    val commentsError: UiText? = null,
    val draft: String = "",
    val replyTo: Comment? = null,
    val sending: Boolean = false,
    val error: UiText? = null,
    val message: UiText? = null,
    val sheet: ReactionsSheet? = null
) {
    val canSend: Boolean
        get() = draft.isNotBlank() && draft.trim().length <= MAX_COMMENT && !sending
}

/** Me gusta, comentarios y 13_Reacciones de 17_PostDetalle (backend nv-83). */
@HiltViewModel
class MomentSocialViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val social: MomentSocialRepository
) : ViewModel() {
    private val momentId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(MomentSocialUiState())
    val state: StateFlow<MomentSocialUiState> = mutableState.asStateFlow()

    init {
        loadComments()
        // La API no dice en el momento si yo di me gusta: se lee de las reacciones (me marca con `yo`).
        viewModelScope.launch {
            (social.reactions(momentId, ReactionTab.LIKES) as? Result.Success)?.data?.let { reactions ->
                mutableState.update { it.copy(liked = reactions.likedByMe, likes = reactions.likes) }
            }
        }
    }

    /** El conteo del momento mientras llegan las reacciones. */
    fun seedLikes(count: Int) = mutableState.update { if (it.likes == null) it.copy(likes = count) else it }

    /** Optimista: cambia el corazón y el conteo al instante; si la API falla, lo regresa. */
    fun toggleLike() {
        val current = state.value
        if (current.liking) return
        val liked = !current.liked
        val delta = if (liked) 1 else -1
        mutableState.update {
            it.copy(liked = liked, likes = ((it.likes ?: 0) + delta).coerceAtLeast(0), liking = true)
        }
        viewModelScope.launch {
            val result = social.setLiked(momentId, liked)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(liking = false)
                    is Result.Failure -> it.copy(
                        liking = false,
                        liked = !liked,
                        likes = ((it.likes ?: 0) - delta).coerceAtLeast(0),
                        error = result.error.toUiText()
                    )
                }
            }
        }
    }

    fun loadComments() {
        mutableState.update { it.copy(commentsLoading = it.comments.isEmpty(), commentsError = null) }
        viewModelScope.launch {
            val result = social.comments(momentId)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(
                        commentsLoading = false,
                        comments = result.data.items,
                        commentsCursor = result.data.nextCursor
                    )
                    is Result.Failure -> it.copy(commentsLoading = false, commentsError = result.error.toUiText())
                }
            }
        }
    }

    fun loadMoreComments() {
        val cursor = state.value.commentsCursor ?: return
        viewModelScope.launch {
            (social.comments(momentId, cursor) as? Result.Success)?.data?.let { page ->
                mutableState.update {
                    it.copy(
                        comments = (it.comments + page.items).distinctBy(Comment::id),
                        commentsCursor = page.nextCursor
                    )
                }
            }
        }
    }

    fun onDraft(value: String) = mutableState.update { it.copy(draft = value.take(MAX_COMMENT), error = null) }

    fun replyTo(comment: Comment) = mutableState.update { it.copy(replyTo = comment) }

    fun cancelReply() = mutableState.update { it.copy(replyTo = null) }

    fun send() {
        val current = state.value
        if (!current.canSend) return
        mutableState.update { it.copy(sending = true, error = null) }
        viewModelScope.launch {
            val result = social.addComment(momentId, current.draft, current.replyTo?.id)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(
                        sending = false,
                        draft = "",
                        replyTo = null,
                        comments = it.comments.withComment(result.data),
                        message = if (result.data.state == CommentState.IN_MODERATION) {
                            UiText.Res(R.string.comment_in_review)
                        } else {
                            null
                        }
                    )
                    is Result.Failure -> it.copy(sending = false, error = result.error.toUiText())
                }
            }
        }
    }

    fun delete(comment: Comment) {
        if (!comment.canDelete) return
        viewModelScope.launch {
            when (val result = social.deleteComment(comment.id)) {
                is Result.Success -> mutableState.update {
                    it.copy(comments = it.comments.without(comment.id), message = UiText.Res(R.string.comment_deleted))
                }
                is Result.Failure -> mutableState.update { it.copy(error = result.error.toUiText()) }
            }
        }
    }

    fun openReactions(tab: ReactionTab = ReactionTab.ALL) {
        mutableState.update { it.copy(sheet = ReactionsSheet(tab)) }
        viewModelScope.launch {
            val result = social.reactions(momentId, tab)
            mutableState.update {
                if (it.sheet?.tab != tab) return@update it
                when (result) {
                    is Result.Success -> it.copy(sheet = ReactionsSheet(tab, loading = false, data = result.data))
                    is Result.Failure -> it.copy(
                        sheet = ReactionsSheet(tab, loading = false, error = result.error.toUiText())
                    )
                }
            }
        }
    }

    fun closeReactions() = mutableState.update { it.copy(sheet = null) }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}

/** Un comentario nuevo va al final; una respuesta, dentro de su comentario. */
internal fun List<Comment>.withComment(comment: Comment): List<Comment> {
    val parent = comment.parentId ?: return this + comment
    return map { if (it.id == parent) it.copy(replies = it.replies + comment) else it }
}

/** Quita un comentario o una respuesta. */
internal fun List<Comment>.without(id: String): List<Comment> =
    filterNot { it.id == id }.map { c -> c.copy(replies = c.replies.filterNot { it.id == id }) }
