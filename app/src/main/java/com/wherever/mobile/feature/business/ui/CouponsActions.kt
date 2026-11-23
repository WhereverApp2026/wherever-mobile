package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.Coupon
import com.wherever.mobile.feature.business.domain.CouponGroup

/** Eventos de 53_NegCupones. */
internal data class CouponsActions(
    val onTab: (CouponGroup) -> Unit = {},
    val onToggle: (Coupon) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onCreate: () -> Unit = {},
    val onMessageShown: () -> Unit = {}
)
