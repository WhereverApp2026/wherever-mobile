package com.wherever.mobile.feature.coupons.domain

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Días antes del vencimiento en que un cupón se marca "vence pronto" (29_Cupones). */
const val EXPIRING_SOON_DAYS = 3L

private const val CENTS_PER_PESO = 100

/** Tipo de cupón de la API (nv-61). Cada uno tiene su color en el boleto. */
enum class CouponKind(val apiValue: String) {
    TWO_FOR_ONE("dos_por_uno"),
    AMOUNT("monto"),
    PERCENT("porcentaje"),
    GIFT("regalo");

    companion object {
        fun fromApi(value: String): CouponKind = entries.firstOrNull { it.apiValue == value } ?: GIFT
    }
}

/** Pestañas de 29_Cupones con su `filtro` de `GET /mis-cupones`. */
enum class CouponsTab(val apiFilter: String) { AVAILABLE("vigentes"), USED("usados") }

/** Cupón de la cartera (`GET /mis-cupones`). `value`: centavos en [CouponKind.AMOUNT], 1–100 en porcentaje. */
data class WalletCoupon(
    val id: String,
    val couponId: String,
    val placeId: String,
    val placeName: String,
    val title: String,
    val kind: CouponKind,
    val value: Int?,
    val state: String,
    val expiresAt: String
) {
    /** Lo que va grande en el boleto: "2x1", "$50", "15%" o null para regalo (la UI dice "Gratis"). */
    val badge: String?
        get() = when (kind) {
            CouponKind.TWO_FOR_ONE -> "2x1"
            CouponKind.AMOUNT -> value?.let { "$" + it / CENTS_PER_PESO }
            CouponKind.PERCENT -> value?.let { "$it%" }
            CouponKind.GIFT -> null
        }
}

/** Código de canje de `POST /mis-cupones/{id}/canjear`: vive hasta [expiresAt] (5 min). */
data class RedeemCode(val code: String, val expiresAt: String)

/** Cómo se dice la vigencia en la tarjeta. */
sealed interface Expiry {
    data object Today : Expiry

    data class InDays(val days: Int) : Expiry

    data class OnDate(val date: LocalDate) : Expiry

    data object Expired : Expiry

    data object Unknown : Expiry

    /** Hoy o en 3 días o menos: va en naranja. */
    val soon: Boolean get() = this == Today || (this is InDays && days <= EXPIRING_SOON_DAYS)
}

/** La vigencia según el reloj inyectado, en la zona del teléfono (spec §2, validaciones). */
fun expiryOf(expiresAt: String, clock: Clock): Expiry {
    val instant = runCatching { Instant.parse(expiresAt) }.getOrNull() ?: return Expiry.Unknown
    if (!instant.isAfter(clock.instant())) return Expiry.Expired
    val zone: ZoneId = clock.zone
    val today = LocalDate.now(clock)
    val day = instant.atZone(zone).toLocalDate()
    val days = ChronoUnit.DAYS.between(today, day)
    return when {
        days <= 0 -> Expiry.Today
        days <= EXPIRING_SOON_DAYS -> Expiry.InDays(days.toInt())
        else -> Expiry.OnDate(day)
    }
}

/** Segundos que le quedan al código de canje; 0 si ya venció o no se entiende la fecha. */
fun secondsLeft(code: RedeemCode, clock: Clock): Long {
    val until = runCatching { Instant.parse(code.expiresAt) }.getOrNull() ?: return 0
    return ChronoUnit.SECONDS.between(clock.instant(), until).coerceAtLeast(0)
}

/** Cupones del consumidor (specs/nv-29). La implementación vive en `data`. */
interface CouponRepository {
    suspend fun wallet(tab: CouponsTab, cursor: String? = null): Result<PageResult<WalletCoupon>>

    /** El último cupón leído de la cartera; null si no se ha cargado (se vuelve a pedir la lista). */
    fun cached(id: String): WalletCoupon?

    /** Condiciones del cupón público (`GET /cupones/{id}`); null si ya no está vigente (404). */
    suspend fun conditions(couponId: String): Result<String?>

    suspend fun redeem(walletId: String): Result<RedeemCode>
}
