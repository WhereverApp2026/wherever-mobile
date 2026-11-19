package com.wherever.mobile.feature.reservations.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.ui.formatTime
import com.wherever.mobile.feature.reservations.domain.ReservationStatus
import com.wherever.mobile.feature.reservations.domain.TimeSlot
import java.text.NumberFormat
import java.time.ZoneId
import java.util.Locale

private const val CENTS = 100.0

/** Centavos de la API a "$1,234.50" en pesos. */
fun formatCents(cents: Long): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")).format(cents / CENTS)

internal fun statusLabel(status: ReservationStatus): Int = when (status) {
    ReservationStatus.HELD -> R.string.reservation_status_held
    ReservationStatus.PENDING -> R.string.reservation_status_pending
    ReservationStatus.CONFIRMED -> R.string.reservation_status_confirmed
    ReservationStatus.ATTENDED -> R.string.reservation_status_attended
    ReservationStatus.NO_SHOW -> R.string.reservation_status_no_show
    ReservationStatus.EXPIRED -> R.string.reservation_status_expired
    ReservationStatus.CANCELLED -> R.string.reservation_status_cancelled
    ReservationStatus.OTHER -> R.string.reservation_status_other
}

@Composable
internal fun StatusBadge(status: ReservationStatus, modifier: Modifier = Modifier) {
    StatusPill(stringResource(statusLabel(status)), modifier = modifier.testTag("reservation_status"))
}

/** Turnos en la hora del teléfono; los que no tienen cupo se ven pero no se eligen. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SlotGrid(slots: List<TimeSlot>, selected: String?, zone: ZoneId, onSelect: (TimeSlot) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        slots.forEach { slot ->
            SelectChip(
                text = formatTime(slot.time, zone),
                selected = slot.time == selected,
                onClick = { onSelect(slot) },
                enabled = slot.available,
                tag = "slot_${slot.time}"
            )
        }
    }
}
