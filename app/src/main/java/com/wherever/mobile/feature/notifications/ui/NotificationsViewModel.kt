package com.wherever.mobile.feature.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.notifications.PushDestination
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.notifications.domain.AppNotification
import com.wherever.mobile.feature.notifications.domain.NotificationType
import com.wherever.mobile.feature.notifications.domain.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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

/** Grupos de US-147. */
enum class DayGroup { TODAY, WEEK, EARLIER }

private const val WEEK_DAYS = 7L

/** Agrupa por el día local de [zone]; una fecha ilegible cae en Antes. */
fun groupByDay(
    items: List<AppNotification>,
    today: LocalDate,
    zone: ZoneId
): List<Pair<DayGroup, List<AppNotification>>> = items.groupBy { notification ->
    val day = runCatching { Instant.parse(notification.createdAt).atZone(zone).toLocalDate() }.getOrNull()
    when {
        day == null -> DayGroup.EARLIER
        !day.isBefore(today) -> DayGroup.TODAY
        day.isAfter(today.minusDays(WEEK_DAYS)) -> DayGroup.WEEK
        else -> DayGroup.EARLIER
    }
}.toSortedMap().map { (group, list) -> group to list }

data class NotificationsUiState(
    val items: List<AppNotification> = emptyList(),
    val nextCursor: String? = null,
    val filter: NotificationFilter = NotificationFilter.ALL,
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    /** Falla de la primera carga: pantalla de error con Reintentar. */
    val loadError: UiText? = null,
    /** Falla de una acción: aviso arriba, la lista se queda. */
    val error: UiText? = null,
    /** Solicitudes aceptadas desde aquí (Notif · solicitud aceptada). */
    val accepted: Set<String> = emptySet(),
    val accepting: Set<String> = emptySet()
) {
    val visible: List<AppNotification>
        get() = filter.types?.let { types -> items.filter { it.type in types } } ?: items

    val hasUnread: Boolean
        get() = items.any { !it.read }
}

/** 18_Notif (specs/nv-18 y nv-52). */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository,
    private val friends: FriendsRepository,
    private val clock: Clock
) : ViewModel() {
    private val mutableState = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = mutableState.asStateFlow()

    private val openChannel = Channel<PushDestination>(Channel.BUFFERED)

    /** Avisos visibles agrupados por día en la zona del teléfono. */
    fun groups(state: NotificationsUiState): List<Pair<DayGroup, List<AppNotification>>> =
        groupByDay(state.visible, LocalDate.now(clock), clock.zone)

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
                is Result.Failure -> mutableState.update {
                    it.copy(loading = false, loadError = result.error.toUiText())
                }
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
                is Result.Failure -> mutableState.update {
                    it.copy(loadingMore = false, error = result.error.toUiText())
                }
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
        mutableState.update { state ->
            state.copy(items = state.items.filterNot { it.id == notification.id }, error = null)
        }
        viewModelScope.launch {
            val result = repository.delete(notification.id)
            if (result is Result.Failure) {
                mutableState.update { it.copy(items = before, error = result.error.toUiText()) }
            }
        }
    }

    /**
     * Aceptar desde el aviso: el aviso solo trae `usuario_id`; la solicitud se busca en recibidas.
     * Si ya no está (la cancelaron o se respondió en otro lado) se avisa y la fila se queda.
     */
    fun accept(notification: AppNotification) {
        val userId = notification.ids[PushDestination.KEY_USER_ID] ?: return
        if (notification.id in state.value.accepting) return
        mutableState.update { it.copy(accepting = it.accepting + notification.id, error = null) }
        viewModelScope.launch {
            val result = when (val received = friends.list(FriendshipList.RECEIVED)) {
                is Result.Success -> received.data.items.firstOrNull { it.person.id == userId }
                    ?.let { friends.respond(it.id, accept = true) }
                    ?: Result.Failure(AppError.NotFound())
                is Result.Failure -> received
            }
            mutableState.update { state ->
                when (result) {
                    is Result.Success -> state.copy(
                        accepting = state.accepting - notification.id,
                        accepted = state.accepted + notification.id
                    )
                    is Result.Failure -> state.copy(
                        accepting = state.accepting - notification.id,
                        error = if (result.error is AppError.NotFound) {
                            UiText.Res(R.string.notification_request_gone)
                        } else {
                            result.error.toUiText()
                        }
                    )
                }
            }
            if (result is Result.Success && !notification.read) markRead(notification)
        }
    }

    private fun markRead(notification: AppNotification) {
        setRead(notification.id, read = true)
        viewModelScope.launch { repository.markRead(notification.id) }
    }

    private fun setRead(id: String, read: Boolean) = mutableState.update { state ->
        state.copy(items = state.items.map { if (it.id == id) it.copy(read = read) else it })
    }
}
