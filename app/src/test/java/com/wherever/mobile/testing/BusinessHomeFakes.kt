package com.wherever.mobile.testing

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.business.domain.BlockReason
import com.wherever.mobile.feature.business.domain.BusinessHomeRepository
import com.wherever.mobile.feature.business.domain.BusinessPanel
import com.wherever.mobile.feature.business.domain.BusinessStats
import com.wherever.mobile.feature.business.domain.DailyTip
import com.wherever.mobile.feature.business.domain.Reservation
import com.wherever.mobile.feature.business.domain.ReservationCharge
import com.wherever.mobile.feature.business.domain.ReservationDay
import com.wherever.mobile.feature.business.domain.ReservationStatus
import com.wherever.mobile.feature.business.domain.ScheduleBlock
import com.wherever.mobile.feature.business.domain.StatsTotals
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset

/** 17 de octubre de 2026, 12:00 en la Ciudad de México (18:00 UTC). */
val businessClock: Clock = Clock.fixed(Instant.parse("2026-10-17T18:00:00Z"), ZoneOffset.UTC)

val businessToday: LocalDate = LocalDate.of(2026, 10, 17)

fun sampleBusinessReservation(
    id: String = "r1",
    status: ReservationStatus = ReservationStatus.CONFIRMED,
    at: LocalDateTime = businessToday.atTime(17, 0),
    deposit: Long = 0,
    code: String = "WH-4821"
) = Reservation(id, code, at, "Mariana G.", 2, null, deposit, status, 1)

fun samplePanel() = BusinessPanel(businessId = "n1", name = "Café Raíz", state = "publicado")

fun sampleBusinessStats() = BusinessStats(
    from = businessToday.minusDays(7),
    to = businessToday.minusDays(1),
    series = emptyList(),
    totals = StatsTotals(reservations = 10, arrivals = 9),
    reservationsChange = null,
    phrase = "",
    origin = null,
    premiumRequired = true
)

/** Modo negocio en memoria; cada llamada queda en [calls]. */
class FakeBusinessHomeRepository : BusinessHomeRepository {
    val calls = mutableListOf<String>()
    var modeResult: Result<Unit> = Result.Success(Unit)
    var panelResult: Result<BusinessPanel> = Result.Success(samplePanel())
    var tipResult: Result<DailyTip> = Result.Failure(AppError.Server())
    var statsResult: Result<BusinessStats> = Result.Success(sampleBusinessStats())
    var reportResult: Result<String> = Result.Success("wherever-2026-10.pdf")
    var reservationsResult: Result<List<Reservation>> = Result.Success(emptyList())
    var updateResult: Result<Reservation>? = null
    var contactResult: Result<Unit> = Result.Success(Unit)
    var checkInResult: Result<Reservation>? = null
    var chargeResult: Result<ReservationCharge> = Result.Failure(AppError.NotFound())
    var blockResult: Result<ScheduleBlock>? = null
    val reservationQueries = mutableListOf<Triple<LocalDate?, ReservationStatus?, String?>>()
    val statsRanges = mutableListOf<Pair<LocalDate, LocalDate>>()

    override suspend fun enterBusinessMode() = modeResult.also { calls += "modo" }

    override suspend fun panel(businessId: String) = panelResult.also { calls += "panel" }

    override suspend fun tip(businessId: String) = tipResult

    override suspend fun stats(businessId: String, from: LocalDate, to: LocalDate, groupBy: String) =
        statsResult.also { statsRanges += from to to }

    override suspend fun downloadMonthlyReport(businessId: String, month: YearMonth) =
        reportResult.also { calls += "reporte $month" }

    override suspend fun reservations(
        businessId: String,
        date: LocalDate?,
        status: ReservationStatus?,
        query: String?
    ): Result<ReservationDay> {
        reservationQueries += Triple(date, status, query)
        calls += "reservas"
        return when (val result = reservationsResult) {
            is Result.Success -> Result.Success(ReservationDay(date ?: businessToday, result.data))
            is Result.Failure -> result
        }
    }

    override suspend fun updateReservation(businessId: String, reservationId: String, status: ReservationStatus) =
        (updateResult ?: Result.Success(sampleBusinessReservation(reservationId, status))).also {
            calls += "patch ${status.apiKey}"
        }

    override suspend fun contact(businessId: String, reservationId: String, text: String) =
        contactResult.also { calls += "mensaje" }

    override suspend fun checkIn(businessId: String, code: String) =
        (checkInResult ?: Result.Success(sampleBusinessReservation(status = ReservationStatus.ARRIVED, code = code)))
            .also { calls += "escanear $code" }

    override suspend fun charge(businessId: String, reservationId: String) = chargeResult.also { calls += "cobro" }

    override suspend fun block(businessId: String, from: LocalDateTime, to: LocalDateTime, reason: BlockReason?) =
        (blockResult ?: Result.Success(ScheduleBlock("b1", from, to, reason))).also { calls += "bloqueo" }

    override suspend fun unblock(businessId: String, blockId: String): Result<Unit> =
        Result.Success(Unit).also { calls += "reabrir $blockId" }
}
