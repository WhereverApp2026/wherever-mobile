package com.wherever.mobile.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** nv-43: permiso de datos sensibles (`privacidad.datos_sensibles`). Sin permiso por defecto. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN priv_sensitiveData INTEGER NOT NULL DEFAULT 0")
    }
}
