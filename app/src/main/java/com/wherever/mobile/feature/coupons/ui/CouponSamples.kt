package com.wherever.mobile.feature.coupons.ui

import com.wherever.mobile.feature.coupons.domain.CouponKind
import com.wherever.mobile.feature.coupons.domain.CouponsTab
import com.wherever.mobile.feature.coupons.domain.Expiry
import com.wherever.mobile.feature.coupons.domain.RedeemCode
import com.wherever.mobile.feature.coupons.domain.WalletCoupon
import java.time.LocalDate

// Datos solo para las vistas previas y las capturas: los textos de la maqueta (29_Cupones, DetCupon · *).

private const val PREVIEW_AMOUNT_CENTS = 5_000
private const val PREVIEW_PERCENT = 15
private const val PREVIEW_SECONDS = 272L
private const val PREVIEW_IN_DAYS = 3
private const val PREVIEW_YEAR = 2026
private const val PREVIEW_OCT = 10
private const val PREVIEW_NOV = 11
private const val PREVIEW_DAY_30 = 30
private const val PREVIEW_DAY_15 = 15

internal val PreviewTwoForOne = WalletCoupon(
    "w1", "c1", "p1", "Café Raíz", "2x1 en café de olla", CouponKind.TWO_FOR_ONE, null, "guardado", ""
)
internal val PreviewAmount = WalletCoupon(
    "w2", "c2", "p2", "Fonda Doña Meche", "$50 en tu consumo", CouponKind.AMOUNT, PREVIEW_AMOUNT_CENTS, "guardado", ""
)
internal val PreviewPercent = WalletCoupon(
    "w3", "c3", "p3", "Jardín del Cerro", "15% en tu cuenta", CouponKind.PERCENT, PREVIEW_PERCENT, "guardado", ""
)
internal val PreviewGift = WalletCoupon(
    "w4", "c4", "p1", "Café Raíz", "Postre de cortesía", CouponKind.GIFT, null, "guardado", ""
)

internal const val PREVIEW_CONDITIONS =
    "Lunes a jueves, antes de las 19:00. Un cupón por mesa. No acumulable con otras promociones."

internal fun previewCouponsState() = CouponsUiState(
    loading = false,
    pages = mapOf(
        CouponsTab.AVAILABLE to CouponsPage(
            listOf(
                CouponCard(PreviewTwoForOne, Expiry.InDays(PREVIEW_IN_DAYS)),
                CouponCard(PreviewAmount, Expiry.Today),
                CouponCard(PreviewPercent, Expiry.OnDate(LocalDate.of(PREVIEW_YEAR, PREVIEW_OCT, PREVIEW_DAY_30))),
                CouponCard(PreviewGift, Expiry.OnDate(LocalDate.of(PREVIEW_YEAR, PREVIEW_NOV, PREVIEW_DAY_15)))
            ),
            null
        ),
        CouponsTab.USED to CouponsPage(listOf(CouponCard(PreviewAmount.copy(state = USED_STATE), Expiry.Expired)), null)
    )
)

internal fun previewDetailState(coupon: WalletCoupon, expiry: Expiry, withCode: Boolean = false) = CouponDetailUiState(
    loading = false,
    coupon = coupon,
    expiry = expiry,
    conditions = PREVIEW_CONDITIONS,
    code = if (withCode) RedeemCode("K7Q2MX", "") else null,
    secondsLeft = if (withCode) PREVIEW_SECONDS else 0
)
