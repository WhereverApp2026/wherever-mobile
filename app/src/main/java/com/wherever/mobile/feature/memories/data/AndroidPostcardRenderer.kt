package com.wherever.mobile.feature.memories.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.InkMuted
import com.wherever.mobile.core.designsystem.Mist
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.feature.memories.domain.PostcardRenderer
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.withContext

// Medidas en píxeles de la imagen final (1080 × 1350, 4:5), no de la pantalla.
private const val WIDTH = 1080
private const val HEIGHT = 1350
private const val MARGIN = 48f
private const val PHOTO_HEIGHT = 960f
private const val TITLE_SIZE = 60f
private const val SUBTITLE_SIZE = 36f
private const val LINE_GAP = 20f
private const val STAMP_SIZE = 170f
private const val STAMP_STROKE = 5f
private const val DASH = 14f
private const val CORNER = 24f
private const val SHADE_ALPHA = 150
private const val JPEG_QUALITY = 90
private const val MAX_SOURCE_SIDE = 2048

/**
 * Dibuja la postal con Canvas y la guarda en `cache/fotos/` como JPEG nuevo: `Bitmap.compress` no copia
 * metadatos, así que no viaja EXIF de ubicación (specs/nv-31 §5). Después la sube `PhotoRepository`.
 */
class AndroidPostcardRenderer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider
) : PostcardRenderer {
    override suspend fun render(source: String, style: PostcardStyle, title: String, subtitle: String): Result<String> =
        withContext(dispatchers.io) {
            try {
                val photo = decode(source.toUri()) ?: return@withContext Result.Failure(AppError.Validation())
                val card = createBitmap(WIDTH, HEIGHT)
                Canvas(card).draw(style, photo, Caption(title, subtitle, context.getString(R.string.memories_stamp)))
                photo.recycle()
                val dir = File(context.cacheDir, "fotos").apply { mkdirs() }
                val file = File(dir, "postal_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { card.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
                card.recycle()
                Result.Success(Uri.fromFile(file).toString())
            } catch (_: IOException) {
                Result.Failure(AppError.Validation())
            }
        }

    /** `ImageDecoder` respeta la rotación de la foto; si no puede con el archivo, se intenta con `BitmapFactory`. */
    private fun decode(uri: Uri): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                return decodeWithImageDecoder(uri)
            } catch (_: IOException) {
                // Formato que ImageDecoder no lee: sigue el respaldo.
            }
        }
        return openStream(uri)?.use { BitmapFactory.decodeStream(it) }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun decodeWithImageDecoder(uri: Uri): Bitmap {
        // `file://` se lee directo; `content://` (cámara y galería) pasa por el ContentResolver.
        val path = uri.path
        val source = if (uri.scheme == "file" && path != null) {
            ImageDecoder.createSource(File(path))
        } else {
            ImageDecoder.createSource(context.contentResolver, uri)
        }
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val side = max(info.size.width, info.size.height)
            if (side > MAX_SOURCE_SIDE) {
                val ratio = MAX_SOURCE_SIDE.toFloat() / side
                decoder.setTargetSize((info.size.width * ratio).toInt(), (info.size.height * ratio).toInt())
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun openStream(uri: Uri): InputStream? {
        val path = uri.path
        return if (uri.scheme == "file" && path != null) {
            File(path).inputStream()
        } else {
            context.contentResolver.openInputStream(uri)
        }
    }
}

/** Textos de la postal: título, subtítulo y el del sello (de `strings.xml`). */
private class Caption(val title: String, val subtitle: String, val stamp: String)

private fun Canvas.draw(style: PostcardStyle, photo: Bitmap, text: Caption) {
    val title = text.title
    val subtitle = text.subtitle
    val paper = Paint().apply { color = Mist.toArgb() }
    when (style) {
        PostcardStyle.CLASSIC -> {
            drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paper)
            val box = RectF(MARGIN, MARGIN, WIDTH - MARGIN, MARGIN + PHOTO_HEIGHT)
            drawCropped(photo, box)
            caption(
                title,
                subtitle,
                box.bottom + MARGIN + TITLE_SIZE,
                Ink.toArgb() to InkMuted.toArgb(),
                center = false
            )
            stamp(
                RectF(
                    WIDTH - MARGIN - STAMP_SIZE,
                    box.bottom + MARGIN,
                    WIDTH - MARGIN,
                    box.bottom + MARGIN + STAMP_SIZE
                ),
                text.stamp
            )
        }
        PostcardStyle.POLAROID -> {
            drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paper)
            val side = WIDTH - 2 * MARGIN
            val box = RectF(MARGIN, MARGIN, WIDTH - MARGIN, MARGIN + side)
            drawCropped(photo, box)
            caption(title, subtitle, box.bottom + 2 * MARGIN, Ink.toArgb() to InkMuted.toArgb(), center = true)
        }
        PostcardStyle.PASSPORT -> {
            drawCropped(photo, RectF(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat()))
            val shade = Paint().apply {
                color = Ink.toArgb()
                alpha = SHADE_ALPHA
            }
            val top = HEIGHT - 2 * MARGIN - TITLE_SIZE - SUBTITLE_SIZE - LINE_GAP
            drawRect(0f, top - MARGIN, WIDTH.toFloat(), HEIGHT.toFloat(), shade)
            caption(title, subtitle, top + TITLE_SIZE, Mist.toArgb() to Mist.toArgb(), center = false)
            val corner = RectF(WIDTH - MARGIN - STAMP_SIZE, MARGIN, WIDTH - MARGIN, MARGIN + STAMP_SIZE)
            stamp(corner, text.stamp, Sunset.toArgb())
        }
    }
}

/** La foto llena el recuadro recortando lo que sobra, sin deformarla. */
private fun Canvas.drawCropped(photo: Bitmap, box: RectF) {
    val scale = max(box.width() / photo.width, box.height() / photo.height)
    val width = min(photo.width.toFloat(), box.width() / scale)
    val height = min(photo.height.toFloat(), box.height() / scale)
    val left = ((photo.width - width) / 2).toInt()
    val top = ((photo.height - height) / 2).toInt()
    val src = Rect(left, top, left + width.toInt(), top + height.toInt())
    drawBitmap(photo, src, box, Paint(Paint.FILTER_BITMAP_FLAG))
}

/** `colors`: título y subtítulo (ARGB). */
private fun Canvas.caption(title: String, subtitle: String, baseline: Float, colors: Pair<Int, Int>, center: Boolean) {
    val align = if (center) Paint.Align.CENTER else Paint.Align.LEFT
    val x = if (center) WIDTH / 2f else MARGIN
    val bold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors.first
        textSize = TITLE_SIZE
        typeface = Typeface.DEFAULT_BOLD
        textAlign = align
    }
    val regular = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors.second
        textSize = SUBTITLE_SIZE
        textAlign = align
    }
    drawText(title, x, baseline, bold)
    if (subtitle.isNotBlank()) drawText(subtitle, x, baseline + SUBTITLE_SIZE + LINE_GAP, regular)
}

/** Sello «Wherever» con borde punteado, como en 72_Postal. */
private fun Canvas.stamp(box: RectF, text: String, tint: Int = RouteBlue.toArgb()) {
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = tint
        style = Paint.Style.STROKE
        strokeWidth = STAMP_STROKE
        pathEffect = DashPathEffect(floatArrayOf(DASH, DASH), 0f)
    }
    drawRoundRect(box, CORNER, CORNER, border)
    val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = tint
        textSize = SUBTITLE_SIZE
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    drawText(text, box.centerX(), box.centerY() + SUBTITLE_SIZE / 2, label)
}
