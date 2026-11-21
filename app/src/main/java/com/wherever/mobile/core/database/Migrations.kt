package com.wherever.mobile.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val VERSION_2 = 2
private const val VERSION_3 = 3
private const val VERSION_4 = 4
private const val VERSION_5 = 5

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

/** nv-53: nombre de usuario, código de amigo y "buscable por nombre de usuario" (encendido como en la API). */
val MIGRATION_3_4 = object : Migration(VERSION_3, VERSION_4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN username TEXT")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN friendCode TEXT")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN priv_searchableByUser INTEGER NOT NULL DEFAULT 1")
    }
}

/** nv-25: mensajes de los grupos para leerlos sin red. Tabla nueva: no toca el perfil. */
val MIGRATION_4_5 = object : Migration(VERSION_4, VERSION_5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `group_message` (`id` TEXT NOT NULL, `groupId` TEXT NOT NULL, " +
                "`clientId` TEXT, `authorId` TEXT, `authorName` TEXT, `authorPhoto` TEXT, " +
                "`system` INTEGER NOT NULL, `text` TEXT NOT NULL, `deleted` INTEGER NOT NULL, " +
                "`sentAt` TEXT NOT NULL, `status` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_group_message_groupId_sentAt` ON `group_message` (`groupId`, `sentAt`)"
        )
    }
}
