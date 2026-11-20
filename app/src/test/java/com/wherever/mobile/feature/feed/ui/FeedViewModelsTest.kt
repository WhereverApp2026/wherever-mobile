package com.wherever.mobile.feature.feed.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.feed.domain.FeedRepository
import com.wherever.mobile.feature.feed.domain.Moment
import com.wherever.mobile.feature.feed.domain.MomentAuthor
import com.wherever.mobile.feature.feed.domain.MomentPage
import com.wherever.mobile.feature.friends.domain.FriendSuggestion
import com.wherever.mobile.feature.friends.domain.Friendship
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.FriendshipPage
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.domain.PersonDetail
import com.wherever.mobile.feature.friends.domain.Relation
import com.wherever.mobile.testing.FakeFriendsRepository
import com.wherever.mobile.testing.FakePlacesRepository
import com.wherever.mobile.testing.FakePlansRepository
import com.wherever.mobile.testing.MainDispatcherRule
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeFeedRepository : FeedRepository {
    val pages = mutableMapOf<String?, Result<MomentPage>>()
    var momentResult: Result<Moment> = Result.Failure(AppError.NotFound())
    var joinResult: Result<Unit> = Result.Success(Unit)
    val calls = mutableListOf<String>()

    override suspend fun feed(cursor: String?): Result<MomentPage> {
        calls += "feed:$cursor"
        return pages[cursor] ?: Result.Success(MomentPage(emptyList(), null))
    }

    override suspend fun moment(id: String): Result<Moment> = momentResult

    override suspend fun joinPlan(planId: String): Result<Unit> {
        calls += "join:$planId"
        return joinResult
    }
}

private fun moment(id: String, author: String = "u1", place: String? = "p1", plan: String? = null) = Moment(
    id = id,
    author = MomentAuthor(author, "user_$author", null),
    imageUrl = "https://img/$id.jpg",
    text = "Texto $id",
    placeId = place,
    planId = plan,
    createdAt = "2026-11-20T10:00:00Z",
    own = false
)

private val NOW: Clock = Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC)

