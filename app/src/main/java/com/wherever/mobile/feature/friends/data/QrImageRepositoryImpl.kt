package com.wherever.mobile.feature.friends.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.QrMatrix
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.friends.domain.QrImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject
import kotlinx.coroutines.withContext

/**
 * PNG del QR (oscuro sobre blanco, con zona de silencio de 4 módulos). Se comparte por el
 * `FileProvider` desde `cache/qr/`, y en Android 10+ se guarda con MediaStore, sin permiso de
 * almacenamiento. En Android 8 y 9 guardar pediría `WRITE_EXTERNAL_STORAGE`: ahí se ofrece Compartir.
 */
class QrImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider
) : QrImageRepository {
    override suspend fun shareable(payload: String): Result<String> = withContext(dispatchers.io) {
        runCatchingIo {
            val dir = File(context.cacheDir, "qr").apply { mkdirs() }
            val file = File(dir, "mi_codigo.png")
            file.outputStream().use { render(payload).compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
            FileProvider.getUriForFile(context, "${context.packageName}.fotos", file).toString()
        }
    }

    override suspend fun saveToGallery(payload: String): Result<Unit> = withContext(dispatchers.io) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext Result.Failure(AppError.Forbidden())
        runCatchingIo {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "wherever_mi_codigo_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Wherever")
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("MediaStore sin destino")
            resolver.openOutputStream(uri).use { stream: OutputStream? ->
                checkNotNull(stream) { "MediaStore sin flujo" }
                render(payload).compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
            }
            Unit
        }
    }

    private fun render(payload: String): Bitmap {
        val matrix = QrMatrix.encode(payload)
        val modules = matrix.size + QUIET_ZONE * 2
        val bitmap = createBitmap(modules * MODULE_PX, modules * MODULE_PX)
        bitmap.eraseColor(android.graphics.Color.WHITE)
        for (y in 0 until matrix.size) {
            for (x in 0 until matrix.size) {
                if (matrix[x, y]) fillModule(bitmap, x + QUIET_ZONE, y + QUIET_ZONE)
            }
        }
        return bitmap
    }

    private fun fillModule(bitmap: Bitmap, moduleX: Int, moduleY: Int) {
        for (dy in 0 until MODULE_PX) {
            for (dx in 0 until MODULE_PX) bitmap[moduleX * MODULE_PX + dx, moduleY * MODULE_PX + dy] = INK
        }
    }

    private inline fun <T> runCatchingIo(block: () -> T): Result<T> = try {
        Result.Success(block())
    } catch (error: IOException) {
        Result.Failure(AppError.Unknown(error.message))
    } catch (error: IllegalStateException) {
        Result.Failure(AppError.Unknown(error.message))
    }

    private companion object {
        const val MODULE_PX = 16
        const val QUIET_ZONE = 4
        const val PNG_QUALITY = 100

        /** `Ink` del sistema de diseño (constitución §4) en ARGB. */
        const val INK = 0xFF10243B.toInt()
    }
}
