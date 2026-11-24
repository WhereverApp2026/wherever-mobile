package com.wherever.mobile.feature.directchat.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.groups.domain.MessageStatus
import com.wherever.mobile.testing.FakeDirectChatRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleDirect
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** nv-25 (Unit): ChatAmigo. */
@OptIn(ExperimentalCoroutinesApi::class)
class DirectChatViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private lateinit var chats: FakeDirectChatRepository
    private val clock = Clock.fixed(Instant.parse("2026-11-24T18:30:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        chats = FakeDirectChatRepository()
    }

    private fun viewModel() =
        DirectChatViewModel(SavedStateHandle(mapOf("personId" to "u2", "personName" to "Mariana")), chats, clock)

    @Test
    fun `abre la conversacion, carga del mas nuevo al mas viejo y marca leido`() = runTest {
        chats.pageResult = Result.Success(
            PageResult(
                listOf(
                    sampleDirect("m2", "u2", "2026-11-24T18:01:00Z"),
                    sampleDirect("m1", "yo", "2026-11-24T18:00:00Z")
                ),
                "cur"
            )
        )
        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(listOf("open:u2", "messages:c1:", "read:c1"), chats.calls)
        assertEquals("Mariana López", state.title)
        assertEquals(listOf("m2", "m1"), state.messages.map { it.id })
        assertFalse(state.isMine(state.messages.first()))
        assertTrue(state.isMine(state.messages.last()))
        assertEquals("cur", state.olderCursor)
    }

    @Test
    fun `sin amistad la API da 404 y la pantalla se cierra`() = runTest {
        chats.openResult = Result.Failure(AppError.NotFound())
        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.state.value.gone)
    }

    @Test
    fun `enviar es optimista y el mensaje del servidor reemplaza al pendiente`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onDraft("  Hola  ")
        vm.send()

        val pending = vm.state.value.messages.single()
        assertEquals(MessageStatus.PENDING, pending.status)
        assertEquals("", vm.state.value.draft)
        advanceUntilIdle()
        val sent = vm.state.value.messages.single()
        assertEquals(MessageStatus.SENT, sent.status)
        assertEquals("Hola", sent.text)
        assertEquals("send:c1:Hola:", chats.calls.last())
    }

    @Test
    fun `si falla queda No se envio y Reintentar usa el mismo id_cliente`() = runTest {
        val clientIds = mutableListOf<String>()
        chats.sendResult = { _, clientId ->
            clientIds += clientId
            Result.Failure(AppError.RateLimited())
        }
        val vm = viewModel()
        advanceUntilIdle()
        vm.onDraft("Hola")
        vm.send()
        advanceUntilIdle()

        val failed = vm.state.value.messages.single()
        assertEquals(MessageStatus.FAILED, failed.status)
        assertEquals(UiText.Res(R.string.groups_chat_rate_limited), vm.state.value.error)
        chats.sendResult = null
        vm.retry(failed)
        advanceUntilIdle()
        assertEquals(MessageStatus.SENT, vm.state.value.messages.single().status)
        assertEquals(failed.clientId, clientIds.single())
    }

    @Test
    fun `el sondeo trae lo nuevo despues del ultimo y no duplica`() = runTest {
        chats.pageResult = Result.Success(PageResult(listOf(sampleDirect("m1", "u2", "2026-11-24T18:00:00Z")), null))
        val vm = viewModel()
        advanceUntilIdle()
        chats.newerResult = Result.Success(
            listOf(sampleDirect("m2", "u2", "2026-11-24T18:01:00Z"), sampleDirect("m3", "u2", "2026-11-24T18:02:00Z"))
        )
        vm.pollOnce()
        vm.pollOnce()

        assertTrue(chats.calls.contains("newer:c1:m1"))
        assertEquals(listOf("m3", "m2", "m1"), vm.state.value.messages.map { it.id })
    }

    @Test
    fun `sin red al abrir muestra el error con Reintentar`() = runTest {
        chats.openResult = Result.Failure(AppError.Network)
        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.state.value.loadError != null)
        assertFalse(vm.state.value.canSend)
    }
}
