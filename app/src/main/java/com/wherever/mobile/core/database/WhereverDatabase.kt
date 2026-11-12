package com.wherever.mobile.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wherever.mobile.feature.profile.data.UserProfileDao
import com.wherever.mobile.feature.profile.data.UserProfileEntity

@Database(entities = [UserProfileEntity::class], version = 2, exportSchema = true)
@TypeConverters(StringListConverter::class)
abstract class WhereverDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
}
