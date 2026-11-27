package com.wherever.mobile.feature.coupons.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.coupons.domain.CouponKind
import com.wherever.mobile.feature.coupons.domain.CouponRepository
import com.wherever.mobile.feature.coupons.domain.CouponsTab
import com.wherever.mobile.feature.coupons.domain.RedeemCode
import com.wherever.mobile.feature.coupons.domain.WalletCoupon
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val PAGE_SIZE = 20

/**
 * Cupones contra la API real (D-29b: sin Room). La cartera leída queda en memoria solo para abrir el detalle;
 * el código de canje nunca se guarda.
 */
@Singleton
class CouponRepositoryImpl @Inject constructor(private val api: CouponsApi) : CouponRepository {
    private val wallet = ConcurrentHashMap<String, WalletCoupon>()

    override suspend fun wallet(tab: CouponsTab, cursor: String?): Result<PageResult<WalletCoupon>> =
        safeApiCall { api.wallet(tab.apiFilter, cursor, PAGE_SIZE) }.map { page ->
            val items = page.items.map { it.toDomain() }
            items.forEach { wallet[it.id] = it }
            PageResult(items, page.nextCursor?.takeIf { it.isNotBlank() })
        }

    override fun cached(id: String): WalletCoupon? = wallet[id]

    override suspend fun conditions(couponId: String): Result<String?> =
        when (val result = safeApiCall { api.coupon(couponId) }) {
            is Result.Success -> Result.Success(result.data.conditions?.takeIf { it.isNotBlank() })
            // D-29d: un cupón que ya no está vigente da 404; el detalle sigue sin condiciones.
            is Result.Failure -> if (result.error is AppError.NotFound) Result.Success(null) else result
        }

    override suspend fun redeem(walletId: String): Result<RedeemCode> =
        safeApiCall { api.redeem(walletId) }.map { RedeemCode(it.code, it.expiresAt) }
}

private fun WalletCouponDto.toDomain() = WalletCoupon(
    id = id,
    couponId = couponId,
    placeId = placeId,
    placeName = placeName,
    title = title,
    kind = CouponKind.fromApi(kind),
    value = value,
    state = state,
    expiresAt = expiresAt
)
