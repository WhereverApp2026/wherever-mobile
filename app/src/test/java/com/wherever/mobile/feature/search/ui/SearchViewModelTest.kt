package com.wherever.mobile.feature.search.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.friends.domain.Friendship
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.FriendshipPage
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.testing.FakeFriendsRepository
import com.wherever.mobile.testing.FakePlacesRepository
import com.wherever.mobile.testing.FakePlansRepository
import com.wherever.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-19: Buscar lugares, personas y planes. */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val places = FakePlacesRepository()
    private val friends = FakeFriendsRepository()
    private val plans = FakePlansRepository()

    private fun viewModel(query: String = "") =
        SearchViewModel(SavedStateHandle(mapOf("query" to query)), places, friends, plans)

    @Test
    fun `sin texto muestra recientes y no busca`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(listOf("tacos"), vm.state.value.recents)
        assertFalse(vm.state.value.hasQuery)
        assertTrue(places.searches.isEmpty())
    }

    @Test
    fun `escribir espera la pausa y busca lugares una sola vez`() = runTest {
        val vm = viewModel()
        vm.onQueryChange("ta")
        vm.onQueryChange("tac")
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS - 1)
        runCurrent()
        assertTrue(places.searches.isEmpty())

        advanceUntilIdle()

        assertEquals(listOf("tac"), places.searches)
        assertEquals(1, vm.state.value.places.size)
        assertFalse(vm.state.value.emptyResults)
    }

    @Test
    fun `sin resultados se marca para ofrecer la IA`() = runTest {
        places.searchResult = Result.Success(emptyList())
        val vm = viewModel(query = "sushi")
        advanceUntilIdle()

        assertTrue(vm.state.value.emptyResults)
    }

    @Test
    fun `personas agrega y pasa a pendiente`() = runTest {
        friends.searchResult = Result.Success(listOf(Person("u2", "Luz", "luz", null)))
        val vm = viewModel(query = "@luz")
        vm.onTab(SearchTab.PEOPLE)
        advanceUntilIdle()
        assertEquals("u2", vm.state.value.people.single().id)

        vm.request(vm.state.value.people.single())
        advanceUntilIdle()

        assertTrue("request:u2" in friends.calls)
        assertTrue("u2" in vm.state.value.sent)
    }

    @Test
    fun `personas marca amigos y solicitudes ya enviadas que la busqueda no trae`() = runTest {
        val luz = Person("u2", "Luz", "luz", null)
        val ana = Person("u3", "Ana", "luz_ana", null)
        friends.searchResult = Result.Success(listOf(luz, ana, Person("u4", "Luzma", "luzma", null)))
        val friend = Friendship("f1", luz, received = false, accepted = true)
        val request = Friendship("f2", ana, received = false, accepted = false)
        friends.lists[FriendshipList.FRIENDS] = Result.Success(FriendshipPage(listOf(friend), 0))
        friends.lists[FriendshipList.SENT] = Result.Success(FriendshipPage(listOf(request), 0))
        val vm = viewModel(query = "@luz")
        vm.onTab(SearchTab.PEOPLE)
        advanceUntilIdle()

        assertEquals(setOf("u2"), vm.state.value.friendIds)
        assertEquals(setOf("u3"), vm.state.value.sent)
    }

    @Test
    fun `personas sin poder leer amigos deja Agregar`() = runTest {
        friends.searchResult = Result.Success(listOf(Person("u2", "Luz", "luz", null)))
        friends.lists[FriendshipList.FRIENDS] = Result.Failure(AppError.Network)
        friends.lists[FriendshipList.SENT] = Result.Failure(AppError.Network)
        val vm = viewModel(query = "@luz")
        vm.onTab(SearchTab.PEOPLE)
        advanceUntilIdle()

        assertEquals("u2", vm.state.value.people.single().id)
        assertTrue(vm.state.value.friendIds.isEmpty())
        assertTrue(vm.state.value.sent.isEmpty())
    }

    @Test
    fun `personas con texto que la API rechaza no llama y muestra la ayuda`() = runTest {
        val vm = viewModel(query = "Tacos al pastor")
        vm.onTab(SearchTab.PEOPLE)
        advanceUntilIdle()

        assertTrue(vm.state.value.peopleQueryInvalid)
        assertTrue(friends.calls.none { it.startsWith("search") })
    }

    @Test
    fun `consulta de personas normaliza usuario y telefono`() {
        assertEquals("luz_m", peopleQuery("@Luz_M"))
        assertEquals("+525512345678", peopleQuery("+52 55 1234 5678"))
        assertEquals("5512345678", peopleQuery("55-1234-5678"))
        assertEquals(null, peopleQuery("lu"))
        assertEquals(null, peopleQuery("luz maria"))
    }

    @Test
    fun `planes filtra los propios por nombre sin acentos`() = runTest {
        val vm = viewModel(query = "CAFE")
        vm.onTab(SearchTab.PLANS)
        advanceUntilIdle()

        assertEquals(listOf("plan1"), vm.state.value.plans.map { it.id })
    }

    @Test
    fun `error de red se muestra y reintentar vuelve a buscar`() = runTest {
        places.searchResult = Result.Failure(AppError.Network)
        val vm = viewModel(query = "tacos")
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.error_network), vm.state.value.error)
        assertFalse(vm.state.value.emptyResults)

        places.searchResult = Result.Success(emptyList())
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.state.value.emptyResults)
    }

    @Test
    fun `borrar recientes vacia la lista`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.clearRecents()
        advanceUntilIdle()

        assertTrue(vm.state.value.recents.isEmpty())
        assertTrue(places.recentCleared)
    }
}
