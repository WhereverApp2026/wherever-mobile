package com.wherever.mobile.feature.directchat.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.directchat.domain.Conversation
import com.wherever.mobile.feature.directchat.domain.DirectChatRepository
import com.wherever.mobile.feature.directchat.domain.DirectMessage
import com.wherever.mobile.feature.groups.domain.MessageStatus
import com.wherever.mobile.feature.groups.ui.MAX_MESSAGE_LENGTH
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DirectChatUiState(
    val personId: String = "",
    val personName: String = "",
    val conversation: Conversation? = null,
    /** Del más nuevo al más viejo (la lista se dibuja invertida). */
    val messages: List<DirectMessage> = emptyList(),
    val loading: Boolean = true,
    val loadError: UiText? = null,
    val olderCursor: String? = null,
    val loadingOlder: Boolean = false,
    val draft: String = "",
    val error: UiText? = null,
    /** Ya no son amigos o la cuenta no está activa (404): la pantalla se cierra. */
    val gone: Boolean = false
) {
    val canSend: Boolean
        get() = draft.isNotBlank() && draft.trim().length <= MAX_MESSAGE_LENGTH && conversation != null

    val empty: Boolean
        get() = !loading && loadError == null && messages.isEmpty()

    val title: String
        get() = conversation?.otherName ?: personName

    /** Mío si no lo escribió la otra persona (los pendientes aún no tienen autor). */
    fun isMine(message: DirectMessage): Boolean = message.authorId == null || message.authorId != conversation?.otherId
}

/** ChatAmigo (specs/nv-25): chat 1 a 1 con un amigo, envío optimista y planes compartidos con su invitación. */
@HiltViewModel
class DirectChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chats: DirectChatRepository,
    private val clock: Clock
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        DirectChatUiState(
            personId = checkNotNull(savedStateHandle["personId"]),
            personName = savedStateHandle.get<String>("personName").orEmpty()
        )
    )
    val state: StateFlow<DirectChatUiState> = mutableState.asStateFlow()
    private var polling: Job? = null

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            val conversation = state.value.conversation ?: when (val opened = chats.open(state.value.personId)) {
                is Result.Success -> opened.data.also { c -> mutableState.update { it.copy(conversation = c) } }
                is Result.Failure -> return@launch onLoadFailure(opened.error)
            }
            when (val page = chats.messages(conversation.id)) {
                is Result.Success -> {
                    mutableState.update {
                        it.copy(
                            loading = false,
                            messages = merge(it.messages, page.data.items),
                            olderCursor = page.data.nextCursor
                        )
                    }
                    chats.markRead(conversation.id)
                }
                is Result.Failure -> onLoadFailure(page.error)
            }
        }
    }

    private fun onLoadFailure(error: AppError) = mutableState.update {
        if (error is AppError.NotFound) {
            it.copy(
                loading = false,
                gone = true
            )
        } else {
            it.copy(loading = false, loadError = error.toUiText())
        }
    }

    fun loadOlder() {
        val current = state.value
        val cursor = current.olderCursor
        val conversation = current.conversation
        if (cursor == null || conversation == null || current.loadingOlder) return
        mutableState.update { it.copy(loadingOlder = true) }
        viewModelScope.launch {
            val result = chats.messages(conversation.id, cursor)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(
                        loadingOlder = false,
                        messages = merge(it.messages, result.data.items),
                        olderCursor = result.data.nextCursor
                    )
                    is Result.Failure -> it.copy(loadingOlder = false, error = result.error.toUiText())
                }
            }
        }
    }

    /** Mientras la pantalla está a la vista pregunta por mensajes nuevos (D28: sin tiempo real aún). */
    fun startPolling() {
        if (polling?.isActive == true) return
        polling = viewModelScope.launch {
            // Al volver (p. ej. de InvitarPlan) se pregunta de inmediato y luego cada 5 s.
            while (isActive) {
                pollOnce()
                delay(POLL_MILLIS)
            }
        }
    }

    /** Sin mensajes del servidor aún se vuelve a pedir la página; si no, lo posterior al último. */
    internal suspend fun pollOnce() {
        val conversation = state.value.conversation ?: return
        val last = state.value.messages.firstOrNull { it.status == MessageStatus.SENT }
        val result = if (last == null) {
            (chats.messages(conversation.id) as? Result.Success)?.data?.items
        } else {
            (chats.newer(conversation.id, last.id) as? Result.Success)?.data?.asReversed()
        }
        if (!result.isNullOrEmpty()) {
            mutableState.update { it.copy(messages = merge(it.messages, result)) }
            chats.markRead(conversation.id)
        }
    }

    fun stopPolling() {
        polling?.cancel()
        polling = null
    }

    fun onDraft(value: String) = mutableState.update { it.copy(draft = value.take(MAX_MESSAGE_LENGTH), error = null) }

    fun send() {
        val current = state.value
        if (!current.canSend) return
        val text = current.draft.trim()
        val clientId = UUID.randomUUID().toString()
        val pending = DirectMessage(
            id = LOCAL_PREFIX + clientId,
            clientId = clientId,
            authorId = null,
            text = text,
            attachment = null,
            deleted = false,
            sentAt = clock.instant().toString(),
            status = MessageStatus.PENDING
        )
        mutableState.update { it.copy(draft = "", error = null, messages = listOf(pending) + it.messages) }
        deliver(pending)
    }

    fun retry(message: DirectMessage) {
        if (message.status != MessageStatus.FAILED) return
        replace(message.id) { it.copy(status = MessageStatus.PENDING) }
        deliver(message)
    }

    private fun deliver(message: DirectMessage) {
        val conversation = state.value.conversation ?: return
        val clientId = message.clientId ?: return
        viewModelScope.launch {
            when (val result = chats.send(conversation.id, message.text, clientId)) {
                is Result.Success -> mutableState.update {
                    it.copy(messages = merge(it.messages.filterNot { m -> m.id == message.id }, listOf(result.data)))
                }
                is Result.Failure -> {
                    replace(message.id) { it.copy(status = MessageStatus.FAILED) }
                    mutableState.update { it.copy(error = sendError(result.error)) }
                }
            }
        }
    }

    private fun replace(id: String, change: (DirectMessage) -> DirectMessage) = mutableState.update {
        it.copy(messages = it.messages.map { m -> if (m.id == id) change(m) else m })
    }

    private fun sendError(error: AppError): UiText = when (error) {
        is AppError.RateLimited -> UiText.Res(R.string.groups_chat_rate_limited)
        else -> error.toUiText()
    }

    fun errorShown() = mutableState.update { it.copy(error = null) }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }

    private companion object {
        const val POLL_MILLIS = 5_000L
        const val LOCAL_PREFIX = "local:"
    }
}

/** Une lo que llegó con lo que hay: lo del servidor reemplaza por id; los pendientes quedan arriba. */
internal fun merge(current: List<DirectMessage>, incoming: List<DirectMessage>): List<DirectMessage> {
    val byId = incoming.associateBy(DirectMessage::id)
    val kept = current.filter { it.id !in byId }
    val (pending, sent) = kept.partition { it.status != MessageStatus.SENT }
    return pending + (sent + incoming).sortedByDescending { m -> m.sentAt.toInstantOrMin() }
}

private fun String.toInstantOrMin(): Instant = try {
    Instant.parse(this)
} catch (_: DateTimeParseException) {
    Instant.MIN
}
