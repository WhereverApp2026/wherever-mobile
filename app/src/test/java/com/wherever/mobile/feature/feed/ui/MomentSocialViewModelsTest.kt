package com.wherever.mobile.feature.feed.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.feed.domain.Comment
import com.wherever.mobile.feature.feed.domain.CommentAuthor
import com.wherever.mobile.feature.feed.domain.CommentState
import com.wherever.mobile.feature.feed.domain.MomentSocialRepository
import com.wherever.mobile.feature.feed.domain.ReactionPerson
import com.wherever.mobile.feature.feed.domain.ReactionTab
import com.wherever.mobile.feature.feed.domain.Reactions
import com.wherever.mobile.feature.feed.domain.StoryMoment
import com.wherever.mobile.feature.feed.domain.StoryPerson
import com.wherever.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private fun comment(
    id: String,
    parent: String? = null,
    state: CommentState = CommentState.PUBLISHED,
    own: Boolean = false
) = Comment(
    id,
    CommentAuthor("u$id", "Persona $id", null, null),
    parent,
    "texto $id",
    state,
    own,
    own,
    "2026-11-23T10:00:00Z"
)

private class FakeMomentSocial : MomentSocialRepository {
    var likeResult: Result<Unit> = Result.Success(Unit)
    var reactions =
        Reactions(1, 1, 0, listOf(ReactionPerson("yo", "Prueba", null, null, "yo", 0, liked = true, going = false)))
    var commentsResult: Result<PageResult<Comment>> = Result.Success(PageResult(listOf(comment("c1")), null))
    var addResult: Result<Comment>? = null
    var stories: List<StoryPerson> = emptyList()
    val calls = mutableListOf<String>()

    override suspend fun setLiked(momentId: String, liked: Boolean): Result<Unit> {
        calls += "like:$liked"
        return likeResult
    }

    override suspend fun reactions(momentId: String, tab: ReactionTab): Result<Reactions> {
        calls += "reactions:${tab.apiValue}"
        return Result.Success(reactions)
    }

    override suspend fun comments(momentId: String, cursor: String?): Result<PageResult<Comment>> = commentsResult

    override suspend fun addComment(momentId: String, text: String, parentId: String?): Result<Comment> {
        calls += "add:$text:$parentId"
        return addResult ?: Result.Success(comment("new", parentId, own = true).copy(text = text))
    }

    override suspend fun deleteComment(commentId: String): Result<Unit> {
        calls += "delete:$commentId"
        return Result.Success(Unit)
    }

    override suspend fun stories(): Result<List<StoryPerson>> = Result.Success(stories)
}

