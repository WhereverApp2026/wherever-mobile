package com.wherever.mobile.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface AppMetadataDao {

    @Query("SELECT value FROM app_metadata WHERE `key` = :key")
    suspend fun value(key: String): String?

    @Upsert
    suspend fun upsert(entity: AppMetadataEntity)

    @Query("DELETE FROM app_metadata")
    suspend fun clear()
}
