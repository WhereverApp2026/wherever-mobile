package com.wherever.mobile.feature.memories.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.di.UploadClient
import com.wherever.mobile.feature.memories.domain.PostcardGallery
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Descarga la imagen publicada con el cliente sin `Authorization` (es una URL pública de imágenes) y la guarda
 * con MediaStore en `Pictures/Wherever`, sin permiso de almacenamiento (Android 10+).
 */
class AndroidPostcardGallery @Inject constructor(
    @ApplicationContext private val context: Context,
    @UploadClient private val client: OkHttpClient,
    private val dispatchers: DispatcherProvider
) : PostcardGallery {
    override suspend fun save(imageUrl: String): Result<Unit> = withContext(dispatchers.io) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext Result.Failure(AppError.Forbidden())
        try {
            val bytes = client.newCall(Request.Builder().url(imageUrl).build()).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                response.body.bytes()
            }
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "wherever_postal_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Wherever")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("MediaStore sin destino")
            resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: throw IOException("MediaStore sin flujo")
            Result.Success(Unit)
        } catch (_: IOException) {
            Result.Failure(AppError.Network)
        }
    }
}
