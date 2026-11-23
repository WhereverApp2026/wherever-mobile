package com.wherever.mobile.feature.friends.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.friends.domain.ContactMatch
import com.wherever.mobile.feature.friends.domain.ContactMatchRepository
import com.wherever.mobile.feature.friends.domain.ContactsReader
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.domain.Relation
import com.wherever.mobile.testing.FakeFriendsRepository
import com.wherever.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeContacts(var phones: List<String>) : ContactsReader {
    var reads = 0
    override suspend fun phoneNumbers(): List<String> {
        reads++
        return phones
    }
}

private class FakeMatcher : ContactMatchRepository {
    var result: Result<List<ContactMatch>> = Result.Success(emptyList())
    val sent = mutableListOf<List<String>>()
    override suspend fun match(phones: List<String>): Result<List<ContactMatch>> {
        sent += phones
        return result
    }
}

/** nv-20 (07_EncuentraAmigos): permiso en contexto, coincidencias y Agregar → Pendiente. */
@OptIn(ExperimentalCoroutinesApi::class)
class FindFriendsViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val contacts = FakeContacts(listOf("7731234567", "+527739876543"))
    private val matcher = FakeMatcher()
    private val friends = FakeFriendsRepository()
    private val mariana = ContactMatch(Person("u1", "Mariana", "mariana", null), Relation.NONE)

    private fun viewModel() = FindFriendsViewModel(contacts, matcher, friends)

    @Test
    fun `sin permiso no lee la agenda y avisa`() = runTest {
        val vm = viewModel()
        vm.onPermission(false)
        advanceUntilIdle()
        assertTrue(vm.state.value.permissionDenied)
        assertEquals(0, contacts.reads)
        assertNull(vm.state.value.matches)
    }

    @Test
    fun `con permiso manda los telefonos y muestra quienes ya estan`() = runTest {
        matcher.result = Result.Success(listOf(mariana))
        val vm = viewModel()
        vm.onPermission(true)
        advanceUntilIdle()
        assertEquals(listOf(listOf("7731234567", "+527739876543")), matcher.sent)
        assertEquals(listOf(mariana), vm.state.value.matches)
    }

    @Test
    fun `agenda vacia no llama a la API y queda en vacio`() = runTest {
        contacts.phones = emptyList()
        val vm = viewModel()
        vm.search()
        advanceUntilIdle()
        assertTrue(matcher.sent.isEmpty())
        assertEquals(emptyList<ContactMatch>(), vm.state.value.matches)
    }

    @Test
    fun `agregar pasa a Pendiente y un 409 tambien`() = runTest {
        val luis = ContactMatch(Person("u2", "Luis", null, null), Relation.NONE)
        matcher.result = Result.Success(listOf(mariana, luis))
        val vm = viewModel()
        vm.search()
        advanceUntilIdle()

        vm.request(mariana)
        advanceUntilIdle()
        friends.requestResult = Result.Failure(AppError.Conflict())
        vm.request(luis)
        advanceUntilIdle()

        assertEquals(listOf(Relation.REQUEST_SENT, Relation.REQUEST_SENT), vm.state.value.matches!!.map { it.relation })
        assertEquals(listOf("request:u1", "request:u2"), friends.calls)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `429 explica el limite diario`() = runTest {
        matcher.result = Result.Failure(AppError.RateLimited(retryAfterSeconds = 60))
        val vm = viewModel()
        vm.search()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.find_friends_rate_limited), vm.state.value.error)
    }
}
