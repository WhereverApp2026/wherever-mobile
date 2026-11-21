package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.wherever.mobile.core.common.QrMatrix
import kotlin.math.floor

/**
 * Código QR de 25_MiCodigo. Siempre oscuro sobre blanco, también en el tema oscuro: un QR invertido
 * lo leen pocos escáneres.
 */
@Composable
fun QrCode(content: String, description: String, modifier: Modifier = Modifier) {
    val matrix = remember(content) { QrMatrix.encode(content) }
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .border(Strokes.Hairline, OutlineSoft, WhereverShapes.Card)
            .background(Color.White, WhereverShapes.Card)
            .padding(Spacing.L)
            .semantics { contentDescription = description }
    ) {
        // Celdas de píxeles enteros: con fracciones el antialias deja rayas claras entre módulos.
        val cell = floor(size.minDimension / matrix.size)
        val origin = floor((size.minDimension - cell * matrix.size) / 2)
        for (y in 0 until matrix.size) {
            for (x in 0 until matrix.size) {
                if (matrix[x, y]) drawRect(Ink, Offset(origin + x * cell, origin + y * cell), Size(cell, cell))
            }
        }
    }
}
