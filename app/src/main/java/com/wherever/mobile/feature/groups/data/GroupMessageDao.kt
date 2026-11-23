package com.wherever.mobile.feature.groups.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMessageDao {
    @Query("SELECT * FROM group_message WHERE groupId = :groupId ORDER BY sentAt DESC, id DESC")
    fun observe(groupId: String): Flow<List<GroupMessageEntity>>

    /** El último que ya está en el servidor: el `despues_de` para pedir lo nuevo. */
    @Query(
        "SELECT * FROM group_message WHERE groupId = :groupId AND status = 'SENT' " +
            "ORDER BY sentAt DESC, id DESC LIMIT 1"
    )
    suspend fun lastSent(groupId: String): GroupMessageEntity?

    @Query("SELECT * FROM group_message WHERE groupId = :groupId AND clientId = :clientId LIMIT 1")
    suspend fun byClientId(groupId: String, clientId: String): GroupMessageEntity?

    @Upsert
    suspend fun upsert(messages: List<GroupMessageEntity>)

    @Query("DELETE FROM group_message WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM group_message WHERE groupId = :groupId")
    suspend fun clearGroup(groupId: String)

    @Query("DELETE FROM group_message")
    suspend fun clear()
}
