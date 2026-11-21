package com.wherever.mobile.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wherever.mobile.feature.groups.data.GroupMessageDao
import com.wherever.mobile.feature.groups.data.GroupMessageEntity
import com.wherever.mobile.feature.profile.data.UserProfileDao
import com.wherever.mobile.feature.profile.data.UserProfileEntity

@Database(entities = [UserProfileEntity::class, GroupMessageEntity::class], version = 5, exportSchema = true)
@TypeConverters(StringListConverter::class)
abstract class WhereverDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    abstract fun groupMessageDao(): GroupMessageDao
}
