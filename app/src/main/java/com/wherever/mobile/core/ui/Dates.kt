package com.wherever.mobile.core.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val DATE_TIME = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("es-MX"))
private val DATE_ONLY = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-MX"))

/** La API manda fechas ISO 8601 en UTC. Si no se puede leer, se muestra tal cual. */
fun formatDateTime(iso: String, zone: ZoneId): String = format(iso, zone, DATE_TIME)

fun formatDate(iso: String, zone: ZoneId): String = format(iso, zone, DATE_ONLY)

private fun format(iso: String, zone: ZoneId, formatter: DateTimeFormatter): String = try {
    formatter.format(Instant.parse(iso).atZone(zone))
} catch (_: DateTimeParseException) {
    iso
}
