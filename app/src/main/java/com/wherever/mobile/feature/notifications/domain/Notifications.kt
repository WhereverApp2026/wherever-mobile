package com.wherever.mobile.feature.notifications.domain

import com.wherever.mobile.core.common.Result

/** Tipos de `GET /notificaciones` (backend nv-40). Uno nuevo cae en [OTHER] y no rompe la lista. */
enum class NotificationType(val apiName: String) {
    FRIEND_REQUEST("solicitud_amistad"),
    FRIEND_ACCEPTED("amistad_aceptada"),
    PLAN_INVITATION("invitacion_plan"),
    VOTE_OPENED("votacion_abierta"),
    VOTE_CLOSED("votacion_cerrada"),
    OTHER("");

    companion object {
        fun from(apiName: String): NotificationType = entries.firstOrNull { it.apiName == apiName } ?: OTHER
    }
}

/** Un aviso. `ids` solo trae ids (`usuario_id`, `grupo_id`, `plan_id`, `votacion_id`), nunca texto. */
data class AppNotification(
    val id: String,
    val type: NotificationType,
    val apiType: String,
    val ids: Map<String, String>,
    val read: Boolean,
    val createdAt: String
)

data class NotificationPage(val items: List<AppNotification>, val nextCursor: String?)

interface NotificationsRepository {
    suspend fun page(cursor: String? = null): Result<NotificationPage>

    suspend fun unreadCount(): Result<Int>

    suspend fun markRead(id: String): Result<Unit>

    suspend fun markAllRead(): Result<Unit>

    suspend fun delete(id: String): Result<Unit>
}
