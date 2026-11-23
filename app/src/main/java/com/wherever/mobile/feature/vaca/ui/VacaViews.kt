package com.wherever.mobile.feature.vaca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.Mist
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.feature.vaca.domain.CENTS_PER_PESO
import java.text.NumberFormat
import java.util.Locale

/** Colores de los círculos de cada integrante, en el orden de la maqueta (Tú, Jorge, Mariana, Diego). */
private val AvatarColors = listOf(RouteBlue, Mint, Gold, Ink)

internal fun avatarColor(index: Int): Color = AvatarColors[index % AvatarColors.size]

/** Centavos a pesos: «$760» o «$70.50». */
internal fun Long.asPesos(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"))
    val whole = this % CENTS_PER_PESO == 0L
    format.minimumFractionDigits = if (whole) 0 else 2
    format.maximumFractionDigits = if (whole) 0 else 2
    return format.format(this.toDouble() / CENTS_PER_PESO)
}

/** Círculo de color con la inicial del integrante. */
@Composable
internal fun MemberAvatar(name: String, color: Color, size: Dp) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size).background(color, CircleShape)) {
        Text(
            name.trim().take(1).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (color == Gold) Ink else Mist
        )
    }
}
