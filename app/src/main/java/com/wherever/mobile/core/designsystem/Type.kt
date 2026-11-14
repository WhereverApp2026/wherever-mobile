package com.wherever.mobile.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

private val base = Typography()

val WhereverTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = SoraFamily),
    displayMedium = base.displayMedium.copy(fontFamily = SoraFamily),
    displaySmall = base.displaySmall.copy(fontFamily = SoraFamily),
    headlineLarge = base.headlineLarge.copy(fontFamily = SoraFamily, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = SoraFamily, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = SoraFamily, fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontFamily = SoraFamily, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontFamily = SoraFamily, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = SoraFamily, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = DmSansFamily),
    bodyMedium = base.bodyMedium.copy(fontFamily = DmSansFamily),
    bodySmall = base.bodySmall.copy(fontFamily = DmSansFamily),
    labelLarge = base.labelLarge.copy(fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = DmSansFamily, fontWeight = FontWeight.Medium),
    labelSmall = base.labelSmall.copy(fontFamily = DmSansFamily, fontWeight = FontWeight.Medium)
)
