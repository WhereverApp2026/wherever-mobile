@file:Suppress("MagicNumber")

package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.LogoMark
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme

/** 01_Bienvenida */
@Composable
fun WelcomeScreen(onCreateAccount: () -> Unit, onHaveAccount: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(RouteBlue, RouteBlue, Ink)))
    ) {
        RouteIllustration(Modifier.fillMaxWidth().height(360.dp))
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(ScreenPadding),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LogoMark(size = 40.dp, background = Color.White, tint = RouteBlue)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(24.dp))
            WhereverPrimaryButton(
                text = stringResource(R.string.welcome_create_account),
                onClick = onCreateAccount,
                tag = "welcome_create_account"
            )
            Spacer(Modifier.height(12.dp))
            WhereverOutlinedButton(
                text = stringResource(R.string.welcome_have_account),
                onClick = onHaveAccount,
                contentColor = Color.White,
                borderColor = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                tag = "welcome_have_account"
            )
        }
    }
}

/** La ruta punteada con tres paradas. Las cifras son la geometría del dibujo, en fracciones del lienzo. */
@Suppress("MagicNumber")
@Composable
private fun RouteIllustration(modifier: Modifier = Modifier) {
    val pin = rememberVectorPainter(Icons.Filled.Place)
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

@Preview
@Composable
private fun WelcomePreview() {
    WhereverTheme(darkTheme = false) { WelcomeScreen({}, {}) }
}
