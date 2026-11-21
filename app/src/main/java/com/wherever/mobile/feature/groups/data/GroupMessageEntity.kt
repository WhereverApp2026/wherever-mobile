package com.wherever.mobile.feature.groups.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mensaje del chat de un grupo guardado para leerlo sin red (specs/nv-25). Es dato personal: se borra
 * al cerrar sesión (`GroupMessagesCleaner`). Los pendientes llevan `id = local:<id_cliente>`.
 */
@Entity(tableName = "group_message", indices = [Index("groupId", "sentAt")])
data class GroupMessageEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val clientId: String?,
    val authorId: String?,
    val authorName: String?,
    val authorPhoto: String?,
    val system: Boolean,
    val text: String,
    val deleted: Boolean,
    val sentAt: String,
    /** `SENT`, `PENDING` o `FAILED` (`MessageStatus`). */
    val status: String
)
