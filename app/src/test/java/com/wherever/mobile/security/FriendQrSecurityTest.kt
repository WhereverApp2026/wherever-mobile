package com.wherever.mobile.security

import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.wherever.mobile.core.common.QrMatrix
import com.wherever.mobile.feature.friends.domain.FriendQr
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * nv-20 (Security): el QR de 25_MiCodigo lleva solo `codigo_amigo` (sin tokens ni datos personales) y lo
 * escaneado se trata como entrada no confiable: solo un código válido llega a `GET /personas/codigo/{codigo}`.
 */
class FriendQrSecurityTest {
    @Test
    fun `el QR generado se lee y contiene solo el prefijo y el codigo`() {
        val payload = FriendQr.payload("k7m2p9qx")

        assertEquals("wherever:amigo:K7M2P9QX", payload)
        assertEquals(payload, decode(QrMatrix.encode(payload)))
        listOf("@", "token", "Bearer", "eyJ", "http").forEach { assertFalse(it, payload.contains(it, true)) }
    }

    @Test
    fun `no se puede generar un QR con algo que no sea un codigo`() {
        listOf("", "ana@correo.com", "eyJhbGciOi.x.y", "K7M2P9Q0", "../../admin").forEach { bad ->
            assertTrue(bad, runCatching { FriendQr.payload(bad) }.isFailure)
        }
    }

    @Test
    fun `lo escaneado que no es un codigo se descarta`() {
        val hostile = listOf(
            "https://phishing.example/wherever?codigo=K7M2P9QX",
            "javascript:alert(1)",
            "wherever:amigo:K7M2P9QX/../../usuarios/yo",
            "wherever:amigo:K7M2P9QX?x=1",
            "K7M2P9QX%2F..",
            "wherever:amigo:" + "A".repeat(FriendQr.MAX_SCANNED_LENGTH),
            "WIFI:S:red;T:WPA;P:clave;;",
            "   "
        )
        hostile.forEach { assertNull(it, FriendQr.parse(it)) }
        assertEquals("K7M2P9QX", FriendQr.parse("  WHEREVER:AMIGO:k7m2p9qx  "))
        assertEquals("K7M2P9QX", FriendQr.parse("k7m2p9qx"))
    }

    @Test
    fun `el escaneo no abre enlaces ni registra lo leido`() {
        val screen = File("src/main/java/com/wherever/mobile/feature/friends/ui/MyCodeScreen.kt").readText()
        val viewModel = File("src/main/java/com/wherever/mobile/feature/friends/ui/MyCodeViewModel.kt").readText()
        assertFalse(screen.contains("ACTION_VIEW"))
        listOf("Log.", "println", "Timber").forEach { assertFalse(it, viewModel.contains(it)) }
        assertTrue(viewModel.contains("FriendQr.parse(raw)"))
    }

    private fun decode(matrix: QrMatrix): String {
        val scale = 4
        val quiet = 4
        val side = (matrix.size + quiet * 2) * scale
        val pixels = IntArray(side * side) { index ->
            val x = index % side / scale - quiet
            val y = index / side / scale - quiet
            val dark = x in 0 until matrix.size && y in 0 until matrix.size && matrix[x, y]
            if (dark) BLACK else WHITE
        }
        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(side, side, pixels)))
        return QRCodeReader().decode(bitmap).text
    }

    private companion object {
        const val BLACK = 0xFF000000.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
    }
}
