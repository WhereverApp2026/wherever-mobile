package com.wherever.mobile.testing

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.coupons.domain.CouponKind
import com.wherever.mobile.feature.coupons.domain.CouponRepository
import com.wherever.mobile.feature.coupons.domain.CouponsTab
import com.wherever.mobile.feature.coupons.domain.RedeemCode
import com.wherever.mobile.feature.coupons.domain.WalletCoupon

fun sampleWalletCoupon(
    id: String = "w1",
    kind: CouponKind = CouponKind.TWO_FOR_ONE,
    value: Int? = null,
    state: String = "guardado",
    expiresAt: String = "2026-11-28T05:59:59Z"
) = WalletCoupon(id, "c-$id", "p1", "Café Raíz", "2x1 en café de olla", kind, value, state, expiresAt)

/** Cartera de cupones en memoria (nv-29). */
class FakeCouponRepository : CouponRepository {
    val calls = mutableListOf<String>()
    val walletResults = mutableMapOf<CouponsTab, Result<PageResult<WalletCoupon>>>(
        CouponsTab.AVAILABLE to Result.Success(PageResult(listOf(sampleWalletCoupon()), null)),
        CouponsTab.USED to Result.Success(PageResult(emptyList(), null))
    )
    var cache = mutableMapOf<String, WalletCoupon>()
    var conditionsResult: Result<String?> = Result.Success("Lunes a jueves.")
    var redeemResult: Result<RedeemCode> = Result.Success(RedeemCode("K7Q2MX", "2026-11-25T18:05:00Z"))

    override suspend fun wallet(tab: CouponsTab, cursor: String?): Result<PageResult<WalletCoupon>> {
        calls += "wallet ${tab.apiFilter} ${cursor.orEmpty()}".trim()
        return walletResults.getValue(tab)
    }

    override fun cached(id: String): WalletCoupon? = cache[id]

    override suspend fun conditions(couponId: String): Result<String?> {
        calls += "conditions $couponId"
        return conditionsResult
    }

    override suspend fun redeem(walletId: String): Result<RedeemCode> {
        calls += "redeem $walletId"
        return redeemResult
    }
}
