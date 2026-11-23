package com.wherever.mobile.feature.business.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

/** Guarda un PDF en Descargas. Separado del repositorio para probar la descarga sin Android. */
fun interface ReportSaver {
    /** Copia todo [input] a [fileName]; `false` si no se pudo escribir. */
    fun save(fileName: String, input: InputStream): Boolean
}

/**
 * Android 10 o más: `MediaStore.Downloads`, sin permisos. Android 8 y 9: la carpeta Descargas propia de la app
 * (tampoco pide permisos; specs/nv-35 §5).
 */
class MediaStoreReportSaver @Inject constructor(@ApplicationContext private val context: Context) : ReportSaver {
    override fun save(fileName: String, input: InputStream): Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStore(fileName, input)
        } else {
            saveToAppFolder(fileName, input)
        }
    } catch (_: IOException) {
        false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToMediaStore(fileName: String, input: InputStream): Boolean {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, PDF)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
        val written = resolver.openOutputStream(uri)?.use { input.copyTo(it) } != null
        if (written) {
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } else {
            resolver.delete(uri, null, null)
        }
        return written
    }

    private fun saveToAppFolder(fileName: String, input: InputStream): Boolean {
        val folder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return false
        File(folder, fileName).outputStream().use { input.copyTo(it) }
        return true
    }

    private companion object {
        const val PDF = "application/pdf"
    }
}
