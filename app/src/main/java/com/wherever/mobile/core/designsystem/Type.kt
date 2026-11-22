package com.wherever.mobile.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.wherever.mobile.R

// Fuentes estáticas, una por peso, sacadas de las variables con fontTools (OFL).
// Con la fuente variable y FontVariation, Android dibujaba todo en peso 400 (specs/nv-45 §2).

/** Sora: títulos. */
val SoraFamily = FontFamily(
    Font(R.font.sora_semibold, FontWeight.SemiBold),
    Font(R.font.sora_bold, FontWeight.Bold)
)

/** DM Sans (tamaño óptico 14): cuerpo y controles. */
val DmSansFamily = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_semibold, FontWeight.SemiBold),
    Font(R.font.dm_sans_bold, FontWeight.Bold)
)

// Escala del Boceto 2 (docs/diseno_boceto2.md §3): títulos solo en Sora Bold; el resto en DM Sans
// Regular o Bold; espaciado de letras 0 en todo el archivo.
private fun style(family: FontFamily, weight: FontWeight, size: TextUnit, lineHeight: TextUnit) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = 0.sp
)

private fun sora(size: TextUnit, lineHeight: TextUnit) = style(SoraFamily, FontWeight.Bold, size, lineHeight)

private fun dmSans(weight: FontWeight, size: TextUnit, lineHeight: TextUnit) =
    style(DmSansFamily, weight, size, lineHeight)

private val base = Typography()

val WhereverTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = SoraFamily, fontWeight = FontWeight.Bold),
    displayMedium = base.displayMedium.copy(fontFamily = SoraFamily, fontWeight = FontWeight.Bold),
    displaySmall = base.displaySmall.copy(fontFamily = SoraFamily, fontWeight = FontWeight.Bold),
    headlineLarge = sora(28.sp, 36.sp),
    headlineMedium = sora(24.sp, 32.sp),
    headlineSmall = sora(20.sp, 28.sp),
    titleLarge = sora(19.sp, 26.sp),
    titleMedium = sora(17.sp, 24.sp),
    titleSmall = sora(16.sp, 22.sp),
    bodyLarge = dmSans(FontWeight.Normal, 16.sp, 24.sp),
    bodyMedium = dmSans(FontWeight.Normal, 14.sp, 20.sp),
    bodySmall = dmSans(FontWeight.Normal, 12.sp, 16.sp),
    // labelLarge sirve para etiquetas de campo, enlaces y chips (14 Bold en Figma); los botones usan ButtonText.
    labelLarge = dmSans(FontWeight.Bold, 14.sp, 20.sp),
    labelMedium = dmSans(FontWeight.Bold, 13.sp, 18.sp),
    labelSmall = dmSans(FontWeight.Bold, 12.sp, 16.sp)
)

/** Texto de los botones principal y secundario: DM Sans Bold 17 (Boceto 2 §4.2). */
val ButtonText: TextStyle = dmSans(FontWeight.Bold, 17.sp, 22.sp)
