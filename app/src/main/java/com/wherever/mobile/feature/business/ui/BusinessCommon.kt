package com.wherever.mobile.feature.business.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.DangerTint
import com.wherever.mobile.core.designsystem.GoldTint
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.RouteBlueTint
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.feature.business.domain.CENTS
import com.wherever.mobile.feature.business.domain.Reservation
import com.wherever.mobile.feature.business.domain.ReservationStatus
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Español de México para fechas y dinero (la API cuenta en la Ciudad de México). */
internal val MexicanSpanish: Locale = Locale.forLanguageTag("es-MX")

private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/** «Viernes 17 de octubre». */
private val LongDate = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", MexicanSpanish)

/** «Vie 17 de octubre». */
private val ShortDate = DateTimeFormatter.ofPattern("EEE d 'de' MMMM", MexicanSpanish)

internal fun LocalDateTime.hourText(): String = format(TimeFormat)

internal fun LocalDate.longText(): String = format(LongDate).capitalized()

internal fun LocalDate.shortText(): String = format(ShortDate).replace(".", "").capitalized()

/** Inicial del día como en la gráfica de la maqueta (L, M, M, J, V, S, D). */
internal fun LocalDate.dayInitial(): String = dayOfWeek.getDisplayName(TextStyle.NARROW, MexicanSpanish).uppercase()

internal fun LocalDate.dayName(): String = dayOfWeek.getDisplayName(TextStyle.FULL, MexicanSpanish)

private fun String.capitalized(): String = replaceFirstChar { it.titlecase(MexicanSpanish) }

/** Centavos a pesos: «$1,480» o «$74.50». */
internal fun Long.asMoney(): String {
    val format = NumberFormat.getCurrencyInstance(MexicanSpanish)
    val whole = this % CENTS == 0L
    format.minimumFractionDigits = if (whole) 0 else 2
    format.maximumFractionDigits = if (whole) 0 else 2
    return format.format(this.toDouble() / CENTS)
}

/** Dos iniciales del nombre del cliente (MG, FO), como en 50_NegCheckin. */
internal fun String.initials(): String = trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    .take(2).joinToString("") { it.take(1).uppercase() }

/** Colores y texto de la píldora de estado de 49_NegReservas. */
@Composable
internal fun ReservationStatusPill(status: ReservationStatus, modifier: Modifier = Modifier) {
    val (label, container) = when (status) {
        ReservationStatus.PENDING -> R.string.business_status_pending to GoldTint
        ReservationStatus.CONFIRMED -> R.string.business_status_confirmed to MintTint
        ReservationStatus.ARRIVED -> R.string.business_status_arrived to RouteBlueTint
        ReservationStatus.NO_SHOW -> R.string.business_status_no_show to DangerTint
        ReservationStatus.CANCELLED -> R.string.business_status_cancelled to DangerTint
        ReservationStatus.OTHER -> R.string.business_status_other to RouteBlueTint
    }
    StatusPill(stringResource(label), modifier, container = container, contentColor = Ink)
}

/** Detalle bajo el nombre: «2 personas, interior» (con nota) o «2 personas, $74» (con anticipo). */
@Composable
internal fun Reservation.detailText(): String {
    val people = pluralStringResource(R.plurals.business_people, people, people)
    val extra = note ?: deposit.takeIf { it > 0 }?.asMoney()
    return if (extra == null) people else stringResource(R.string.business_people_with, people, extra)
}

/** Fila de 49_NegReservas: hora | cliente y detalle | estado. */
@Composable
internal fun ReservationCard(reservation: Reservation, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.clickable(onClick = onClick).testTag("reservation_${reservation.id}")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Sizes.Thumbnail)
                .padding(horizontal = Spacing.L, vertical = Spacing.S)
        ) {
            Text(
                text = reservation.dateTime.hourText(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            VerticalDivider(
                modifier = Modifier.padding(horizontal = Spacing.M).height(Sizes.IconXXL),
                color = MaterialTheme.colorScheme.outline
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reservation.client,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = reservation.detailText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            ReservationStatusPill(reservation.status, Modifier.padding(start = Spacing.S))
        }
    }
}

/** Hora en recuadro azul claro, como en «Próximas hoy» de 48_NegPanel. */
@Composable
internal fun TimeBadge(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.M))
            .background(RouteBlueTint)
            .heightIn(min = Sizes.AvatarList)
            .padding(horizontal = Spacing.S)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RouteBlue
        )
    }
}

/**
 * Barras de la maqueta (48 y 56): la más alta en naranja. Sin datos (todo en 0), barras mínimas iguales.
 * [labels] vacío oculta la fila de días.
 */
@Composable
internal fun BarChart(
    values: List<Int>,
    labels: List<String>,
    height: Dp,
    barColor: Color,
    modifier: Modifier = Modifier,
    highlightColor: Color = Sunset
) {
    val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1
    val best = values.indexOf(values.maxOrNull() ?: 0).takeIf { (values.maxOrNull() ?: 0) > 0 }
    Column(modifier = modifier.fillMaxWidth().testTag("business_chart")) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth().height(height)
        ) {
            values.forEachIndexed { index, value ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(fraction = (value.toFloat() / max).coerceIn(MIN_BAR, 1f))
                        .clip(RoundedCornerShape(Radius.S))
                        .background(if (index == best) highlightColor else barColor)
                )
            }
        }
        if (labels.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.S),
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.S)
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Alto mínimo de una barra para que un día en 0 se vea. */
private const val MIN_BAR = 0.08f

/** Círculo azul con las iniciales del cliente. */
@Composable
internal fun InitialsAvatar(name: String, size: Dp) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size).clip(CircleShape).background(RouteBlue)
    ) {
        Text(
            text = name.initials(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/** Fila etiqueta | valor del detalle de 50_NegCheckin. */
@Composable
internal fun DetailRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.TouchTarget).padding(horizontal = Spacing.L)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = Spacing.S)
        )
    }
}
