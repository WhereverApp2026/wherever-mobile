package com.wherever.mobile.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.wherever.mobile.R

private fun variableFont(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

/** Sora: títulos. */
val SoraFamily = FontFamily(
    variableFont(R.font.sora, FontWeight.SemiBold),
    variableFont(R.font.sora, FontWeight.Bold)
)

/** DM Sans: cuerpo y controles. */
val DmSansFamily = FontFamily(
    variableFont(R.font.dm_sans, FontWeight.Normal),
    variableFont(R.font.dm_sans, FontWeight.Medium),
    variableFont(R.font.dm_sans, FontWeight.SemiBold)
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
