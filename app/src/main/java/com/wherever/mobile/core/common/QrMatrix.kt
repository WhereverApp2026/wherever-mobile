package com.wherever.mobile.core.common

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder

/** Módulos de un código QR (ZXing), sin zona de silencio: `true` es un módulo oscuro. */
class QrMatrix private constructor(val size: Int, private val cells: BooleanArray) {
    operator fun get(x: Int, y: Int): Boolean = cells[y * size + x]

    companion object {
        /** Corrección M (15 %): aguanta una pantalla con brillo bajo o un reflejo. */
        fun encode(content: String): QrMatrix {
            val matrix = Encoder.encode(content, ErrorCorrectionLevel.M).matrix
            val size = matrix.width
            val cells = BooleanArray(size * size) { index -> matrix.get(index % size, index / size).toInt() == 1 }
            return QrMatrix(size, cells)
        }
    }
}
