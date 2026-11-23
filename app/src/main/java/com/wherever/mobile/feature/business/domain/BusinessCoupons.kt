package com.wherever.mobile.feature.business.domain

import com.wherever.mobile.core.common.Result
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

/** Título de un cupón: 1 a 40 caracteres. */
const val COUPON_TITLE_MAX = 40

/** Porcentaje de descuento: 1 a 100. */
const val COUPON_PERCENT_MAX = 100

/** Cupo por horario: 1 a 500 personas. */
const val CAPACITY_MAX = 500

/** Duración del turno: 15 a 240 minutos, en múltiplos de 15. */
const val TURN_STEP = 15
const val TURN_MAX = 240

/** Tipo de cupón (`tipo`). 2x1 y regalo no llevan valor. */
enum class CouponType(val apiValue: String, val hasValue: Boolean) {
    TWO_FOR_ONE("dos_por_uno", false),
    PERCENT("porcentaje", true),
    AMOUNT("monto", true),
    GIFT("regalo", false);

    companion object {
        fun from(value: String?): CouponType = entries.firstOrNull { it.apiValue == value } ?: PERCENT
    }
}

/** Pestañas de 53_NegCupones; los pausados van con los activos (su interruptor está apagado). */
enum class CouponGroup { ACTIVE, SCHEDULED, ENDED }

enum class CouponStatus(val apiValue: String, val group: CouponGroup) {
    ACTIVE("activo", CouponGroup.ACTIVE),
    PAUSED("pausado", CouponGroup.ACTIVE),
    SCHEDULED("programado", CouponGroup.SCHEDULED),
    ENDED("terminado", CouponGroup.ENDED);

    companion object {
        /** Lo que no es activo, pausado ni programado (vencido, agotado…) cuenta como terminado. */
        fun from(value: String?): CouponStatus = entries.firstOrNull { it.apiValue == value } ?: ENDED
    }
}

data class Coupon(
    val id: String,
    val title: String,
    val type: CouponType,
    /** Porcentaje (1–100) o centavos (monto); nulo en 2x1 y regalo. */
    val value: Long?,
    val conditions: String?,
    val validUntil: String?,
    val status: CouponStatus,
    /** 0 = sin límite. */
    val usageLimit: Int,
    val uses: Int,
    val version: Long
)

/** Cupón nuevo de 54_NegCrear (`POST /negocios/{id}/cupones`). */
data class NewCoupon(
    val title: String,
    val type: CouponType,
    val value: Long?,
    val conditions: String?,
    val start: OffsetDateTime,
    val end: OffsetDateTime,
    /** 0 domingo a 6 sábado; vacío = todos los días. */
    val days: List<Int>,
    val hoursFrom: LocalTime?,
    val hoursTo: LocalTime?,
    val usageLimit: Int
)

/** Lo que 55_NegHoras edita de `reservas_config`; lo demás se conserva tal cual en el `PUT`. */
data class CapacityConfig(val capacity: Int, val turnMinutes: Int, val version: Long)

/** Un turno del día (`GET /lugares/{id}/disponibilidad`). */
data class HourSlot(val hour: String, val available: Boolean, val free: Int)

/** Cupones y cupo del modo negocio (specs/nv-37). */
interface BusinessCouponsRepository {
    suspend fun coupons(businessId: String): Result<List<Coupon>>

    suspend fun create(businessId: String, coupon: NewCoupon): Result<Coupon>

    /** Activar o pausar con `If-Match`; versión vieja o con canjes = `Conflict`. */
    suspend fun setActive(businessId: String, coupon: Coupon, active: Boolean): Result<Coupon>

    /** «Sugerir con IA»; sin IA = 503. */
    suspend fun suggestTitle(businessId: String, type: CouponType, value: Long?, description: String?): Result<String>

    /** `reservas_config` del negocio (de `GET /negocios/mios`). Sin reservas configuradas = `NotFound`. */
    suspend fun capacity(businessId: String): Result<CapacityConfig>

    /** `PUT reservas-config` con `If-Match`, conservando los demás campos de la configuración. */
    suspend fun saveCapacity(businessId: String, config: CapacityConfig): Result<CapacityConfig>

    suspend fun availability(businessId: String, date: LocalDate): Result<List<HourSlot>>
}

/** Reglas de 54_NegCrear: título 1–40, porcentaje 1–100, monto > 0 y fin después del inicio. */
fun isValidCoupon(title: String, type: CouponType, value: Long?, start: LocalDate, end: LocalDate): Boolean {
    val titleOk = title.trim().length in 1..COUPON_TITLE_MAX
    val valueOk = when (type) {
        CouponType.PERCENT -> value != null && value in 1..COUPON_PERCENT_MAX.toLong()
        CouponType.AMOUNT -> value != null && value > 0
        CouponType.TWO_FOR_ONE, CouponType.GIFT -> true
    }
    return titleOk && valueOk && end.isAfter(start)
}

/** Cupo 1–500 y turno de 15 a 240 minutos en múltiplos de 15. */
fun isValidCapacity(capacity: Int, turnMinutes: Int): Boolean =
    capacity in 1..CAPACITY_MAX && turnMinutes in TURN_STEP..TURN_MAX && turnMinutes % TURN_STEP == 0
