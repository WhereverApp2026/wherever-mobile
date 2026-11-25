package com.wherever.mobile.feature.coupons.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.feature.coupons.domain.CouponKind
import com.wherever.mobile.feature.coupons.domain.Expiry
import com.wherever.mobile.feature.coupons.domain.WalletCoupon
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DAY_MONTH = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-MX"))

/** Color del valor en el boleto, como en 29_Cupones. */
internal fun CouponKind.color(): Color = when (this) {
    CouponKind.TWO_FOR_ONE -> RouteBlue
    CouponKind.AMOUNT -> Sunset
    CouponKind.PERCENT -> Mint
    CouponKind.GIFT -> Gold
}

/** Texto sobre el color: oscuro sobre el dorado, claro sobre los demás (contraste de la maqueta). */
internal fun CouponKind.onColor(): Color = if (this == CouponKind.GIFT ||
    this == CouponKind.AMOUNT
) {
    Ink
} else {
    Color.White
}

@Composable
internal fun badgeText(coupon: WalletCoupon): String = coupon.badge ?: stringResource(R.string.coupons_badge_gift)

@Composable
internal fun expiryText(expiry: Expiry): String = when (expiry) {
    Expiry.Today -> stringResource(R.string.coupons_expires_today)
    is Expiry.InDays -> pluralStringResource(R.plurals.coupons_expires_in_days, expiry.days, expiry.days)
    is Expiry.OnDate -> stringResource(R.string.coupons_expires_on, expiry.date.format(DAY_MONTH))
    Expiry.Expired -> stringResource(R.string.coupons_expired)
    Expiry.Unknown -> ""
}

/** El boleto: valor a la izquierda en su color y los datos a la derecha. `action` va abajo a la derecha. */
@Composable
internal fun CouponTicket(
    coupon: WalletCoupon,
    expiry: Expiry,
    modifier: Modifier = Modifier,
    used: Boolean = false,
    action: (@Composable () -> Unit)? = null
) {
    SurfaceCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(Sizes.LabelColumn)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = Radius.Card, bottomStart = Radius.Card))
                    .background(if (used) MaterialTheme.colorScheme.surfaceVariant else coupon.kind.color())
                    .padding(vertical = Spacing.XXL)
            ) {
                Text(
                    badgeText(coupon),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (used) MaterialTheme.colorScheme.onSurfaceVariant else coupon.kind.onColor(),
                    maxLines = 1
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.XS),
                modifier = Modifier.weight(1f).padding(Spacing.L)
            ) {
                Text(
                    coupon.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    coupon.placeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val text = if (used) stringResource(R.string.coupons_used_label) else expiryText(expiry)
                    Text(
                        text,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (!used && expiry.soon) Sunset else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f).testTag("coupon_expiry_${coupon.id}")
                    )
                    action?.invoke()
                }
            }
        }
    }
}

@Composable
internal fun UseLink(onClick: () -> Unit, tag: String) {
    WhereverTextLink(stringResource(R.string.coupons_use), onClick, modifier = Modifier.testTag(tag))
}
