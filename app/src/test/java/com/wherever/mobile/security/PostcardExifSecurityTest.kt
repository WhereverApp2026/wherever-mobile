package com.wherever.mobile.security

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.memories.data.AndroidPostcardRenderer
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PHOTO_SIDE = 64

/** 20° 3' 34" N, 99° 20' 35" O (Tula de Allende), en el formato racional de EXIF. */
private const val LATITUDE = "20/1,3/1,34/1"
private const val LONGITUDE = "99/1,20/1,35/1"

/**
 * nv-31 (seguridad): la postal que se sube no lleva EXIF de ubicación. Se toma un JPEG con GPS y se dibuja la
 * postal: el archivo nuevo sale sin coordenadas. Con Robolectric en el paquete `security`, que corre después de
 * `integration` (Conscrypt, ver Memoria.md).
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PostcardExifSecurityTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val io: CoroutineContext = dispatcher
    }

    private fun jpegWithGps(): File {
        val file = File(context.cacheDir, "con_gps.jpg")
        val bitmap = Bitmap.createBitmap(PHOTO_SIDE, PHOTO_SIDE, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.RED)
        }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        ExifInterface(file.absolutePath).apply {
            setAttribute(ExifInterface.TAG_GPS_LATITUDE, LATITUDE)
            setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N")
            setAttribute(ExifInterface.TAG_GPS_LONGITUDE, LONGITUDE)
            setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W")
            saveAttributes()
        }
        assertTrue("la foto de prueba debe tener GPS", ExifInterface(file.absolutePath).getLatLong(FloatArray(2)))
        return file
    }

    @Test
    fun `la postal dibujada no lleva la ubicacion de la foto`() = runTest(dispatcher) {
        val source = Uri.fromFile(jpegWithGps()).toString()
        val renderer = AndroidPostcardRenderer(context, dispatchers)

        PostcardStyle.entries.forEach { style ->
            val result = renderer.render(source, style, "Saludos desde Café Raíz", "Tula de Allende")

            check(result is Result.Success) { "no se pudo dibujar la postal $style: $result" }
            val output = File(Uri.parse(result.data).path!!)
            val exif = ExifInterface(output.absolutePath)
            assertFalse("$style no debe traer GPS", exif.getLatLong(FloatArray(2)))
            assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE))
            assertEquals(1080, exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 1080))
        }
    }
}
