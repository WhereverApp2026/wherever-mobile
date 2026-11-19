package com.wherever.mobile.feature.friends.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.friends.domain.FriendSuggestion
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.Friendship
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.FriendshipPage
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.domain.PersonDetail
import com.wherever.mobile.feature.friends.domain.Relation
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private fun person(id: String, name: String = "Persona $id", username: String? = "user$id") =
    Person(id, name, username, null)

private fun friendship(id: String, personId: String, received: Boolean = false, accepted: Boolean = true) =
    Friendship(id, person(personId), received, accepted)

private class FakeFriendsRepository : FriendsRepository {
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
        return requestResult ?: Result.Success(friendship("f-$userId", userId, accepted = false))
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

/** nv-53: amigos, solicitudes, agregar y perfil de otra persona. */
@OptIn(ExperimentalCoroutinesApi::class)
class FriendsViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeFriendsRepository()

    @Test
    fun `lista de amigos se filtra sin acentos ni mayusculas`() = runTest(mainDispatcher.dispatcher) {
        repository.lists[FriendshipList.FRIENDS] = Result.Success(
            FriendshipPage(
                listOf(
                    Friendship("f1", person("1", "José Pérez", "pepe"), received = false, accepted = true),
                    Friendship("f2", person("2", "Ana", "ana_l"), received = false, accepted = true)
                ),
                pending = 2
            )
        )
        val vm = FriendsViewModel(repository)
        vm.load()
        advanceUntilIdle()
        assertEquals(2, vm.state.value.pending)

        vm.onQuery("JOSE")
        assertEquals(listOf("f1"), vm.state.value.visible.map { it.id })
        vm.onQuery("ana_")
        assertEquals(listOf("f2"), vm.state.value.visible.map { it.id })
    }

    @Test
    fun `aceptar quita la solicitud y baja el contador`() = runTest(mainDispatcher.dispatcher) {
        val request = friendship("f9", "9", received = true, accepted = false)
        repository.lists[FriendshipList.RECEIVED] = Result.Success(FriendshipPage(listOf(request), 1))
        val vm = FriendsViewModel(repository)
        vm.onTab(FriendshipList.RECEIVED)
        advanceUntilIdle()

        vm.accept(request)
        advanceUntilIdle()

        assertTrue("respond:f9:true" in repository.calls)
        assertTrue(vm.state.value.items.isEmpty())
        assertEquals(0, vm.state.value.pending)
    }

    @Test
    fun `si falla rechazar la solicitud se queda con el error`() = runTest(mainDispatcher.dispatcher) {
        val request = friendship("f9", "9", received = true, accepted = false)
        repository.lists[FriendshipList.RECEIVED] = Result.Success(FriendshipPage(listOf(request), 1))
        repository.actionResult = Result.Failure(AppError.Network)
        val vm = FriendsViewModel(repository)
        vm.onTab(FriendshipList.RECEIVED)
        advanceUntilIdle()

        vm.reject(request)
        advanceUntilIdle()

        assertEquals(1, vm.state.value.items.size)
        assertTrue(vm.state.value.error != null)
        assertTrue(vm.state.value.busy.isEmpty())
    }

    @Test
    fun `buscar pide al menos 3 letras y agregar marca enviada`() = runTest(mainDispatcher.dispatcher) {
        repository.searchResult = Result.Success(listOf(person("7")))
        val vm = AddFriendViewModel(repository, FakeProfileRepository(sampleProfile()))
        advanceUntilIdle()

        vm.onQuery("an")
        assertFalse(vm.state.value.canSearch)
        vm.onQuery("@ana")
        vm.search()
        advanceUntilIdle()
        assertEquals(listOf("7"), vm.state.value.results?.map { it.id })

        vm.request(person("7"))
        advanceUntilIdle()
        assertTrue("7" in vm.state.value.sent)
        assertEquals(UiText.Res(R.string.friends_request_sent, listOf("Persona 7")), vm.state.value.message)
    }

