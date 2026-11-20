package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp

// Ilustraciones de la maqueta (01_Bienvenida). Viven aquí porque su geometría es parte del diseño.

/** La ruta punteada con tres paradas. Las cifras son la geometría del dibujo, en fracciones del lienzo. */
@Suppress("MagicNumber")
@Composable
fun RouteIllustration(modifier: Modifier = Modifier) {
    val pin = rememberVectorPainter(Icons.Outlined.Place)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, h * 0.82f)
            cubicTo(w * 0.15f, h * 0.75f, w * 0.15f, h * 0.55f, w * 0.3f, h * 0.55f)
            cubicTo(w * 0.42f, h * 0.55f, w * 0.48f, h * 0.68f, w * 0.58f, h * 0.65f)
            cubicTo(w * 0.72f, h * 0.6f, w * 0.72f, h * 0.35f, w * 0.85f, h * 0.3f)
            cubicTo(w * 0.92f, h * 0.27f, w * 0.97f, h * 0.27f, w, h * 0.26f)
        }
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.6f),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 8.dp.toPx()))
            )
        )
        val cafe = Offset(w * 0.22f, h * 0.6f)
        val place = Offset(w * 0.55f, h * 0.66f)
        val goal = Offset(w * 0.81f, h * 0.38f)
        stop(cafe, Color.White)
        cupIcon(cafe, RouteBlue)
        stop(place, Sunset)
        val pinSize = 28.dp.toPx()
        translate(place.x - pinSize / 2, place.y - pinSize / 2) {
            with(pin) { draw(Size(pinSize, pinSize), colorFilter = ColorFilter.tint(Ink)) }
        }
        stop(goal, Mint)
        targetIcon(goal, Color.White)
    }
}

private fun DrawScope.stop(center: Offset, fill: Color) {
    drawCircle(Color.White, radius = 28.dp.toPx(), center = center)
    drawCircle(fill, radius = 24.dp.toPx(), center = center)
}

@Suppress("MagicNumber") // Geometría del ícono en dp.
private fun DrawScope.cupIcon(center: Offset, color: Color) {
    val unit = 1.dp.toPx()
    val stroke = Stroke(width = 2 * unit, cap = StrokeCap.Round)
    drawRoundRect(
        color = color,
        topLeft = center + Offset(-10 * unit, -7 * unit),
        size = Size(15 * unit, 15 * unit),
        cornerRadius = CornerRadius(4 * unit),
        style = stroke
    )
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = center + Offset(2 * unit, -4 * unit),
        size = Size(8 * unit, 8 * unit),
        style = stroke
    )
}

@Suppress("MagicNumber") // Geometría del ícono en dp.
private fun DrawScope.targetIcon(center: Offset, color: Color) {
    val unit = 1.dp.toPx()
    drawCircle(color, radius = 11 * unit, center = center, style = Stroke(width = 2 * unit))
    drawCircle(color, radius = 4 * unit, center = center)
}
