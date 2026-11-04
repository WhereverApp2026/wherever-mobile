package com.wherever.mobile.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppMetadataEntity::class, OnboardingStateEntity::class],
    version = 2,
    exportSchema = true
)
abstract class WhereverDatabase : RoomDatabase() {
    abstract fun appMetadataDao(): AppMetadataDao

    abstract fun onboardingDao(): OnboardingDao
}
