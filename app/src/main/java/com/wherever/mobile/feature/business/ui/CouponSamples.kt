// MagicNumber: datos de ejemplo de 53, 54 y 55 copiados de la maqueta, solo para vistas previas y capturas.
@file:Suppress("MagicNumber")

package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.CapacityConfig
import com.wherever.mobile.feature.business.domain.Coupon
import com.wherever.mobile.feature.business.domain.CouponStatus
import com.wherever.mobile.feature.business.domain.CouponType
import com.wherever.mobile.feature.business.domain.HourSlot
import java.time.LocalDate

internal val SAMPLE_TODAY: LocalDate = LocalDate.of(2026, 10, 17)

internal fun sampleCoupons() = listOf(
    Coupon(
        "c1",
        "2x1 en café",
        CouponType.TWO_FOR_ONE,
        null,
        "Lun a jue, antes de las 19:00",
        null,
        CouponStatus.ACTIVE,
        0,
        18,
        1
    ),
    Coupon(
        "c2",
        "$50 de descuento",
        CouponType.AMOUNT,
        5_000,
        "Mínimo $200, hasta el 31 oct",
        null,
        CouponStatus.ACTIVE,
        50,
        7,
        1
    ),
    Coupon(
        "c3",
        "15% entre semana",
        CouponType.PERCENT,
        15,
        "Solo con reservación",
        null,
        CouponStatus.PAUSED,
        100,
        31,
        2
    ),
    Coupon(
        "c4",
        "Postre de regalo",
        CouponType.GIFT,
        null,
        null,
        "2026-11-30T23:59:59-06:00",
        CouponStatus.SCHEDULED,
        0,
        0,
        1
    )
)

internal fun sampleCouponForm() = CouponFormUiState(
    type = CouponType.PERCENT,
    value = "15",
    title = "15% entre semana",
    days = setOf(1, 2, 3, 4),
    hoursFrom = "12:00",
    hoursTo = "19:00",
    start = SAMPLE_TODAY,
    end = SAMPLE_TODAY.plusDays(30),
    usageLimit = 50
)

internal fun sampleCapacityState() = CapacityUiState(
    loading = false,
    saved = CapacityConfig(capacity = 12, turnMinutes = 60, version = 3),
    capacity = 12,
    turnMinutes = 60,
    days = (0..6).map { SAMPLE_TODAY.plusDays(it.toLong()) },
    date = SAMPLE_TODAY,
    slots = listOf(
        HourSlot("12:00", true, 9),
        HourSlot("13:00", true, 5),
        HourSlot("14:00", true, 1),
        HourSlot("15:00", false, 0),
        HourSlot("16:00", true, 5),
        HourSlot("17:00", true, 8)
    )
)
