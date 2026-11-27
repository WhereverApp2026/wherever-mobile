package com.wherever.mobile.feature.coupons.ui

import com.wherever.mobile.feature.coupons.domain.CouponsTab
import com.wherever.mobile.feature.coupons.domain.WalletCoupon

/** Lo que puede pedir 29_Cupones; la navegación la resuelve el NavHost. */
data class CouponsActions(
    val onBack: () -> Unit = {},
    val onTab: (CouponsTab) -> Unit = {},
    val onOpen: (WalletCoupon) -> Unit = {},
    val onExplore: () -> Unit = {},
    val onLoadMore: () -> Unit = {},
    val onRetry: () -> Unit = {}
)

/** Lo que puede pedir 47_DetCupon. Copiar lo resuelve la pantalla (portapapeles). */
data class CouponDetailActions(
    val onBack: () -> Unit = {},
    val onRedeem: () -> Unit = {},
    val onCopy: (String) -> Unit = {},
    val onReserve: (WalletCoupon) -> Unit = {},
    val onPlace: (WalletCoupon) -> Unit = {},
    val onRetry: () -> Unit = {}
)
