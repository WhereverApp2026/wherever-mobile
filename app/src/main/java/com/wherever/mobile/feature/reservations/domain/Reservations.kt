package com.wherever.mobile.feature.reservations.domain

import com.wherever.mobile.core.common.Result
import java.time.LocalDate

/** Un turno de `GET /lugares/{id}/disponibilidad`; `time` es RFC 3339 en UTC. */
data class TimeSlot(val time: String, val available: Boolean, val freeSeats: Int)

enum class ReservationStatus(val apiName: String) {
    /** Apartada mientras se paga el anticipo (backend nv-59/nv-60). */
    HELD("bloqueada"),
    PENDING("pendiente"),
    CONFIRMED("confirmada"),
    ATTENDED("asistio"),
    NO_SHOW("no_asistio"),
    EXPIRED("expirada"),
    CANCELLED("cancelada"),
    OTHER("");

    companion object {
        fun from(apiName: String): ReservationStatus = entries.firstOrNull { it.apiName == apiName } ?: OTHER
    }
}

data class CancellationPolicy(val type: String, val hoursLimit: Int, val percentBefore: Int, val percentAfter: Int)

data class ReservationMessage(val id: String, val text: String, val createdAt: String)

data class Reservation(
    val id: String,
    val code: String,
    val placeId: String,
    val dateTime: String,
    val people: Int,
    val note: String?,
    val status: ReservationStatus,
    val holdExpiresAt: String?,
    val depositCents: Long,
    val policy: CancellationPolicy,
    val refundCents: Long,
    val canChange: Boolean,
    val canCancel: Boolean,
    val version: Int,
    val messages: List<ReservationMessage> = emptyList()
)

data class ReservationPage(val items: List<Reservation>, val nextCursor: String?)

data class RefundQuote(val cents: Long, val percent: Int)

/** Datos de una reserva nueva. `idempotencyKey` se repite en cada reintento del mismo intento. */
data class NewReservation(
    val placeId: String,
    val dateTime: String,
    val people: Int,
    val note: String?,
    val idempotencyKey: String
)

/** Reservas del consumidor (wherever-backend nv-59, J5). */
interface ReservationsRepository {
    suspend fun availability(placeId: String, date: LocalDate, people: Int): Result<List<TimeSlot>>

    suspend fun create(reservation: NewReservation): Result<Reservation>

    suspend fun list(upcoming: Boolean, cursor: String? = null): Result<ReservationPage>

    suspend fun get(id: String): Result<Reservation>

    suspend fun refundQuote(id: String): Result<RefundQuote>

    suspend fun cancel(id: String, reason: String?): Result<Reservation>

    /** `PATCH` con `If-Match` de [Reservation.version]; versión vieja = 409. */
    suspend fun changeTime(reservation: Reservation, dateTime: String): Result<Reservation>
}
