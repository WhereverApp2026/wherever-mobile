package com.wherever.mobile.core.database

import android.content.Context
import com.wherever.mobile.core.security.TokenCipher
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.SecureRandom
import net.zetetic.database.sqlcipher.SQLiteDatabase

/** Nombre del archivo de Room. */
const val DATABASE_NAME = "wherever.db"

private const val KEY_FILE = "wherever_db_key.enc"
private const val KEY_BYTES = 32
private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

/**
 * Llave de la base cifrada (constitución E-06, MASVS-STORAGE-1): 32 bytes aleatorios envueltos con la clave
 * AES-GCM de Android Keystore y guardados en `noBackupFilesDir` (no salen en respaldos).
 * Si la llave no se puede leer (Keystore borrado, archivo dañado), la base vieja no sirve: se borra y se crea
 * otra; Room es una caché de la API y se vuelve a llenar.
 */
class DatabaseKeyProvider(private val context: Context, private val cipher: TokenCipher) {
    private val keyFile: File get() = File(context.noBackupFilesDir, KEY_FILE)

    fun passphrase(): ByteArray {
        val stored = try {
            keyFile.takeIf { it.exists() }?.readBytes()?.let(cipher::decrypt)
        } catch (_: GeneralSecurityException) {
            null
        } catch (_: IOException) {
            null
        }
        if (stored != null && stored.size == KEY_BYTES) return stored
        if (keyFile.exists() || isEncrypted(context.getDatabasePath(DATABASE_NAME))) {
            // Llave perdida con una base cifrada: no hay forma de abrirla.
            context.deleteDatabase(DATABASE_NAME)
        }
        val fresh = ByteArray(KEY_BYTES).also(SecureRandom()::nextBytes)
        keyFile.writeBytes(cipher.encrypt(fresh))
        return fresh
    }
}

/** Llave cruda de 256 bits para SQLCipher (`x'…'`): sin derivación PBKDF2, porque ya es aleatoria. */
fun rawKey(passphrase: ByteArray): String = "x'" + passphrase.joinToString("") { "%02x".format(it) } + "'"

/** `true` si el archivo existe y no empieza con la cabecera de SQLite en claro. */
internal fun isEncrypted(file: File): Boolean {
    if (!file.exists() || file.length() < SQLITE_HEADER.size) return false
    val header = file.inputStream().use { input -> ByteArray(SQLITE_HEADER.size).also { input.read(it) } }
    return !header.contentEquals(SQLITE_HEADER)
}

/**
 * Las instalaciones de antes de la E-06 tienen `wherever.db` sin cifrar: se copia cifrada con
 * `sqlcipher_export` (conserva tablas, datos y `user_version`, así siguen las migraciones de Room)
 * y se reemplaza el archivo. Se llama antes de abrir Room.
 */
fun migratePlaintextDatabase(context: Context, passphrase: ByteArray) {
    val plain = context.getDatabasePath(DATABASE_NAME)
    if (!plain.exists() || isEncrypted(plain)) return
    System.loadLibrary("sqlcipher")
    val encrypted = File(plain.parentFile, "$DATABASE_NAME.cifrando")
    encrypted.delete()
    val flags = SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY // el ATTACH hereda estos permisos
    val db = SQLiteDatabase.openDatabase(plain.path, "", null, flags, null, null)
    try {
        // Room deja la base en WAL, que abre varias conexiones: el ATTACH quedaría en una y el export en otra.
        // Sin WAL hay una sola conexión y todo corre en ella.
        db.disableWriteAheadLogging()
        db.execSQL("ATTACH DATABASE '${encrypted.path}' AS cifrada KEY \"${rawKey(passphrase)}\"")
        // Una consulta que avanza el cursor: así SQLite ejecuta de verdad la función.
        db.rawQuery("SELECT sqlcipher_export('cifrada')", arrayOf<String>()).use { it.moveToFirst() }
        db.execSQL("DETACH DATABASE cifrada")
    } finally {
        db.close()
    }
    // `sqlcipher_export` no copia `user_version`; sin ella Room creería que es una base nueva.
    val version = SQLiteDatabase.openDatabase(plain.path, "", null, SQLiteDatabase.OPEN_READONLY, null, null)
        .use { it.version }
    SQLiteDatabase.openDatabase(
        encrypted.path,
        rawKey(passphrase).toByteArray(),
        null,
        SQLiteDatabase.OPEN_READWRITE,
        null,
        null
    ).use { it.version = version }
    listOf("-wal", "-shm", "-journal").forEach { File(plain.path + it).delete() }
    check(encrypted.renameTo(plain) || (plain.delete() && encrypted.renameTo(plain))) {
        "No se pudo reemplazar la base sin cifrar"
    }
}
