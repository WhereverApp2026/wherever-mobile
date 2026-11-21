package com.wherever.mobile.feature.groups.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.groups.domain.ChatMessage
import com.wherever.mobile.feature.groups.domain.Group
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Límite de la API para `texto` (backend nv-57). */
const val MAX_MESSAGE_LENGTH = 1000

data class GroupChatUiState(
    val group: Group? = null,
    val plan: Plan? = null,
    /** Del más nuevo al más viejo (la lista se dibuja invertida). */
    val messages: List<ChatMessage> = emptyList(),
    val myId: String? = null,
    val loading: Boolean = true,
    val loadError: UiText? = null,
    /** Hay mensajes en Room pero la API no respondió: se leen sin red. */
    val offline: Boolean = false,
    val olderCursor: String? = null,
    val loadingOlder: Boolean = false,
    val draft: String = "",
    val error: UiText? = null,
    /** El grupo ya no es mío o no estoy confirmado (404): la pantalla se cierra. */
    val gone: Boolean = false
) {
    val canSend: Boolean
        get() = draft.isNotBlank() && draft.trim().length <= MAX_MESSAGE_LENGTH && group != null

    val empty: Boolean
        get() = !loading && loadError == null && messages.isEmpty()

    /** Título: el nombre del grupo, el del plan o los integrantes. */
    val title: String?
        get() = group?.name ?: plan?.name
}

/** 45_GrupoChat (specs/nv-25): plan fijado, mensajes con envío optimista y lectura sin red. */
@HiltViewModel
class GroupChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groups: GroupsRepository,
    private val plans: PlansRepository,
    profiles: ProfileRepository
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(GroupChatUiState())
    val state: StateFlow<GroupChatUiState> = mutableState.asStateFlow()
    private var polling: Job? = null

    init {
        viewModelScope.launch {
            combine(groups.observeMessages(groupId), profiles.observeProfile()) { messages, profile ->
                messages to profile?.id
            }.collect { (messages, myId) ->
                mutableState.update { it.copy(messages = messages, myId = myId) }
            }
        }
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = it.group == null, loadError = null, offline = false) }
        viewModelScope.launch {
            when (val result = groups.group(groupId)) {
                is Result.Success -> {
                    mutableState.update { it.copy(group = result.data) }
                    loadPlan(result.data.planId)
                    loadMessages()
                }
                is Result.Failure -> onLoadFailure(result.error)
            }
        }
    }

    private suspend fun loadPlan(planId: String) {
        // El plan es complemento: si falla, el chat sigue.
        (plans.get(planId) as? Result.Success)?.let { plan -> mutableState.update { it.copy(plan = plan.data) } }
    }

    private suspend fun loadMessages() {
        val result = groups.refreshMessages(groupId)
        mutableState.update {
            when (result) {
                is Result.Success -> it.copy(loading = false, olderCursor = result.data)
                is Result.Failure -> it.copy(loading = false, offline = true, error = result.error.toUiText())
            }
        }
    }

    private fun onLoadFailure(error: AppError) = mutableState.update {
        when {
            error is AppError.NotFound -> it.copy(loading = false, gone = true)
            // Sin red con mensajes guardados: se leen de Room con el aviso.
            it.messages.isNotEmpty() -> it.copy(loading = false, offline = true, error = error.toUiText())
            else -> it.copy(loading = false, loadError = error.toUiText())
        }
    }

    fun loadOlder() {
        val current = state.value
        val cursor = current.olderCursor ?: return
        if (current.loadingOlder) return
        mutableState.update { it.copy(loadingOlder = true) }
        viewModelScope.launch {
            val result = groups.loadOlder(groupId, cursor)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(loadingOlder = false, olderCursor = result.data)
                    is Result.Failure -> it.copy(loadingOlder = false, error = result.error.toUiText())
                }
            }
        }
    }

    /** Mientras la pantalla está a la vista pregunta por mensajes nuevos (D28: sin tiempo real aún). */
    fun startPolling() {
        if (polling?.isActive == true) return
        polling = viewModelScope.launch {
            while (isActive) {
                delay(POLL_MILLIS)
                if (state.value.group != null && groups.pollNew(groupId) is Result.Success) {
                    mutableState.update { it.copy(offline = false) }
                }
            }
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
        mutableState.update { it.copy(draft = "", error = null) }
        viewModelScope.launch {
            val result = groups.send(groupId, text)
            if (result is Result.Failure) {
                // El mensaje queda en la lista marcado "No se envió" con Reintentar.
                mutableState.update { it.copy(error = sendError(result.error)) }
            }
        }
    }

    fun retry(message: ChatMessage) {
        val clientId = message.clientId ?: return
        viewModelScope.launch {
            val result = groups.retry(groupId, clientId)
            if (result is Result.Failure) mutableState.update { it.copy(error = sendError(result.error)) }
        }
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
    }
}
