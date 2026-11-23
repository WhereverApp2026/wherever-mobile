package com.wherever.mobile.feature.business.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.business.domain.BUSINESS_ZONE
import com.wherever.mobile.feature.business.domain.BlockReason
import com.wherever.mobile.feature.business.domain.BusinessHomeRepository
import com.wherever.mobile.feature.business.domain.BusinessPanel
import com.wherever.mobile.feature.business.domain.BusinessStats
import com.wherever.mobile.feature.business.domain.DailyTip
import com.wherever.mobile.feature.business.domain.NextReservation
import com.wherever.mobile.feature.business.domain.Reservation
import com.wherever.mobile.feature.business.domain.ReservationCharge
import com.wherever.mobile.feature.business.domain.ReservationDay
import com.wherever.mobile.feature.business.domain.ReservationStatus
import com.wherever.mobile.feature.business.domain.ScheduleBlock
import com.wherever.mobile.feature.business.domain.SealCriterion
import com.wherever.mobile.feature.business.domain.SealProgress
import com.wherever.mobile.feature.business.domain.StatsOrigin
import com.wherever.mobile.feature.business.domain.StatsPoint
import com.wherever.mobile.feature.business.domain.StatsTotals
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.withContext

private const val MODE_BUSINESS = "negocio"

/** Modo negocio contra la API real (specs/nv-35). Nada se guarda en el teléfono salvo el PDF que pide el dueño. */
class BusinessHomeRepositoryImpl @Inject constructor(
    private val api: BusinessHomeApi,
    private val saver: ReportSaver,
    private val dispatchers: DispatcherProvider
) : BusinessHomeRepository {
    override suspend fun enterBusinessMode(): Result<Unit> = safeApiCall { api.mode(ModeDto(MODE_BUSINESS)) }

    override suspend fun panel(businessId: String): Result<BusinessPanel> =
        safeApiCall { api.panel(businessId).toDomain() }

    override suspend fun tip(businessId: String): Result<DailyTip> = safeApiCall {
        api.tip(businessId).let { DailyTip(it.text, it.action) }
    }

    override suspend fun stats(
        businessId: String,
        from: LocalDate,
        to: LocalDate,
        groupBy: String
    ): Result<BusinessStats> = safeApiCall {
        api.stats(businessId, from.toString(), to.toString(), groupBy).toDomain()
    }

    override suspend fun downloadMonthlyReport(businessId: String, month: YearMonth): Result<String> {
        val fileName = "wherever-$month.pdf"
        val saved = safeApiCall {
            val body = api.monthlyReport(businessId, month.toString())
            withContext(dispatchers.io) { body.use { saver.save(fileName, it.byteStream()) } }
        }
        return when (saved) {
            is Result.Success -> if (saved.data) Result.Success(fileName) else Result.Failure(AppError.Unknown())
            is Result.Failure -> saved
        }
    }

    override suspend fun reservations(
        businessId: String,
        date: LocalDate?,
        status: ReservationStatus?,
        query: String?
    ): Result<ReservationDay> = safeApiCall {
        val dto = api.reservations(
            businessId,
            date?.toString(),
            status?.apiKey,
            query?.trim()?.takeIf { it.isNotEmpty() }
        )
        ReservationDay(LocalDate.parse(dto.date), dto.items.map { it.toDomain() })
    }

    override suspend fun updateReservation(
        businessId: String,
        reservationId: String,
        status: ReservationStatus
    ): Result<Reservation> = safeApiCall {
        api.updateReservation(businessId, reservationId, ReservationStatusDto(status.apiKey)).toDomain()
    }

    override suspend fun contact(businessId: String, reservationId: String, text: String): Result<Unit> =
        safeApiCall { api.contact(businessId, reservationId, ContactDto(text.trim())) }

    override suspend fun checkIn(businessId: String, code: String): Result<Reservation> =
        safeApiCall { api.checkIn(businessId, CheckInDto(code.trim().uppercase())).toDomain() }

    override suspend fun charge(businessId: String, reservationId: String): Result<ReservationCharge> = safeApiCall {
        api.charge(businessId, reservationId).let {
            ReservationCharge(it.deposit, it.refunds, it.commission, it.net, it.status)
        }
    }

    override suspend fun block(
        businessId: String,
        from: LocalDateTime,
        to: LocalDateTime,
        reason: BlockReason?
    ): Result<ScheduleBlock> = safeApiCall {
        val dto = api.block(businessId, BlockRequestDto(from.toApi(), to.toApi(), reason?.apiKey))
        ScheduleBlock(
            id = dto.id,
            from = parseDateTime(dto.from),
            to = parseDateTime(dto.to),
            reason = BlockReason.entries.firstOrNull { it.apiKey == dto.reason }
        )
    }

    override suspend fun unblock(businessId: String, blockId: String): Result<Unit> =
        safeApiCall { api.unblock(businessId, blockId) }
}

/** Hora local de la Ciudad de México con su desfase, como la espera la API. */
private fun LocalDateTime.toApi(): String = atZone(BUSINESS_ZONE).toOffsetDateTime().toString()

/** La API manda ISO 8601 con desfase; se muestra en la hora de la Ciudad de México. */
internal fun parseDateTime(value: String): LocalDateTime = try {
    OffsetDateTime.parse(value).atZoneSameInstant(BUSINESS_ZONE).toLocalDateTime()
} catch (_: DateTimeParseException) {
    LocalDateTime.parse(value)
}

internal fun ReservationDto.toDomain() = Reservation(
    id = id,
    code = code,
    dateTime = parseDateTime(dateTime),
    client = client,
    people = people,
    note = note?.takeIf { it.isNotBlank() },
    deposit = deposit,
    status = ReservationStatus.fromApi(status),
    version = version
)

private fun PanelDto.toDomain() = BusinessPanel(
    businessId = businessId,
    name = name,
    state = state,
    reservationsToday = reservationsToday,
    nextReservation = nextReservation?.let { NextReservation(it.time, it.people) },
    couponsRedeemedToday = couponsRedeemedToday,
    activeCoupons = activeCoupons,
    arrivalsToday = arrivalsToday,
    weekRating = weekRating,
    ratingChange = ratingChange,
    reviewsToAnswer = reviewsToAnswer,
    seal = seal,
    sealProgress = sealProgress?.let { progress ->
        SealProgress(progress.eligible, progress.criteria.map { SealCriterion(it.name, it.current, it.goal, it.met) })
    }
)

private fun StatsDto.toDomain() = BusinessStats(
    from = LocalDate.parse(from),
    to = LocalDate.parse(to),
    series = series.map { StatsPoint(LocalDate.parse(it.start), it.reservations, it.arrivals, it.coupons) },
    totals = StatsTotals(totals.views, totals.reservations, totals.arrivals, totals.coupons, totals.favorites),
    reservationsChange = change.reservations,
    phrase = phrase,
    origin = advanced?.origin?.let { StatsOrigin(it.search, it.map, it.home, it.aiPlan, it.coupon) },
    premiumRequired = premiumRequired
)
