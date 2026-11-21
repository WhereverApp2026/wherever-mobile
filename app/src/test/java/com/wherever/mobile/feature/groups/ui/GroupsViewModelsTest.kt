package com.wherever.mobile.feature.groups.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.groups.domain.MessageStatus
import com.wherever.mobile.testing.FakeGroupsRepository
import com.wherever.mobile.testing.FakePlansRepository
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleGroup
import com.wherever.mobile.testing.sampleMessage
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** nv-25 (Unit): 44_MisGrupos y 45_GrupoChat con carga, error, vacío, sin red y envío. */
@OptIn(ExperimentalCoroutinesApi::class)
class GroupsViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private lateinit var groups: FakeGroupsRepository
    private lateinit var plans: FakePlansRepository
    private lateinit var profiles: FakeProfileRepository

    @Before
    fun setUp() {
        groups = FakeGroupsRepository()
        plans = FakePlansRepository()
        profiles = FakeProfileRepository(sampleProfile())
    }

    private fun chat() = GroupChatViewModel(SavedStateHandle(mapOf("id" to "g1")), groups, plans, profiles)

    @Test
    fun `mis grupos carga, pagina y queda vacio`() = runTest {
        groups.groupsResult = Result.Success(PageResult(listOf(sampleGroup("g1")), "c2"))
        val vm = MyGroupsViewModel(groups)
        advanceUntilIdle()
        assertEquals(listOf("g1"), vm.state.value.groups.map { it.id })

        groups.groupsResult = Result.Success(PageResult(listOf(sampleGroup("g2"), sampleGroup("g1")), null))
        vm.loadMore()
        advanceUntilIdle()
        assertEquals(listOf("g1", "g2"), vm.state.value.groups.map { it.id })
        assertNull(vm.state.value.nextCursor)
        assertEquals(listOf(null, "c2"), groups.cursors)

        groups.groupsResult = Result.Success(PageResult(emptyList(), null))
        val empty = MyGroupsViewModel(groups)
        advanceUntilIdle()
        assertTrue(empty.state.value.empty)
    }

    @Test
    fun `mis grupos sin red muestra error y conserva la lista si ya habia`() = runTest {
        groups.groupsResult = Result.Failure(AppError.Network)
        val vm = MyGroupsViewModel(groups)
        advanceUntilIdle()
        assertNotNull(vm.state.value.loadError)

        groups.groupsResult = Result.Success(PageResult(listOf(sampleGroup()), null))
        vm.load()
        advanceUntilIdle()
        groups.groupsResult = Result.Failure(AppError.Network)
        vm.load()
        advanceUntilIdle()
        assertNull(vm.state.value.loadError)
        assertNotNull(vm.state.value.error)
        assertEquals(1, vm.state.value.groups.size)
    }

    @Test
    fun `aceptar une y rechazar quita el grupo`() = runTest {
        groups.groupsResult = Result.Success(
            PageResult(listOf(sampleGroup("g1", MemberStatus.INVITED), sampleGroup("g2", MemberStatus.INVITED)), null)
        )
        val vm = MyGroupsViewModel(groups)
        advanceUntilIdle()

        vm.respond(vm.state.value.groups[0], accept = true)
        advanceUntilIdle()
        assertEquals(MemberStatus.CONFIRMED, vm.state.value.groups[0].myStatus)
        assertEquals(UiText.Res(R.string.groups_joined), vm.state.value.message)

        vm.respond(vm.state.value.groups[1], accept = false)
        advanceUntilIdle()
        assertEquals(listOf("g1"), vm.state.value.groups.map { it.id })

        // Ya confirmado: no se vuelve a responder.
        vm.respond(vm.state.value.groups[0], accept = false)
        advanceUntilIdle()
        assertEquals(1, vm.state.value.groups.size)
    }

    @Test
    fun `chat carga grupo, plan y mensajes y distingue los mios`() = runTest {
        groups.messages.value = listOf(sampleMessage("2", author = "u1"), sampleMessage("1"))
        groups.refreshResult = Result.Success("older")

        val vm = chat()
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.loading)
        assertEquals("Tacos", state.title)
        assertNotNull(state.plan)
        assertEquals("u1", state.myId)
        assertEquals(2, state.messages.size)
        assertEquals("older", state.olderCursor)

        vm.loadOlder()
        advanceUntilIdle()
        assertEquals(listOf<String?>("older"), groups.cursors)
        assertNull(vm.state.value.olderCursor)
    }

    @Test
    fun `chat sin red lee Room con aviso y sin nada muestra error`() = runTest {
        groups.messages.value = listOf(sampleMessage("1"))
        groups.refreshResult = Result.Failure(AppError.Network)
        val vm = chat()
        advanceUntilIdle()
        assertTrue(vm.state.value.offline)
        assertNull(vm.state.value.loadError)

        groups.messages.value = emptyList()
        groups.groupResult = Result.Failure(AppError.Network)
        val down = chat()
        advanceUntilIdle()
        assertNotNull(down.state.value.loadError)
    }

    @Test
    fun `chat de un grupo que ya no es mio se cierra`() = runTest {
        groups.groupResult = Result.Failure(AppError.NotFound())
        val vm = chat()
        advanceUntilIdle()
        assertTrue(vm.state.value.gone)
    }

    @Test
    fun `enviar limpia el borrador, valida largo y reintenta el fallido`() = runTest {
        val vm = chat()
        advanceUntilIdle()

        vm.onDraft("   ")
        assertFalse(vm.state.value.canSend)
        vm.onDraft("x".repeat(MAX_MESSAGE_LENGTH + 5))
        assertEquals(MAX_MESSAGE_LENGTH, vm.state.value.draft.length)

        vm.onDraft("  Llevo el coche ")
        vm.send()
        advanceUntilIdle()
        assertEquals(listOf("Llevo el coche"), groups.sent)
        assertEquals("", vm.state.value.draft)

        groups.sendResult = Result.Failure(AppError.RateLimited())
        vm.onDraft("otra")
        vm.send()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.groups_chat_rate_limited), vm.state.value.error)

        vm.retry(sampleMessage("9", status = MessageStatus.FAILED, clientId = "c-9"))
        advanceUntilIdle()
        assertEquals(listOf("c-9"), groups.retried)
    }

    @Test
    fun `pregunta por mensajes nuevos solo mientras esta a la vista`() = runTest {
        val vm = chat()
        advanceUntilIdle()

        vm.startPolling()
        advanceTimeBy(5_001)
        runCurrent()
        assertEquals(1, groups.polls)
        vm.stopPolling()
        advanceTimeBy(20_000)
        runCurrent()
        assertEquals(1, groups.polls)
    }
}
