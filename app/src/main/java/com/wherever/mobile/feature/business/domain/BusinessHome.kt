package com.wherever.mobile.feature.business.domain

import com.wherever.mobile.core.common.Result
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

/** La API cuenta los días en la hora de la Ciudad de México (specs/nv-35). */
val BUSINESS_ZONE: ZoneId = ZoneId.of("America/Mexico_City")

/** Máximo del texto de «Contactar» (`POST reservas/{rid}/mensajes`). */
const val CONTACT_MAX = 500

/** Máximo de días de `GET /negocios/{id}/estadisticas`. */
const val STATS_MAX_DAYS = 90

/** Mes de 56_NegStats: los 30 días hasta ayer, como el rango por omisión de la API. */
const val DAYS_IN_MONTH = 30

/** Centavos por peso: la API manda el dinero en centavos. */
const val CENTS = 100

/** `GET /negocios/{id}/panel`. Cada dato con `?` llega nulo si su fuente no responde y se oculta. */
data class BusinessPanel(
    val businessId: String,
    val name: String,
    val state: String,
    val reservationsToday: Int? = null,
    val nextReservation: NextReservation? = null,
    val couponsRedeemedToday: Int? = null,
    val activeCoupons: Int? = null,
    val arrivalsToday: Int? = null,
    val weekRating: Double? = null,
    val ratingChange: Double? = null,
    val reviewsToAnswer: Int? = null,
    val seal: Boolean = false,
    val sealProgress: SealProgress? = null
)

data class NextReservation(val time: String, val people: Int)

data class SealProgress(val eligible: Boolean, val criteria: List<SealCriterion>)

data class SealCriterion(val name: String, val current: Double?, val goal: Double, val met: Boolean)

/** Consejo del día (`GET /negocios/{id}/consejo`). */
data class DailyTip(val text: String, val action: String)

/** Rangos de 56_NegStats. La API da 90 días como máximo: «Año» de la maqueta se vuelve Trimestre. */
enum class StatsRange(val days: Int, val groupBy: String) {
    WEEK(DAYS_IN_WEEK, "dia"),
    MONTH(DAYS_IN_MONTH, "dia"),
    QUARTER(STATS_MAX_DAYS, "semana")
}

data class StatsPoint(val start: LocalDate, val reservations: Int, val arrivals: Int, val coupons: Int)

data class StatsTotals(
    val views: Int = 0,
    val reservations: Int = 0,
    val arrivals: Int = 0,
    val coupons: Int = 0,
    val favorites: Int = 0
)

/** De dónde llegan las visitas (solo con plan premium). */
data class StatsOrigin(val search: Int, val map: Int, val home: Int, val aiPlan: Int, val coupon: Int) {
    val total: Int get() = search + map + home + aiPlan + coupon
}

data class BusinessStats(
    val from: LocalDate,
    val to: LocalDate,
    val series: List<StatsPoint>,
    val totals: StatsTotals,
    /** Cambio contra el periodo anterior, en porcentaje; nulo si el anterior fue 0. */
    val reservationsChange: Double?,
    val phrase: String,
    val origin: StatsOrigin?,
    val premiumRequired: Boolean
) {
    /** Asistencia: llegadas entre reservas. */
    val attendance: Int? get() = if (totals.reservations > 0) totals.arrivals * PERCENT / totals.reservations else null
}

private const val PERCENT = 100

enum class ReservationStatus(val apiKey: String) {
    PENDING("pendiente"),
    CONFIRMED("confirmada"),
    ARRIVED("asistio"),
    NO_SHOW("no_asistio"),
    CANCELLED("cancelada"),
    OTHER("");

    companion object {
        fun fromApi(key: String): ReservationStatus = entries.firstOrNull { it.apiKey == key } ?: OTHER
    }
}

/** Reserva del negocio. Sin correo ni teléfono del cliente (la API no los manda). */
data class Reservation(
    val id: String,
    val code: String,
    val dateTime: LocalDateTime,
    val client: String,
    val people: Int,
    val note: String?,
    /** Centavos. */
    val deposit: Long,
    val status: ReservationStatus,
    val version: Long
)

data class ReservationDay(val date: LocalDate, val items: List<Reservation>)

/** Cobro de una reserva (`GET reservas/{rid}/cobro`), en centavos. */
data class ReservationCharge(
    val deposit: Long,
    val refunds: Long,
    val commission: Long,
    val net: Long,
    val status: String
)

enum class BlockReason(val apiKey: String) { PRIVATE_EVENT("evento_privado"), FULL("lleno"), OTHER("otro") }

/** Horario cerrado (`POST /negocios/{id}/bloqueos`). */
data class ScheduleBlock(val id: String, val from: LocalDateTime, val to: LocalDateTime, val reason: BlockReason?)

/** Panel, estadísticas, reservas y check-in del modo negocio (specs/nv-35). Todo contra la API, sin caché. */
interface BusinessHomeRepository {
    /** `POST /usuarios/yo/modo` con `negocio`; sin rol comerciante = `Forbidden`. */
    suspend fun enterBusinessMode(): Result<Unit>

    suspend fun panel(businessId: String): Result<BusinessPanel>

    suspend fun tip(businessId: String): Result<DailyTip>

    suspend fun stats(businessId: String, from: LocalDate, to: LocalDate, groupBy: String): Result<BusinessStats>

    /** Guarda el PDF del mes en Descargas y devuelve su nombre. */
    suspend fun downloadMonthlyReport(businessId: String, month: YearMonth): Result<String>

    suspend fun reservations(
        businessId: String,
        date: LocalDate?,
        status: ReservationStatus? = null,
        query: String? = null
    ): Result<ReservationDay>

    /** Confirmar (desde pendiente), Llegó o No llegó (desde confirmada). 409 si cambió en otro lado. */
    suspend fun updateReservation(
        businessId: String,
        reservationId: String,
        status: ReservationStatus
    ): Result<Reservation>

    suspend fun contact(businessId: String, reservationId: String, text: String): Result<Unit>

    /** Marca la llegada con el código de la reserva; ya usado = `Conflict`. */
    suspend fun checkIn(businessId: String, code: String): Result<Reservation>

    /** Sin pago = `NotFound`. */
    suspend fun charge(businessId: String, reservationId: String): Result<ReservationCharge>

    suspend fun block(
        businessId: String,
        from: LocalDateTime,
        to: LocalDateTime,
        reason: BlockReason?
    ): Result<ScheduleBlock>

    suspend fun unblock(businessId: String, blockId: String): Result<Unit>
}

/** Texto de Contactar válido: 1 a 500 caracteres sin los signos menor y mayor que (la API los rechaza). */
fun isValidContactText(text: String): Boolean = text.trim().length in 1..CONTACT_MAX && '<' !in text && '>' !in text