    @Test
    fun `409 al agregar cuenta como enviada sin error`() = runTest(mainDispatcher.dispatcher) {
        repository.requestResult = Result.Failure(AppError.Conflict("ya existe"))
        val vm = AddFriendViewModel(repository, FakeProfileRepository(sampleProfile()))
        advanceUntilIdle()

        vm.request(person("7"))
        advanceUntilIdle()

        assertTrue("7" in vm.state.value.sent)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `codigo invalido no llama y el inexistente avisa`() = runTest(mainDispatcher.dispatcher) {
        val vm = AddFriendViewModel(repository, FakeProfileRepository(sampleProfile()))
        advanceUntilIdle()

        vm.onCode("abc")
        vm.findByCode()
        assertEquals(UiText.Res(R.string.friends_code_invalid), vm.state.value.codeError)
        assertFalse(repository.calls.any { it.startsWith("code:") })

        vm.onCode("k7m2p9qx")
        vm.findByCode()
        advanceUntilIdle()
        assertTrue("code:K7M2P9QX" in repository.calls)
        assertEquals(UiText.Res(R.string.friends_code_not_found), vm.state.value.codeError)
    }

    @Test
    fun `nombre de usuario se normaliza y el ocupado avisa`() = runTest(mainDispatcher.dispatcher) {
        val profiles = FakeProfileRepository(sampleProfile())
        val vm = AddFriendViewModel(repository, profiles)
        advanceUntilIdle()

        vm.editUsername()
        vm.onUsername("@Ana_L")
        assertEquals("ana_l", vm.state.value.usernameDraft)
        profiles.usernameResult = Result.Failure(AppError.Conflict("repetido"))
        vm.saveUsername()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.friends_username_taken), vm.state.value.usernameError)

        profiles.usernameResult = null
        vm.saveUsername()
        advanceUntilIdle()
        assertFalse(vm.state.value.editingUsername)
        assertEquals("ana_l", vm.state.value.myUsername)
    }

    @Test
    fun `reglas de nombre de usuario y codigo`() {
        assertTrue(isValidUsername("@Ana_2026"))
        assertFalse(isValidUsername("an"))
        assertFalse(isValidUsername("ana lopez"))
        assertTrue(isValidFriendCode("k7m2p9qx"))
        // Sin 0, 1, I, L ni O, como el alfabeto del backend.
        assertFalse(isValidFriendCode("K7M2P9Q0"))
    }

    @Test
    fun `perfil de amigo encuentra la amistad y eliminar pide confirmacion`() = runTest(mainDispatcher.dispatcher) {
        repository.personResult = Result.Success(PersonDetail(person("5"), Relation.FRIEND, "Explorador", 3, 1))
        repository.lists[FriendshipList.FRIENDS] = Result.Success(FriendshipPage(listOf(friendship("f5", "5")), 0))
        val vm = PersonViewModel(SavedStateHandle(mapOf("id" to "5")), repository)
        advanceUntilIdle()
        assertEquals("f5", vm.state.value.friendshipId)

        vm.askRemove()
        assertEquals(PersonConfirm.REMOVE, vm.state.value.confirm)
        assertFalse(repository.calls.any { it.startsWith("remove:") })
        vm.confirm()
        advanceUntilIdle()
        assertTrue("remove:f5" in repository.calls)
    }

    @Test
    fun `bloquear cierra la pantalla`() = runTest(mainDispatcher.dispatcher) {
        repository.personResult = Result.Success(PersonDetail(person("5"), Relation.FRIEND))
        repository.lists[FriendshipList.FRIENDS] = Result.Success(FriendshipPage(listOf(friendship("f5", "5")), 0))
        val vm = PersonViewModel(SavedStateHandle(mapOf("id" to "5")), repository)
        advanceUntilIdle()

        vm.askBlock()
        vm.confirm()
        advanceUntilIdle()

        assertTrue("block:f5" in repository.calls)
        assertTrue(vm.state.value.blocked)
    }

    @Test
    fun `solicitud recibida se acepta desde el perfil y una persona inexistente da 404`() =
        runTest(mainDispatcher.dispatcher) {
            repository.personResult = Result.Success(PersonDetail(person("6"), Relation.REQUEST_RECEIVED))
            repository.lists[FriendshipList.RECEIVED] =
                Result.Success(FriendshipPage(listOf(friendship("f6", "6", received = true, accepted = false)), 1))
            val vm = PersonViewModel(SavedStateHandle(mapOf("id" to "6")), repository)
            advanceUntilIdle()

            vm.accept()
            advanceUntilIdle()
            assertTrue("respond:f6:true" in repository.calls)

            repository.personResult = Result.Failure(AppError.NotFound())
            val missing = PersonViewModel(SavedStateHandle(mapOf("id" to "x")), repository)
            advanceUntilIdle()
            assertEquals(UiText.Res(R.string.friends_person_not_found), missing.state.value.loadError)
        }
}
