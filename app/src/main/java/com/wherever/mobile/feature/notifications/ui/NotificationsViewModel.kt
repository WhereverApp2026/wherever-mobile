package com.wherever.mobile.feature.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.notifications.PushDestination
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.notifications.domain.AppNotification
import com.wherever.mobile.feature.notifications.domain.NotificationType
import com.wherever.mobile.feature.notifications.domain.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Filtros de 18_Notif. La API no filtra: se filtra lo ya cargado. */
enum class NotificationFilter(val types: Set<NotificationType>?) {
    ALL(null),
    SOCIAL(setOf(NotificationType.FRIEND_REQUEST, NotificationType.FRIEND_ACCEPTED)),
    PLANS(setOf(NotificationType.PLAN_INVITATION, NotificationType.VOTE_OPENED, NotificationType.VOTE_CLOSED))
}

data class NotificationsUiState(
    val items: List<AppNotification> = emptyList(),
    val nextCursor: String? = null,
    val filter: NotificationFilter = NotificationFilter.ALL,
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    /** Falla de la primera carga: pantalla de error con Reintentar. */
    val loadError: UiText? = null,
    /** Falla de una acción: aviso arriba, la lista se queda. */
    val error: UiText? = null
) {
    val visible: List<AppNotification>
        get() = filter.types?.let { types -> items.filter { it.type in types } } ?: items

    val hasUnread: Boolean
        get() = items.any { !it.read }
}

/** 18_Notif (specs/nv-18 y nv-52). */
@HiltViewModel
class NotificationsViewModel @Inject constructor(private val repository: NotificationsRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = mutableState.asStateFlow()

    private val openChannel = Channel<PushDestination>(Channel.BUFFERED)

    /** A dónde lleva tocar un aviso; mismo destino que su push (specs/nv-46). */
    val open = openChannel.receiveAsFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        mutableState.update { it.copy(loading = true, loadError = null, error = null) }
        viewModelScope.launch {
            when (val result = repository.page()) {
                is Result.Success -> mutableState.update {
                    it.copy(loading = false, items = result.data.items, nextCursor = result.data.nextCursor)
                }
                is Result.Failure -> mutableState.update { it.copy(loading = false, loadError = result.error.toUiText()) }
            }
        }
    }

    fun loadMore() {
        val current = state.value
        val cursor = current.nextCursor ?: return
        if (current.loadingMore) return
        mutableState.update { it.copy(loadingMore = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.page(cursor)) {
                is Result.Success -> mutableState.update { state ->
                    val known = state.items.map { it.id }.toSet()
                    state.copy(
                        loadingMore = false,
                        items = state.items + result.data.items.filterNot { it.id in known },
                        nextCursor = result.data.nextCursor
                    )
                }
                is Result.Failure -> mutableState.update { it.copy(loadingMore = false, error = result.error.toUiText()) }
            }
        }
    }

    fun onFilter(filter: NotificationFilter) = mutableState.update { it.copy(filter = filter) }

    fun errorShown() = mutableState.update { it.copy(error = null) }

    /** Tocar marca leída (sin esperar a la API) y abre su destino. */
    fun open(notification: AppNotification) {
        if (!notification.read) {
            setRead(notification.id, read = true)
            viewModelScope.launch {
                if (repository.markRead(notification.id) is Result.Failure) setRead(notification.id, read = false)
            }
        }
        val data = notification.ids + (PushDestination.KEY_TYPE to notification.apiType)
        PushDestination.from(data)?.let { openChannel.trySend(it) }
    }

    fun markAllRead() {
        val before = state.value.items
        if (before.none { !it.read }) return
        mutableState.update { state -> state.copy(items = state.items.map { it.copy(read = true) }, error = null) }
        viewModelScope.launch {
            val result = repository.markAllRead()
            if (result is Result.Failure) {
                mutableState.update { it.copy(items = before, error = result.error.toUiText()) }
            }
        }
    }

    fun delete(notification: AppNotification) {
        val before = state.value.items
        mutableState.update { state -> state.copy(items = state.items.filterNot { it.id == notification.id }, error = null) }
        viewModelScope.launch {
            val result = repository.delete(notification.id)
            if (result is Result.Failure) {
                mutableState.update { it.copy(items = before, error = result.error.toUiText()) }
            }
        }
    }

    private fun setRead(id: String, read: Boolean) = mutableState.update { state ->
        state.copy(items = state.items.map { if (it.id == id) it.copy(read = read) else it })
    }
}