/** Pestaña Amigos, 17_PostDetalle y 29_PlanAmigo (specs/nv-17) con repositorios falsos. */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelsTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val feed = FakeFeedRepository()
    private val friends = FakeFriendsRepository()
    private val places = FakePlacesRepository()
    private val plans = FakePlansRepository()

    private fun feedViewModel() = FriendsFeedViewModel(feed, friends, places, NOW)

    @Test
    fun `no carga hasta que se muestra la pestaña y luego solo una vez`() = runTest(mainRule.dispatcher) {
        val vm = feedViewModel()
        advanceUntilIdle()
        assertTrue(feed.calls.isEmpty())

        vm.onShown()
        vm.onShown()
        advanceUntilIdle()

        assertEquals(listOf("feed:null"), feed.calls)
    }

    @Test
    fun `resuelve el nombre del amigo y el del lugar una sola vez por lugar`() = runTest(mainRule.dispatcher) {
        feed.pages[null] = Result.Success(MomentPage(listOf(moment("m1"), moment("m2")), nextCursor = null))
        friends.lists[FriendshipList.FRIENDS] = Result.Success(
            FriendshipPage(listOf(Friendship("f1", Person("u1", "Mariana", "mariana", null), false, true)), 0)
        )
        val vm = feedViewModel()
        vm.onShown()
        advanceUntilIdle()

        val items = vm.state.value.items
        assertEquals(listOf("Mariana", "Mariana"), items.map { it.authorName })
        assertEquals("Café Andrade", items.first().placeName)
        assertEquals(listOf("p1"), places.detailCalls)
        assertEquals(UiText.Res(R.string.feed_time_hours, listOf(2L)), items.first().time)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `sin momentos queda vacio y sin error`() = runTest(mainRule.dispatcher) {
        val vm = feedViewModel()
        vm.onShown()
        advanceUntilIdle()

        assertTrue(vm.state.value.empty)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `sin red muestra el error y reintentar vuelve a pedir el feed`() = runTest(mainRule.dispatcher) {
        feed.pages[null] = Result.Failure(AppError.Network)
        val vm = feedViewModel()
        vm.onShown()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.error_network), vm.state.value.error)
        assertFalse(vm.state.value.empty)

        feed.pages[null] = Result.Success(MomentPage(listOf(moment("m1")), null))
        vm.refresh()
        advanceUntilIdle()

        assertNull(vm.state.value.error)
        assertEquals(1, vm.state.value.items.size)
    }

    @Test
    fun `ver mas agrega la siguiente pagina`() = runTest(mainRule.dispatcher) {
        feed.pages[null] = Result.Success(MomentPage(listOf(moment("m1")), nextCursor = "c2"))
        feed.pages["c2"] = Result.Success(MomentPage(listOf(moment("m2", place = null)), nextCursor = null))
        val vm = feedViewModel()
        vm.onShown()
        advanceUntilIdle()

        vm.loadMore()
        advanceUntilIdle()

        assertEquals(listOf("m1", "m2"), vm.state.value.items.map { it.moment.id })
        assertNull(vm.state.value.nextCursor)
    }

    @Test
    fun `agregar una sugerencia pasa a pendiente y no se manda dos veces`() = runTest(mainRule.dispatcher) {
        val luis = Person("u9", "Luis", "luis", null)
        friends.suggestionsResult = Result.Success(listOf(FriendSuggestion(luis, 3)))
        val vm = feedViewModel()
        vm.onShown()
        advanceUntilIdle()
        assertEquals(1, vm.state.value.suggestions.size)

        vm.request(luis)
        vm.request(luis)
        advanceUntilIdle()

        assertEquals(1, friends.calls.count { it == "request:u9" })
        assertTrue("u9" in vm.state.value.sent)
    }

    @Test
    fun `publicacion borrada o sin permiso se marca como no disponible`() = runTest(mainRule.dispatcher) {
        val vm = MomentDetailViewModel(feed, friends, places, NOW, SavedStateHandle(mapOf("id" to "m1")))
        advanceUntilIdle()

        assertTrue(vm.state.value.gone)
        assertEquals(UiText.Res(R.string.feed_moment_gone), vm.state.value.error)
    }

    @Test
    fun `publicacion con autor y lugar resueltos`() = runTest(mainRule.dispatcher) {
        feed.momentResult = Result.Success(moment("m1", plan = "pl1"))
        friends.personResult = Result.Success(PersonDetail(Person("u1", "Mariana", "mariana", null), Relation.FRIEND))
        val vm = MomentDetailViewModel(feed, friends, places, NOW, SavedStateHandle(mapOf("id" to "m1")))
        advanceUntilIdle()

        val item = vm.state.value.item
        assertEquals("Mariana", item?.authorName)
        assertEquals("Café Andrade", item?.placeName)
        assertEquals("pl1", item?.moment?.planId)
    }

    private fun planViewModel() = FriendPlanViewModel(plans, feed, NOW, SavedStateHandle(mapOf("id" to "pl1")))

    @Test
    fun `me apunto confirma en la hoja y queda apuntado`() = runTest(mainRule.dispatcher) {
        val vm = planViewModel()
        advanceUntilIdle()
        vm.askJoin()
        assertTrue(vm.state.value.confirmJoin)

        vm.join()
        vm.join()
        advanceUntilIdle()

        assertEquals(1, feed.calls.count { it == "join:pl1" })
        assertTrue(vm.state.value.joined)
        assertFalse(vm.state.value.confirmJoin)
        assertEquals(UiText.Res(R.string.feed_joined), vm.state.value.message)
    }

    @Test
    fun `plan lleno explica el motivo`() = runTest(mainRule.dispatcher) {
        feed.joinResult = Result.Failure(AppError.Conflict())
        val vm = planViewModel()
        advanceUntilIdle()

        vm.join()
        advanceUntilIdle()

        assertFalse(vm.state.value.joined)
        assertEquals(UiText.Res(R.string.feed_plan_full), vm.state.value.message)
    }

    @Test
    fun `hacer mi version copia el plan para manana y abre la copia`() = runTest(mainRule.dispatcher) {
        val vm = planViewModel()
        advanceUntilIdle()

        vm.copy()
        advanceUntilIdle()

        assertEquals("2026-11-21", plans.clonedTo)
        assertEquals("plan2", vm.copied.first())
    }
}
