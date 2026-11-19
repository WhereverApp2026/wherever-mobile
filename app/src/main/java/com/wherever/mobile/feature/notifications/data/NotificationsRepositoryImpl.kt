package com.wherever.mobile.feature.notifications.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.notifications.domain.AppNotification
import com.wherever.mobile.feature.notifications.domain.NotificationPage
import com.wherever.mobile.feature.notifications.domain.NotificationType
import com.wherever.mobile.feature.notifications.domain.NotificationsRepository
import javax.inject.Inject
import kotlinx.serialization.json.JsonPrimitive

/** Tamaño de página: la API acepta hasta 50. */
internal const val NOTIFICATIONS_PAGE = 20

class NotificationsRepositoryImpl @Inject constructor(private val api: NotificationsApi) : NotificationsRepository {
    override suspend fun page(cursor: String?): Result<NotificationPage> = safeApiCall {
        api.page(cursor, NOTIFICATIONS_PAGE)
    }.map { dto -> NotificationPage(dto.items.map { it.toDomain() }, dto.nextCursor?.takeIf { it.isNotBlank() }) }

    override suspend fun unreadCount(): Result<Int> = safeApiCall { api.unread().total }

    override suspend fun markRead(id: String): Result<Unit> = safeApiCall {
        api.mark(id, MarkReadDto(read = true))
    }.map { }

    override suspend fun markAllRead(): Result<Unit> = safeApiCall { api.markAll() }

    override suspend fun delete(id: String): Result<Unit> = safeApiCall { api.delete(id) }
}

internal fun NotificationDto.toDomain() = AppNotification(
    id = id,
    type = NotificationType.from(type),
    apiType = type,
    // Solo ids en texto; cualquier otro valor se ignora.
    ids = data.mapNotNull { (key, value) ->
        (value as? JsonPrimitive)?.takeIf { it.isString }?.content?.let { key to it }
    }.toMap(),
    read = read,
    createdAt = createdAt
)
