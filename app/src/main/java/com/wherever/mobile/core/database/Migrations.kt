package com.wherever.mobile.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val VERSION_2 = 2
private const val VERSION_3 = 3

/** nv-50: foto de perfil (`foto_url`). */
val MIGRATION_2_3 = object : Migration(VERSION_2, VERSION_3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN photoUrl TEXT")
    }
}

/** nv-43: permiso de datos sensibles (`privacidad.datos_sensibles`). Sin permiso por defecto. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN priv_sensitiveData INTEGER NOT NULL DEFAULT 0")
    }
}
