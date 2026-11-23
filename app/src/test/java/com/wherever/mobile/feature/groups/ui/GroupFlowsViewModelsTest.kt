package com.wherever.mobile.feature.groups.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.friends.domain.Friendship
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.FriendshipPage
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.groups.domain.GroupMember
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.testing.FakeFriendsRepository
import com.wherever.mobile.testing.FakeGroupsRepository
import com.wherever.mobile.testing.FakePlansRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleGroup
import com.wherever.mobile.testing.sampleVote
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** nv-25 (Unit): 46_Grupo, 48_Votar, 43_CrearGrupo e InvitarPlan. */
@OptIn(ExperimentalCoroutinesApi::class)
class GroupFlowsViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private lateinit var groups: FakeGroupsRepository
    private lateinit var plans: FakePlansRepository
    private lateinit var friends: FakeFriendsRepository
    private val clock = Clock.fixed(Instant.parse("2026-11-23T18:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        groups = FakeGroupsRepository()
        plans = FakePlansRepository()
        friends = FakeFriendsRepository()
    }

    private fun friend(id: String, name: String) = Friendship("f$id", Person(id, name, null, null), false, true)

    private fun organizerGroup() = sampleGroup().copy(
        iAmOrganizer = true,
        members = listOf(
            GroupMember("yo", "Jorge", null, true, MemberStatus.CONFIRMED),
            GroupMember("u2", "Luis Pérez", null, false, MemberStatus.INVITED),
            GroupMember("u3", "Eva", null, false, MemberStatus.DECLINED)
        )
    )

    @Test
    fun `grupo carga plan y votaciones, oculta a quien declino y pone al organizador primero`() = runTest {
        groups.groupResult = Result.Success(organizerGroup())
        groups.votesResult = Result.Success(listOf(sampleVote("v1"), sampleVote("v2", open = false)))
        val vm = GroupDetailViewModel(SavedStateHandle(mapOf("id" to "g1")), groups, groups, groups, plans)
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.loading)
        assertEquals("Tarde de café", state.plan?.name)
        assertEquals(1, state.openVotes)
        assertEquals(listOf("yo", "u2"), state.members.map { it.userId })
    }

    @Test
    fun `grupo ajeno o borrado cierra la pantalla`() = runTest {
        groups.groupResult = Result.Failure(AppError.NotFound())
        val vm = GroupDetailViewModel(SavedStateHandle(mapOf("id" to "g1")), groups, groups, groups, plans)
        advanceUntilIdle()
        assertTrue(vm.state.value.gone)
    }

    @Test
    fun `el organizador renombra con limite y saca a un integrante`() = runTest {
        groups.groupResult = Result.Success(organizerGroup())
        val vm = GroupDetailViewModel(SavedStateHandle(mapOf("id" to "g1")), groups, groups, groups, plans)
        advanceUntilIdle()

        vm.startRename()
        vm.onName("x".repeat(MAX_GROUP_NAME + 5))
        assertEquals(MAX_GROUP_NAME, vm.state.value.nameDraft?.length)
        vm.onName("  ")
        assertFalse(vm.state.value.canSaveName)
        vm.onName("Los del domingo")
        vm.saveName()
        advanceUntilIdle()
        assertEquals("Los del domingo", vm.state.value.group?.name)
        assertNull(vm.state.value.nameDraft)

        vm.askRemove(vm.state.value.members.last())
        vm.remove()
        advanceUntilIdle()
        assertTrue("remove:u2" in groups.calls)
        assertEquals(listOf("yo", "u3"), vm.state.value.group?.members?.map { it.userId })
    }

    @Test
    fun `renombrar con version vieja avisa y recarga`() = runTest {
        groups.groupResult = Result.Success(organizerGroup())
        val vm = GroupDetailViewModel(SavedStateHandle(mapOf("id" to "g1")), groups, groups, groups, plans)
        advanceUntilIdle()
        groups.renameResult = Result.Failure(AppError.Conflict())
        vm.startRename()
        vm.onName("Nuevo")
        vm.saveName()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.group_detail_conflict), vm.state.value.error)
    }

    @Test
    fun `quien no organiza no renombra y al salir se cierra`() = runTest {
        val vm = GroupDetailViewModel(SavedStateHandle(mapOf("id" to "g1")), groups, groups, groups, plans)
        advanceUntilIdle()
        vm.startRename()
        assertNull(vm.state.value.nameDraft)

        vm.askLeave()
        assertTrue(vm.state.value.confirmLeave)
        vm.leave()
        advanceUntilIdle()
        assertTrue("leave:g1" in groups.calls)
        assertTrue(vm.state.value.gone)
    }

    @Test
    fun `votar exige elegir otra opcion y registra el voto`() = runTest {
        groups.votesResult = Result.Success(listOf(sampleVote("v1", myVote = "n1")))
        val vm = VoteViewModel(SavedStateHandle(mapOf("id" to "g1")), groups, groups, clock)
        advanceUntilIdle()
        val vote = vm.state.value.votes.single()

        assertFalse(vm.state.value.canSend(vote))
        vm.select(vote, "n1")
        assertFalse(vm.state.value.canSend(vote))
        vm.select(vote, "n2")
        assertTrue(vm.state.value.canSend(vote))
        vm.send(vote)
        advanceUntilIdle()

        assertTrue("vote:v1:n2" in groups.calls)
        assertEquals("n2", vm.state.value.votes.single().myVote)
        assertEquals(UiText.Res(R.string.vote_sent), vm.state.value.message)
    }

    @Test
    fun `una votacion vencida segun el reloj no deja votar`() = runTest {
        groups.votesResult = Result.Success(listOf(sampleVote("v1", closesAt = "2026-11-23T17:59:00Z")))
        val vm = VoteViewModel(SavedStateHandle(mapOf("id" to "g1")), groups, groups, clock)
        advanceUntilIdle()
        val vote = vm.state.value.votes.single()
        assertFalse(vm.state.value.isOpen(vote))
        vm.select(vote, "n2")
        assertTrue(vm.state.value.selected.isEmpty())
    }

    @Test
    fun `votacion cerrada en el servidor avisa y recarga`() = runTest {
        val vm = VoteViewModel(SavedStateHandle(mapOf("id" to "g1")), groups, groups, clock)
        advanceUntilIdle()
        groups.voteResult = Result.Failure(AppError.Conflict())
        val vote = vm.state.value.votes.single()
        vm.select(vote, "n2")
        vm.send(vote)
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.vote_closed_now), vm.state.value.error)
    }

    @Test
    fun `solo el organizador cierra la votacion`() = runTest {
        val vm = VoteViewModel(SavedStateHandle(mapOf("id" to "g1")), groups, groups, clock)
        advanceUntilIdle()
        vm.close(vm.state.value.votes.single())
        assertFalse(groups.calls.any { it.startsWith("close") })

        groups.groupResult = Result.Success(organizerGroup())
        vm.load()
        advanceUntilIdle()
        vm.close(vm.state.value.votes.single())
        advanceUntilIdle()
        assertTrue("close:v1" in groups.calls)
        assertFalse(vm.state.value.votes.single().open)
        assertEquals(UiText.Res(R.string.vote_closed), vm.state.value.message)
    }

    @Test
    fun `crear grupo elige amigos, filtra y nombra el grupo`() = runTest {
        friends.lists[FriendshipList.FRIENDS] = Result.Success(
            FriendshipPage(listOf(friend("u2", "Luis"), friend("u1", "Ana")), 0)
        )
        val vm = CreateGroupViewModel(SavedStateHandle(mapOf("planId" to "plan1")), groups, groups, friends, plans)
        advanceUntilIdle()

        assertEquals(listOf("Ana", "Luis"), vm.state.value.friends.map { it.name })
        assertFalse(vm.state.value.canSubmit)
        vm.onQuery("lu")
        assertEquals(listOf("u2"), vm.state.value.visibleFriends.map { it.id })
        vm.toggle(vm.state.value.visibleFriends.single())
        vm.onName("Tacos")
        vm.submit()
        val done = vm.events.first()

        assertEquals(CreateGroupEvent.Done("new"), done)
        assertEquals(listOf("create:plan1:u2", "rename:Tacos"), groups.calls)
    }

    @Test
    fun `invitar a un grupo no repite integrantes ni pasa de 11`() = runTest {
        groups.groupResult = Result.Success(organizerGroup())
        friends.lists[FriendshipList.FRIENDS] = Result.Success(
            FriendshipPage((1..14).map { friend("u$it", "Amigo $it") }, 0)
        )
        val vm = CreateGroupViewModel(
            SavedStateHandle(mapOf("planId" to "plan1", "groupId" to "g1")),
            groups,
            groups,
            friends,
            plans
        )
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state.inviting)
        assertFalse(state.canPick("u2"))
        assertEquals(MAX_INVITEES - 2, state.seatsLeft)
        state.friends.forEach(vm::toggle)
        assertEquals(state.seatsLeft, vm.state.value.selected.size)
        vm.submit()
        advanceUntilIdle()
        assertTrue(groups.calls.single().startsWith("invite:g1:"))
    }

    @Test
    fun `invitar a un plan elige uno y envia una sola vez`() = runTest {
        val vm = InvitePlanViewModel(
            SavedStateHandle(mapOf("personId" to "u7", "personName" to "Mariana")),
            groups,
            plans
        )
        advanceUntilIdle()
        assertEquals("Mariana", vm.state.value.personName)
        assertFalse(vm.state.value.canSend)

        vm.select("plan1")
        vm.send()
        advanceUntilIdle()
        assertEquals("g9", vm.state.value.sentToGroup)
        assertEquals(listOf("inviteToPlan:plan1:u7"), groups.calls)
        vm.send()
        advanceUntilIdle()
        assertEquals(1, groups.calls.size)
    }

    @Test
    fun `invitar a un plan muestra el error de la API`() = runTest {
        groups.inviteResult = Result.Failure(AppError.Conflict("El grupo ya está lleno"))
        val vm = InvitePlanViewModel(SavedStateHandle(mapOf("personId" to "u7")), groups, plans)
        advanceUntilIdle()
        vm.select("plan1")
        vm.send()
        advanceUntilIdle()
        assertEquals(UiText.Plain("El grupo ya está lleno"), vm.state.value.error)
        assertNull(vm.state.value.sentToGroup)
    }
}
