package com.wherever.mobile.feature.notifications.ui

import app.cash.turbine.test
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.notifications.PushDestination
import com.wherever.mobile.feature.notifications.domain.AppNotification
import com.wherever.mobile.feature.notifications.domain.NotificationPage
import com.wherever.mobile.feature.notifications.domain.NotificationType
import com.wherever.mobile.feature.notifications.domain.NotificationsRepository
import com.wherever.mobile.navigation.NotificationsRoute
import com.wherever.mobile.navigation.PlanRoute
import com.wherever.mobile.navigation.route
import com.wherever.mobile.testing.MainDispatcherRule
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private fun notification(
    id: String,
    type: NotificationType = NotificationType.FRIEND_REQUEST,
    read: Boolean = false,
    ids: Map<String, String> = mapOf("usuario_id" to "u$id")
) = AppNotification(id, type, type.apiName, ids, read, "2026-11-19T08:00:00Z")

private class FakeNotificationsRepository : NotificationsRepository {
    val pages = mutableMapOf<String?, Result<NotificationPage>>()
    var unread: Result<Int> = Result.Success(0)
    var markResult: Result<Unit> = Result.Success(Unit)
    var markAllResult: Result<Unit> = Result.Success(Unit)
    var deleteResult: Result<Unit> = Result.Success(Unit)
    val calls = mutableListOf<String>()

    override suspend fun page(cursor: String?): Result<NotificationPage> {
        calls += "page:$cursor"
        return pages[cursor] ?: Result.Success(NotificationPage(emptyList(), null))
    }

    override suspend fun unreadCount(): Result<Int> = unread

    override suspend fun markRead(id: String): Result<Unit> {
        calls += "read:$id"
        return markResult
    }

    override suspend fun markAllRead(): Result<Unit> {
        calls += "readAll"
        return markAllResult
    }

    override suspend fun delete(id: String): Result<Unit> {
        calls += "delete:$id"
        return deleteResult
    }
}

