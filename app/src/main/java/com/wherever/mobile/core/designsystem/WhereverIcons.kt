package com.wherever.mobile.core.designsystem

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Íconos de la maqueta que no trae `material-icons-core` (sin sumar la librería extendida). */
@Suppress("MagicNumber", "HardcodedDesignValue")
object WhereverIcons {
    /** Flecha hacia una bandeja: Guardar imagen. */
    val Download: ImageVector by lazy {
        outline("Download") {
            moveTo(12f, 4f)
            lineTo(12f, 15f)
            moveTo(7f, 10f)
            lineTo(12f, 15f)
            lineTo(17f, 10f)
            moveTo(5f, 20f)
            lineTo(19f, 20f)
        }
    }

    /** Esquinas de visor con una línea: Escanear. */
    val QrScan: ImageVector by lazy {
        outline("QrScan") {
            moveTo(4f, 9f)
            lineTo(4f, 4f)
            lineTo(9f, 4f)
            moveTo(15f, 4f)
            lineTo(20f, 4f)
            lineTo(20f, 9f)
            moveTo(20f, 15f)
            lineTo(20f, 20f)
            lineTo(15f, 20f)
            moveTo(9f, 20f)
            lineTo(4f, 20f)
            lineTo(4f, 15f)
            moveTo(7f, 12f)
            lineTo(17f, 12f)
        }
    }

    private fun outline(name: String, block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Ink),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block
        ).build()
}
