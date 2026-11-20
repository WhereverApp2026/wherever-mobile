package com.wherever.mobile.feature.reservations.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverDangerButton
import com.wherever.mobile.core.designsystem.WhereverDialog
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverTextButton
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.core.ui.formatDateTime
import com.wherever.mobile.core.ui.formatTime
import com.wherever.mobile.feature.reservations.domain.RefundQuote
import com.wherever.mobile.feature.reservations.domain.Reservation
import com.wherever.mobile.feature.reservations.domain.ReservationStatus
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

private const val CODE_SIZE_SP = 40
private const val DEFAULT_DURATION_MIN = 90L

/** 56_ReservaConfirmada y detalle de la reserva (US-076, US-077, US-088). */
@Composable
fun ReservationRoute(onBack: () -> Unit, viewModel: ReservationViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val reservation = state.reservation
    val loadError = state.loadError
    when {
        state.loading -> LoadingState()
        reservation == null && loadError != null -> FormScreen {
            BackCircleButton(onClick = onBack)
            ErrorState(loadError.asString(), viewModel::load)
        }
        reservation != null -> ReservationContent(reservation, state, viewModel, onBack)
    }
    state.cancelQuote?.let { CancelDialog(it, viewModel) }
    state.changeSlots?.let { slots ->
        val zone = remember { ZoneId.systemDefault() }
        WhereverDialog(
            onDismissRequest = viewModel::dismissChange,
            title = { Text(stringResource(R.string.reservation_change)) },
            text = {
                if (slots.none { it.available }) {
                    Text(stringResource(R.string.reserve_no_slots))
                } else {
                    SlotGrid(slots, null, zone, viewModel::changeTo)
                }
            },
            confirmButton = {
                WhereverTextButton(onClick = viewModel::dismissChange) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

@Composable
private fun ReservationContent(
    reservation: Reservation,
    state: ReservationUiState,
    viewModel: ReservationViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val zone = remember { ZoneId.systemDefault() }
    val placeName = state.placeName ?: stringResource(R.string.reservation_place_fallback)
    val shareText = stringResource(
        R.string.reservation_share_text,
        placeName,
        formatDateTime(reservation.dateTime, zone),
        reservation.code
    )
    FormScreen(footer = { Actions(reservation, state, viewModel, context, placeName, shareText) }) {
        BackCircleButton(onClick = onBack)
        state.message?.let { InfoBanner(it.asString(), tag = "reservation_message") }
        state.error?.let { ErrorBanner(it.asString()) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.S),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatusBadge(reservation.status)
            Text(placeName, style = MaterialTheme.typography.headlineMedium)
            if (reservation.code.isNotBlank() && reservation.status.isActive()) {
                Text(stringResource(R.string.reservation_code), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    reservation.code,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = CODE_SIZE_SP.sp,
                    modifier = Modifier.testTag("reservation_code")
                )
                Text(stringResource(R.string.reservation_code_hint), style = MaterialTheme.typography.bodySmall)
            }
        }
        Details(reservation, zone)
        HoldNotice(reservation, zone)
        if (reservation.messages.isNotEmpty()) {
            Text(stringResource(R.string.reservation_messages), style = MaterialTheme.typography.titleMedium)
            reservation.messages.forEach { message ->
                SurfaceCard {
                    Column(modifier = Modifier.padding(Spacing.M)) {
                        Text(message.text)
                        Text(
                            formatDateTime(message.createdAt, zone),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun ReservationStatus.isActive(): Boolean =
    this == ReservationStatus.CONFIRMED || this == ReservationStatus.PENDING || this == ReservationStatus.HELD

@Composable
private fun Details(reservation: Reservation, zone: ZoneId) {
    SurfaceCard(modifier = Modifier.testTag("reservation_details")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Text(
                stringResource(
                    R.string.reservation_when,
                    formatDate(reservation.dateTime, zone),
                    formatTime(reservation.dateTime, zone)
                )
            )
            Text(pluralStringResource(R.plurals.reserve_people_count, reservation.people, reservation.people))
            reservation.note?.let { Text(stringResource(R.string.reservation_note, it)) }
            if (reservation.depositCents > 0) {
                Text(stringResource(R.string.reservation_deposit, formatCents(reservation.depositCents)))
            }
            Text(
                stringResource(
                    R.string.reservation_policy,
                    reservation.policy.percentBefore,
                    reservation.policy.hoursLimit,
                    reservation.policy.percentAfter
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Apartada con anticipo: la app aún no cobra (sin SDK de pagos en el stack), así que se avisa cuándo vence. */
@Composable
private fun HoldNotice(reservation: Reservation, zone: ZoneId) {
    if (reservation.status != ReservationStatus.HELD) return
    InfoBanner(
        stringResource(
            R.string.reservation_held_notice,
            formatCents(reservation.depositCents),
            reservation.holdExpiresAt?.let { formatTime(it, zone) }.orEmpty()
        ),
        icon = Icons.Outlined.Info,
        tag = "reservation_held"
    )
}

@Composable
private fun Actions(
    reservation: Reservation,
    state: ReservationUiState,
    viewModel: ReservationViewModel,
    context: Context,
    placeName: String,
    shareText: String
) {
    if (reservation.status.isActive()) {
        WhereverOutlinedButton(
            text = stringResource(R.string.reservation_calendar),
            onClick = { addToCalendar(context, reservation, placeName) },
            icon = Icons.Outlined.DateRange,
            tag = "reservation_calendar"
        )
        WhereverOutlinedButton(
            text = stringResource(R.string.reservation_share),
            onClick = {
                val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, shareText)
                context.startActivity(Intent.createChooser(send, null))
            },
            icon = Icons.Outlined.Share,
            tag = "reservation_share"
        )
    }
    if (reservation.canChange) {
        WhereverOutlinedButton(
            text = stringResource(R.string.reservation_change),
            onClick = viewModel::openChange,
            enabled = !state.working,
            tag = "reservation_change"
        )
    }
    if (reservation.canCancel) {
        WhereverDangerButton(
            text = stringResource(R.string.reservation_cancel),
            onClick = viewModel::askCancel,
            enabled = !state.working,
            tag = "reservation_cancel"
        )
    }
}

/** Calendario con `ACTION_INSERT`: no pide permiso; la persona confirma en su app de calendario. */
private fun addToCalendar(context: Context, reservation: Reservation, placeName: String) {
    val start = runCatching { Instant.parse(reservation.dateTime).toEpochMilli() }.getOrNull() ?: return
    val intent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + Duration.ofMinutes(DEFAULT_DURATION_MIN).toMillis())
        .putExtra(CalendarContract.Events.TITLE, placeName)
        .putExtra(CalendarContract.Events.DESCRIPTION, reservation.code)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // Sin app de calendario: no hay nada que abrir.
    }
}

@Composable
private fun CancelDialog(quote: RefundQuote, viewModel: ReservationViewModel) {
    var reason by remember { mutableStateOf("") }
    WhereverDialog(
        onDismissRequest = viewModel::dismissCancel,
        title = { Text(stringResource(R.string.reservation_cancel_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
                Text(
                    if (quote.cents > 0) {
                        stringResource(R.string.reservation_cancel_refund, formatCents(quote.cents), quote.percent)
                    } else {
                        stringResource(R.string.reservation_cancel_no_refund)
                    },
                    modifier = Modifier.testTag("reservation_refund")
                )
                LabeledTextField(
                    label = stringResource(R.string.reservation_cancel_reason),
                    value = reason,
                    onValueChange = { reason = it.take(NOTE_MAX) },
                    tag = "reservation_cancel_reason"
                )
            }
        },
        confirmButton = {
            WhereverTextButton(
                onClick = { viewModel.confirmCancel(reason) },
                modifier = Modifier.testTag("confirm_dialog_accept")
            ) { Text(stringResource(R.string.reservation_cancel)) }
        },
        dismissButton = {
            WhereverTextButton(onClick = viewModel::dismissCancel) { Text(stringResource(R.string.reservation_keep)) }
        }
    )
}
