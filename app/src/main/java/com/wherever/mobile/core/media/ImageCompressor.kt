package com.wherever.mobile.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.max

/** Foto lista para subir: JPEG, lado mayor de 1600 px como mucho (specs/nv-50). */
class CompressedImage(val bytes: ByteArray, val mimeType: String = "image/jpeg")

/** Lee la foto de la galería o de la cámara y la comprime antes de subirla. `null` si no se pudo leer. */
interface ImageCompressor {
    suspend fun compress(source: String): CompressedImage?
}

private const val MAX_SIDE = 1600
private const val JPEG_QUALITY = 85

/**
 * `ImageDecoder` (API 28+) aplica la orientación EXIF, así la foto de cámara no queda de lado.
 * En API 26-27 se usa `BitmapFactory` con submuestreo. El backend vuelve a codificar y quita
 * los metadatos (nv-45), así que aquí no se conserva el EXIF.
 */
class AndroidImageCompressor @Inject constructor(@ApplicationContext private val context: Context) : ImageCompressor {
    override suspend fun compress(source: String): CompressedImage? = try {
        decode(source.toUri())?.let { bitmap ->
            val scaled = bitmap.scaledDown()
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            if (scaled !== bitmap) scaled.recycle()
            bitmap.recycle()
            CompressedImage(out.toByteArray())
        }
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    }

    private fun decode(uri: Uri): Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val decoderSource = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(decoderSource) { decoder, info, _ ->
            val side = max(info.size.width, info.size.height)
            if (side > MAX_SIDE) {
                val ratio = MAX_SIDE.toFloat() / side
                decoder.setTargetSize((info.size.width * ratio).toInt(), (info.size.height * ratio).toInt())
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    private fun Bitmap.scaledDown(): Bitmap {
        val side = max(width, height)
        if (side <= MAX_SIDE) return this
        val ratio = MAX_SIDE.toFloat() / side
        return Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
    }
}
