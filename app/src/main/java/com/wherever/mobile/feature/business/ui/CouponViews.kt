package com.wherever.mobile.feature.business.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.Mist
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.feature.business.domain.CENTS
import com.wherever.mobile.feature.business.domain.CouponType

private val TicketColors = listOf(RouteBlue, Sunset, Mint)

/** Color del talón de cada cupón, en el orden de la maqueta (azul, naranja, verde). */
internal fun ticketColor(index: Int): Color = TicketColors[index % TicketColors.size]

/** «2x1», «15%», «$50» o «Regalo». */
@Composable
internal fun couponBadge(type: CouponType, value: Long?): String = when (type) {
    CouponType.TWO_FOR_ONE -> stringResource(R.string.coupon_badge_2x1)
    CouponType.GIFT -> stringResource(R.string.coupon_badge_gift)
    CouponType.PERCENT -> stringResource(R.string.business_percent, (value ?: 0L).toInt())
    CouponType.AMOUNT -> ((value ?: 0L)).asMoney().takeIf { value != null } ?: stringResource(R.string.business_dash)
}

/** Talón de color con el valor y el cuerpo del cupón a la derecha (53_NegCupones y la vista previa de 54). */
@Composable
internal fun CouponTicket(
    badge: String,
    color: Color,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    SurfaceCard(modifier = modifier) {
        Row(modifier = Modifier.height(IntrinsicSize.Min).padding(vertical = Spacing.None)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.width(
                    Sizes.ThumbnailL
                ).fillMaxHeight().heightIn(min = Sizes.ThumbnailL).background(color)
            ) {
                Text(badge, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Mist)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = Spacing.M, vertical = Spacing.M)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                footer?.invoke()
            }
            trailing?.let {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight().padding(end = Spacing.M)) {
                    it()
                }
            }
        }
    }
}

/** Pesos enteros escritos a centavos (para la vista previa). */
internal fun pesosToCents(text: String): Long? = text.toLongOrNull()?.times(CENTS)
