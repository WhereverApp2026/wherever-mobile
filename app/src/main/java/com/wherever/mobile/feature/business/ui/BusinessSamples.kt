// MagicNumber y LongParameterList: datos de ejemplo de las vistas previas y capturas (48, 49, 50 y 56), copiados
// de la maqueta. No se usan fuera de @Preview ni llegan a la app.
@file:Suppress("MagicNumber", "LongParameterList")

package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.BusinessPanel
import com.wherever.mobile.feature.business.domain.BusinessStats
import com.wherever.mobile.feature.business.domain.DailyTip
import com.wherever.mobile.feature.business.domain.Reservation
import com.wherever.mobile.feature.business.domain.ReservationCharge
import com.wherever.mobile.feature.business.domain.ReservationStatus
import com.wherever.mobile.feature.business.domain.StatsOrigin
import com.wherever.mobile.feature.business.domain.StatsPoint
import com.wherever.mobile.feature.business.domain.StatsRange
import com.wherever.mobile.feature.business.domain.StatsTotals
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

internal fun samplePanelState(): BusinessPanelUiState {
    val today = LocalDate.of(2026, 10, 17)
    return BusinessPanelUiState(
        loading = false,
        panel = BusinessPanel(
            businessId = "n1",
            name = "Café Raíz",
            state = "publicado",
            reservationsToday = 12,
            arrivalsToday = 5,
            couponsRedeemedToday = 3,
            weekRating = 4.6,
            ratingChange = 0.2,
            reviewsToAnswer = 2,
            activeCoupons = 1,
            seal = true
        ),
        tip = DailyTip("Los martes de 4 a 6 tienes poca gente. ¿Lanzas un cupón?", "crear_cupon"),
        week = BusinessStats(
            from = today.minusDays(7),
            to = today.minusDays(1),
            series = listOf(3, 5, 4, 7, 12, 11, 6).mapIndexed { i, v ->
                StatsPoint(today.minusDays(7L - i), v, v, 0)
            },
            totals = StatsTotals(reservations = 48),
            reservationsChange = 18.0,
            phrase = "",
            origin = null,
            premiumRequired = true
        ),
        upcoming = listOf(
            sampleReservation("r1", "Mariana G.", today.atTime(17, 0), 2, ReservationStatus.CONFIRMED),
            sampleReservation("r2", "Familia Ortega", today.atTime(18, 30), 5, ReservationStatus.PENDING)
        )
    )
}

internal fun sampleReservation(
    id: String,
    client: String,
    at: LocalDateTime,
    people: Int,
    status: ReservationStatus,
    deposit: Long = 0,
    note: String? = null
) = Reservation(id, "WH-48$id", at, client, people, note, deposit, status, 1)

internal val sampleHeader = BusinessHeader(listOf(BusinessChoice("n1", "Café Raíz", null)), "n1") {}

internal fun sampleStatsState(range: StatsRange = StatsRange.WEEK): BusinessStatsUiState {
    val to = LocalDate.of(2026, 10, 16)
    val values = listOf(9, 13, 11, 15, 19, 17, 8)
    return BusinessStatsUiState(
        range = range,
        loading = false,
        stats = BusinessStats(
            from = to.minusDays(6),
            to = to,
            series = values.mapIndexed { i, v -> StatsPoint(to.minusDays(6L - i), v, (v * 0.9).roundToInt(), 2) },
            totals = StatsTotals(views = 420, reservations = 84, arrivals = 76, coupons = 56, favorites = 12),
            reservationsChange = 18.0,
            phrase = "Tuviste 18 % más reservas que la semana pasada.",
            origin = StatsOrigin(search = 24, map = 6, home = 4, aiPlan = 62, coupon = 4),
            premiumRequired = false
        )
    )
}

internal fun sampleReservationsState(): BusinessReservationsUiState {
    val day = LocalDate.of(2026, 10, 17)
    return BusinessReservationsUiState(
        date = day,
        today = day,
        loading = false,
        items = listOf(
            sampleReservation(
                "1",
                "Luis Hernández",
                day.atTime(13, 0),
                2,
                ReservationStatus.ARRIVED,
                note = "interior"
            ),
            sampleReservation("2", "Ana y amigos", day.atTime(14, 30), 4, ReservationStatus.ARRIVED, note = "terraza"),
            sampleReservation("3", "Mariana G.", day.atTime(17, 0), 2, ReservationStatus.CONFIRMED, deposit = 7400),
            sampleReservation("4", "Familia Ortega", day.atTime(18, 30), 5, ReservationStatus.PENDING, deposit = 12000),
            sampleReservation("5", "Jorge M.", day.atTime(19, 30), 3, ReservationStatus.CONFIRMED, deposit = 9000)
        )
    )
}

internal fun sampleCheckinState(): CheckinUiState {
    val day = LocalDate.of(2026, 10, 17)
    return CheckinUiState(
        code = "WH-4821",
        reservation = sampleReservation("21", "Mariana G.", day.atTime(17, 0), 2, ReservationStatus.CONFIRMED, 7400),
        charge = ReservationCharge(deposit = 7400, refunds = 0, commission = 740, net = 6660, status = "pagado")
    )
}
