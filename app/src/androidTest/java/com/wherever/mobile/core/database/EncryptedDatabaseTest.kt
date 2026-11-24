package com.wherever.mobile.core.database

import android.database.sqlite.SQLiteDatabase as PlainDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Constitución E-06: una base en claro de antes se cifra sin perder filas ni la versión de Room. */
@RunWith(AndroidJUnit4::class)
class EncryptedDatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val file get() = context.getDatabasePath(DATABASE_NAME)
    private var backup: ByteArray? = null

    @Before
    fun setUp() {
        System.loadLibrary("sqlcipher")
        // La prueba corre en la misma app: guarda la base real y la devuelve al final.
        backup = file.takeIf { it.exists() }?.readBytes()
        context.deleteDatabase(DATABASE_NAME)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(DATABASE_NAME)
        backup?.let { file.writeBytes(it) }
    }

    @Test
    fun laBaseEnClaroSeCifraConSusDatosYVersion() {
        file.parentFile?.mkdirs()
        PlainDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE t (id INTEGER PRIMARY KEY, texto TEXT)")
            db.execSQL("INSERT INTO t (texto) VALUES ('hola'), ('adiós')")
            db.version = 5
        }
        assertFalse(isEncrypted(file))
        val key = ByteArray(32) { it.toByte() }

        migratePlaintextDatabase(context, key)

        assertTrue("El archivo quedó cifrado", isEncrypted(file))
        val db = SQLiteDatabase.openDatabase(
            file.path,
            rawKey(key).toByteArray(),
            null,
            SQLiteDatabase.OPEN_READWRITE,
            null,
            null
        )
        db.use {
            assertEquals(5, it.version)
            it.rawQuery("SELECT count(*) FROM t", arrayOf<String>()).use { c ->
                c.moveToFirst()
                assertEquals(2, c.getInt(0))
            }
        }
    }
}
