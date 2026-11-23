package com.wherever.mobile.feature.business.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.business.domain.BusinessCouponsRepository
import com.wherever.mobile.feature.business.domain.CapacityConfig
import com.wherever.mobile.feature.business.domain.Coupon
import com.wherever.mobile.feature.business.domain.CouponStatus
import com.wherever.mobile.feature.business.domain.CouponType
import com.wherever.mobile.feature.business.domain.HourSlot
import com.wherever.mobile.feature.business.domain.NewCoupon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/** Cupones que se piden de una vez (la API pagina; un negocio no suele pasar de este número). */
private const val COUPONS_LIMIT = 50

/** La ocupación por hora se mide para una persona: `lugares_libres` del turno. */
private const val ONE_PERSON = 1

private val HOUR = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Cupones y cupo contra la API real (specs/nv-37). La configuración de reservas se guarda en memoria tal cual
 * llegó para devolver en el `PUT` los campos que 55_NegHoras no edita (anticipo, políticas…).
 */
class BusinessCouponsRepositoryImpl @Inject constructor(private val api: BusinessCouponsApi) :
    BusinessCouponsRepository {
    private val configs = ConcurrentHashMap<String, JsonObject>()

    override suspend fun coupons(businessId: String): Result<List<Coupon>> =
        safeApiCall { api.coupons(businessId, COUPONS_LIMIT).items.map { it.toDomain() } }

    override suspend fun create(businessId: String, coupon: NewCoupon): Result<Coupon> = safeApiCall {
        val hours = if (coupon.hoursFrom != null && coupon.hoursTo != null) {
            CouponHoursDto(coupon.hoursFrom.format(HOUR), coupon.hoursTo.format(HOUR))
        } else {
            null
        }
        api.create(
            businessId,
            NewCouponDto(
                title = coupon.title.trim(),
                type = coupon.type.apiValue,
                value = coupon.value.takeIf { coupon.type.hasValue },
                conditions = coupon.conditions?.trim()?.takeIf { it.isNotEmpty() },
                start = coupon.start.toString(),
                end = coupon.end.toString(),
                days = coupon.days.takeIf { it.isNotEmpty() }?.sorted(),
                hours = hours,
                usageLimit = coupon.usageLimit
            )
        ).toDomain()
    }

    override suspend fun setActive(businessId: String, coupon: Coupon, active: Boolean): Result<Coupon> = safeApiCall {
        val status = if (active) CouponStatus.ACTIVE else CouponStatus.PAUSED
        api.setStatus(businessId, coupon.id, "\"${coupon.version}\"", CouponStatusDto(status.apiValue)).toDomain()
    }

    override suspend fun suggestTitle(
        businessId: String,
        type: CouponType,
        value: Long?,
        description: String?
    ): Result<String> = safeApiCall {
        api.suggestTitle(
            businessId,
            TitleRequestDto(
                type.apiValue,
                value,
                description?.takeIf {
                    it.isNotBlank()
                }
            )
        ).title
    }

    override suspend fun capacity(businessId: String): Result<CapacityConfig> {
        val result = safeApiCall { api.mine().items.firstOrNull { it.id == businessId } }
        return when (result) {
            is Result.Failure -> result
            is Result.Success -> {
                val config = result.data?.reservationsConfig ?: return Result.Failure(AppError.NotFound())
                configs[businessId] = config
                Result.Success(config.toCapacity(result.data.version))
            }
        }
    }

    override suspend fun saveCapacity(businessId: String, config: CapacityConfig): Result<CapacityConfig> =
        safeApiCall {
            val base = configs[businessId] ?: JsonObject(emptyMap())
            val body = JsonObject(
                base + mapOf(
                    "capacidad_por_horario" to JsonPrimitive(config.capacity),
                    "duracion_turno_min" to JsonPrimitive(config.turnMinutes)
                )
            )
            val saved = api.saveConfig(businessId, "\"${config.version}\"", body)
            val newConfig = saved.reservationsConfig ?: body
            configs[businessId] = newConfig
            newConfig.toCapacity(saved.version)
        }

    override suspend fun availability(businessId: String, date: LocalDate): Result<List<HourSlot>> = safeApiCall {
        api.availability(businessId, date.toString(), ONE_PERSON).slots.map { HourSlot(it.hour, it.available, it.free) }
    }
}

private fun JsonObject.toCapacity(version: Long) = CapacityConfig(
    capacity = this["capacidad_por_horario"]?.jsonPrimitive?.intOrNull ?: 0,
    turnMinutes = this["duracion_turno_min"]?.jsonPrimitive?.intOrNull ?: 0,
    version = version
)

private fun CouponDto.toDomain() = Coupon(
    id = id,
    title = title,
    type = CouponType.from(type),
    value = value,
    conditions = conditions?.takeIf { it.isNotBlank() },
    validUntil = validUntil,
    status = CouponStatus.from(status),
    usageLimit = usageLimit,
    uses = uses,
    version = version
)