/** nv-17 (Unit): me gusta, comentarios, reacciones e historias. */
@OptIn(ExperimentalCoroutinesApi::class)
class MomentSocialViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val social = FakeMomentSocial()

    private fun vm() = MomentSocialViewModel(SavedStateHandle(mapOf("id" to "m1")), social)

    @Test
    fun `sabe si yo di me gusta y lo quita al instante`() = runTest {
        val vm = vm()
        advanceUntilIdle()
        assertTrue(vm.state.value.liked)
        assertEquals(1, vm.state.value.likes)

        vm.toggleLike()
        assertFalse("optimista", vm.state.value.liked)
        assertEquals(0, vm.state.value.likes)
        advanceUntilIdle()
        assertTrue("like:false" in social.calls)
    }

    @Test
    fun `si la API falla el corazon regresa y avisa`() = runTest {
        social.reactions = Reactions(0, 0, 0, emptyList())
        val vm = vm()
        advanceUntilIdle()
        social.likeResult = Result.Failure(AppError.Network)
        vm.toggleLike()
        advanceUntilIdle()
        assertFalse(vm.state.value.liked)
        assertEquals(0, vm.state.value.likes)
        assertEquals(UiText.Res(R.string.error_network), vm.state.value.error)
    }

    @Test
    fun `comenta, responde dentro del comentario y borra`() = runTest {
        val vm = vm()
        advanceUntilIdle()
        assertFalse(vm.state.value.canSend)
        vm.onDraft("Muy bueno")
        vm.send()
        advanceUntilIdle()
        assertEquals(listOf("c1", "new"), vm.state.value.comments.map { it.id })
        assertEquals("", vm.state.value.draft)

        vm.replyTo(vm.state.value.comments.first())
        vm.onDraft("Gracias")
        vm.send()
        advanceUntilIdle()
        assertTrue("add:Gracias:c1" in social.calls)
        assertEquals(1, vm.state.value.comments.first().replies.size)
        assertNull(vm.state.value.replyTo)
        assertEquals("el conteo sube con cada comentario", 2, vm.state.value.commentsDelta)

        vm.delete(vm.state.value.comments.last())
        advanceUntilIdle()
        assertEquals(listOf("c1"), vm.state.value.comments.map { it.id })
        assertEquals("y baja al borrar", 1, vm.state.value.commentsDelta)
        assertEquals(UiText.Res(R.string.comment_deleted), vm.state.value.message)
    }

    @Test
    fun `comentario en moderacion avisa y el texto tiene limite`() = runTest {
        social.addResult = Result.Success(comment("m", state = CommentState.IN_MODERATION, own = true))
        val vm = vm()
        advanceUntilIdle()
        vm.onDraft("x".repeat(MAX_COMMENT + 10))
        assertEquals(MAX_COMMENT, vm.state.value.draft.length)
        vm.send()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.comment_in_review), vm.state.value.message)
    }

    @Test
    fun `si la API rechaza el texto muestra su motivo y no cuenta el comentario`() = runTest {
        social.addResult = Result.Failure(
            AppError.Validation(listOf(FieldError("texto", "el texto no cumple las normas de la comunidad")))
        )
        val vm = vm()
        advanceUntilIdle()
        vm.onDraft("Si verdad")
        vm.send()
        advanceUntilIdle()
        assertEquals(UiText.Plain("El texto no cumple las normas de la comunidad"), vm.state.value.error)
        assertEquals(0, vm.state.value.commentsDelta)
        assertEquals("Si verdad", vm.state.value.draft)
    }

    @Test
    fun `no se borra un comentario ajeno`() = runTest {
        val vm = vm()
        advanceUntilIdle()
        vm.delete(vm.state.value.comments.single())
        advanceUntilIdle()
        assertFalse(social.calls.any { it.startsWith("delete") })
    }

    @Test
    fun `la hoja de reacciones carga la pestana elegida y se cierra`() = runTest {
        val vm = vm()
        advanceUntilIdle()
        vm.openReactions(ReactionTab.ALL)
        advanceUntilIdle()
        assertEquals(ReactionTab.ALL, vm.state.value.sheet?.tab)
        assertEquals(1, vm.state.value.sheet?.data?.all)
        vm.closeReactions()
        assertNull(vm.state.value.sheet)
    }

    @Test
    fun `el visor empieza en la persona elegida, avanza y termina`() = runTest {
        social.stories = listOf(
            StoryPerson(
                "u1",
                "ana",
                null,
                listOf(StoryMoment("a1", null, null, false, ""), StoryMoment("a2", null, null, false, ""))
            ),
            StoryPerson("u2", "luis", null, listOf(StoryMoment("l1", null, null, false, "")))
        )
        val viewer = StoryViewerViewModel(SavedStateHandle(mapOf("authorId" to "u2")), social)
        advanceUntilIdle()
        assertEquals("l1", viewer.state.value.currentMoment?.id)
        viewer.previous()
        assertEquals("a2", viewer.state.value.currentMoment?.id)
        viewer.next()
        viewer.next()
        assertTrue(viewer.state.value.finished)

        val row = StoriesViewModel(social)
        advanceUntilIdle()
        assertEquals(listOf("u1", "u2"), row.state.value.people.map { it.authorId })
    }
}
