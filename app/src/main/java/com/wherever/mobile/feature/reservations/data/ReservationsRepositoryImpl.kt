package com.wherever.mobile.feature.reservations.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.reservations.domain.CancellationPolicy
import com.wherever.mobile.feature.reservations.domain.NewReservation
import com.wherever.mobile.feature.reservations.domain.RefundQuote
import com.wherever.mobile.feature.reservations.domain.Reservation
import com.wherever.mobile.feature.reservations.domain.ReservationMessage
import com.wherever.mobile.feature.reservations.domain.ReservationPage
import com.wherever.mobile.feature.reservations.domain.ReservationStatus
import com.wherever.mobile.feature.reservations.domain.ReservationsRepository
import com.wherever.mobile.feature.reservations.domain.TimeSlot
import java.time.LocalDate
import javax.inject.Inject

internal const val RESERVATIONS_PAGE = 20

class ReservationsRepositoryImpl @Inject constructor(private val api: ReservationsApi) : ReservationsRepository {
    override suspend fun availability(placeId: String, date: LocalDate, people: Int): Result<List<TimeSlot>> =
        safeApiCall {
            api.availability(placeId, date.toString(), people).slots.map {
                TimeSlot(it.time, it.available, it.freeSeats)
            }
        }

    override suspend fun create(reservation: NewReservation): Result<Reservation> = safeApiCall {
        api.create(
            reservation.idempotencyKey,
            CreateReservationDto(
                placeId = reservation.placeId,
                dateTime = reservation.dateTime,
                people = reservation.people,
                note = reservation.note?.trim()?.takeIf { it.isNotEmpty() }
            )
        ).toDomain()
    }

    override suspend fun list(upcoming: Boolean, cursor: String?): Result<ReservationPage> = safeApiCall {
        api.list(if (upcoming) "proximas" else "pasadas", cursor, RESERVATIONS_PAGE)
    }.map { dto -> ReservationPage(dto.items.map { it.toDomain() }, dto.nextCursor?.takeIf { it.isNotBlank() }) }

    override suspend fun get(id: String): Result<Reservation> = safeApiCall { api.get(id).toDomain() }

    override suspend fun refundQuote(id: String): Result<RefundQuote> = safeApiCall {
        api.refund(id).let { RefundQuote(it.cents, it.percent) }
    }

    override suspend fun cancel(id: String, reason: String?): Result<Reservation> = safeApiCall {
        api.cancel(id, CancelReservationDto(reason?.trim()?.takeIf { it.isNotEmpty() })).toDomain()
    }

    override suspend fun changeTime(reservation: Reservation, dateTime: String): Result<Reservation> = safeApiCall {
        api.change(reservation.id, "\"${reservation.version}\"", ChangeReservationDto(dateTime)).toDomain()
    }
}

internal fun ReservationDto.toDomain() = Reservation(
    id = id,
    code = code,
    placeId = placeId,
    dateTime = dateTime,
    people = people,
    note = note?.takeIf { it.isNotBlank() },
    status = ReservationStatus.from(status),
    holdExpiresAt = holdExpiresAt,
    depositCents = deposit.cents,
    policy = CancellationPolicy(policy.type, policy.hoursLimit, policy.percentBefore, policy.percentAfter),
    refundCents = refundCents,
    canChange = canChange,
    canCancel = canCancel,
    version = version,
    messages = messages.map { ReservationMessage(it.id, it.text, it.createdAt) }
)
