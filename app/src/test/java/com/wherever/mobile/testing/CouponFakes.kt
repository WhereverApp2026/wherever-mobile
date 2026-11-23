package com.wherever.mobile.testing

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.business.domain.BusinessCouponsRepository
import com.wherever.mobile.feature.business.domain.CapacityConfig
import com.wherever.mobile.feature.business.domain.Coupon
import com.wherever.mobile.feature.business.domain.CouponStatus
import com.wherever.mobile.feature.business.domain.CouponType
import com.wherever.mobile.feature.business.domain.HourSlot
import com.wherever.mobile.feature.business.domain.NewCoupon
import java.time.LocalDate

fun sampleCoupon(id: String = "c1", status: CouponStatus = CouponStatus.ACTIVE, version: Long = 1) =
    Coupon(id, "2x1 en café", CouponType.TWO_FOR_ONE, null, null, null, status, 0, 3, version)

/** Cupones y cupo en memoria (nv-37). */
class FakeBusinessCouponsRepository : BusinessCouponsRepository {
    val calls = mutableListOf<String>()
    var couponsResult: Result<List<Coupon>> = Result.Success(
        listOf(
            sampleCoupon("c1"),
            sampleCoupon("c2", CouponStatus.PAUSED),
            sampleCoupon("c3", CouponStatus.SCHEDULED),
            sampleCoupon("c4", CouponStatus.ENDED)
        )
    )
    var setResult: Result<Coupon>? = null
    var created: NewCoupon? = null
    var createResult: Result<Coupon> = Result.Success(sampleCoupon("c9"))
    var titleResult: Result<String> = Result.Success("2x1 de tarde")
    var capacityResult: Result<CapacityConfig> = Result.Success(CapacityConfig(12, 60, 3))
    var saveResult: Result<CapacityConfig>? = null
    var savedConfig: CapacityConfig? = null
    val availabilityDates = mutableListOf<LocalDate>()

    override suspend fun coupons(businessId: String) = couponsResult.also { calls += "cupones" }

    override suspend fun create(businessId: String, coupon: NewCoupon): Result<Coupon> {
        created = coupon
        return createResult
    }

    override suspend fun setActive(businessId: String, coupon: Coupon, active: Boolean): Result<Coupon> {
        calls += "estado ${coupon.id} $active"
        return setResult ?: Result.Success(
            coupon.copy(status = if (active) CouponStatus.ACTIVE else CouponStatus.PAUSED, version = coupon.version + 1)
        )
    }

    override suspend fun suggestTitle(businessId: String, type: CouponType, value: Long?, description: String?) =
        titleResult.also { calls += "titulo ${type.apiValue}" }

    override suspend fun capacity(businessId: String) = capacityResult.also { calls += "config" }

    override suspend fun saveCapacity(businessId: String, config: CapacityConfig): Result<CapacityConfig> {
        savedConfig = config
        return saveResult ?: Result.Success(config.copy(version = config.version + 1))
    }

    override suspend fun availability(businessId: String, date: LocalDate): Result<List<HourSlot>> {
        availabilityDates += date
        return Result.Success(listOf(HourSlot("12:00", true, 9), HourSlot("13:00", false, 0)))
    }
}