/** nv-52: lista de avisos, filtros, leídas, borrar y destino al tocar. */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeNotificationsRepository()
    private val clock: Clock = Clock.fixed(Instant.parse("2026-11-19T18:00:00Z"), ZoneId.of("America/Mexico_City"))

    private fun withFirstPage(vararg items: AppNotification, next: String? = null) {
        repository.pages[null] = Result.Success(NotificationPage(items.toList(), next))
    }

    @Test
    fun `carga la primera pagina y luego la siguiente sin repetir`() = runTest(mainDispatcher.dispatcher) {
        withFirstPage(notification("1"), notification("2"), next = "c2")
        repository.pages["c2"] = Result.Success(NotificationPage(listOf(notification("2"), notification("3")), null))
        val vm = NotificationsViewModel(repository, clock)
        advanceUntilIdle()
        assertEquals(listOf("1", "2"), vm.state.value.items.map { it.id })
        assertEquals("c2", vm.state.value.nextCursor)

        vm.loadMore()
        advanceUntilIdle()

        assertEquals(listOf("1", "2", "3"), vm.state.value.items.map { it.id })
        assertNull(vm.state.value.nextCursor)
    }

    @Test
    fun `falla inicial muestra error y reintentar vuelve a pedir`() = runTest(mainDispatcher.dispatcher) {
        repository.pages[null] = Result.Failure(AppError.Network)
        val vm = NotificationsViewModel(repository, clock)
        advanceUntilIdle()
        assertNotNull(vm.state.value.loadError)

        withFirstPage(notification("1"))
        vm.retry()
        advanceUntilIdle()

        assertNull(vm.state.value.loadError)
        assertEquals(1, vm.state.value.items.size)
    }

    @Test
    fun `filtros separan social y planes`() = runTest(mainDispatcher.dispatcher) {
        withFirstPage(
            notification("1"),
            notification("2", NotificationType.PLAN_INVITATION, ids = mapOf("plan_id" to "p1")),
            notification("3", NotificationType.VOTE_OPENED, ids = mapOf("votacion_id" to "v1"))
        )
        val vm = NotificationsViewModel(repository, clock)
        advanceUntilIdle()

        vm.onFilter(NotificationFilter.SOCIAL)
        assertEquals(listOf("1"), vm.state.value.visible.map { it.id })
        vm.onFilter(NotificationFilter.PLANS)
        assertEquals(listOf("2", "3"), vm.state.value.visible.map { it.id })
    }

    @Test
    fun `tocar marca leida y abre el plan de la invitacion`() = runTest(mainDispatcher.dispatcher) {
        val invitation = notification("2", NotificationType.PLAN_INVITATION, ids = mapOf("plan_id" to "p1"))
        withFirstPage(invitation)
        val vm = NotificationsViewModel(repository, clock)
        advanceUntilIdle()

        vm.open.test {
            vm.open(invitation)
            val destination = awaitItem()
            assertEquals(PlanRoute("p1"), destination.route())
        }
        advanceUntilIdle()
        assertTrue(vm.state.value.items.single().read)
        assertTrue("read:2" in repository.calls)
    }

    @Test
    fun `si falla marcar leida vuelve a no leida`() = runTest(mainDispatcher.dispatcher) {
        val request = notification("1")
        withFirstPage(request)
        repository.markResult = Result.Failure(AppError.Network)
        val vm = NotificationsViewModel(repository, clock)
        advanceUntilIdle()

        vm.open(request)
        advanceUntilIdle()

        assertFalse(vm.state.value.items.single().read)
    }

    @Test
    fun `una ya leida no vuelve a llamar a la API`() = runTest(mainDispatcher.dispatcher) {
        val read = notification("1", read = true)
        withFirstPage(read)
        val vm = NotificationsViewModel(repository, clock)
        advanceUntilIdle()

        vm.open(read)
        advanceUntilIdle()

        assertFalse(repository.calls.any { it.startsWith("read:") })
    }

    @Test
    fun `marcar todas y su falla restaura`() = runTest(mainDispatcher.dispatcher) {
        withFirstPage(notification("1"), notification("2", read = true))
        val vm = NotificationsViewModel(repository, clock)
        advanceUntilIdle()
        assertTrue(vm.state.value.hasUnread)

        repository.markAllResult = Result.Failure(AppError.Server())
        vm.markAllRead()
        advanceUntilIdle()
        assertTrue(vm.state.value.hasUnread)
        assertNotNull(vm.state.value.error)

        repository.markAllResult = Result.Success(Unit)
        vm.markAllRead()
        advanceUntilIdle()
        assertFalse(vm.state.value.hasUnread)
    }

    @Test
    fun `borrar quita la fila y su falla la regresa`() = runTest(mainDispatcher.dispatcher) {
        val first = notification("1")
        withFirstPage(first, notification("2"))
        val vm = NotificationsViewModel(repository, clock)
        advanceUntilIdle()

        repository.deleteResult = Result.Failure(AppError.Network)
        vm.delete(first)
        advanceUntilIdle()
        assertEquals(listOf("1", "2"), vm.state.value.items.map { it.id })

        repository.deleteResult = Result.Success(Unit)
        vm.delete(first)
        advanceUntilIdle()
        assertEquals(listOf("2"), vm.state.value.items.map { it.id })
    }

    @Test
    fun `agrupa en hoy, esta semana y antes en la zona del telefono`() {
        val items = listOf(
            notification("hoy").copy(createdAt = "2026-11-19T07:00:00Z"),
            // 2026-11-19 01:00 UTC es 18 de noviembre en CDMX.
            notification("ayer").copy(createdAt = "2026-11-19T01:00:00Z"),
            notification("viejo").copy(createdAt = "2026-11-01T12:00:00Z"),
            notification("roto").copy(createdAt = "no es fecha")
        )

        val groups = groupByDay(items, LocalDate.of(2026, 11, 19), ZoneId.of("America/Mexico_City"))

        assertEquals(
            listOf(
                DayGroup.TODAY to listOf("hoy"),
                DayGroup.WEEK to listOf("ayer"),
                DayGroup.EARLIER to listOf("viejo", "roto")
            ),
            groups.map { (group, list) -> group to list.map { it.id } }
        )
    }

    @Test
    fun `push sin pantalla propia abre Avisos`() {
        assertEquals(NotificationsRoute, PushDestination.FriendRequest("u1").route())
        assertEquals(NotificationsRoute, PushDestination.VoteOpened("g1", "v1").route())
        assertEquals(NotificationsRoute, PushDestination.PlanInvitation("g1", null).route())
        assertEquals(NotificationsRoute, PushDestination.Notifications.route())
    }

    @Test
    fun `contador de la campana no baja de cero y se queda si falla`() = runTest(mainDispatcher.dispatcher) {
        val badge = UnreadBadgeViewModel(repository)
        repository.unread = Result.Success(4)
        badge.refresh()
        advanceUntilIdle()
        assertEquals(4, badge.count.value)

        repository.unread = Result.Failure(AppError.Network)
        badge.refresh()
        advanceUntilIdle()
        assertEquals(4, badge.count.value)
    }
}
