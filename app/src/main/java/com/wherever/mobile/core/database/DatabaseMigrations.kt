package com.wherever.mobile.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `onboarding_state` (
                `id` INTEGER NOT NULL,
                `userId` TEXT NOT NULL,
                `step` TEXT NOT NULL,
                `interests` TEXT NOT NULL,
                `vibe` TEXT NOT NULL,
                `budget` TEXT NOT NULL,
                `company` TEXT NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}
