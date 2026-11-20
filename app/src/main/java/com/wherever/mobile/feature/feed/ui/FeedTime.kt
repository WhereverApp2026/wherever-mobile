package com.wherever.mobile.feature.feed.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.ui.UiText
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

private const val MINUTES_PER_HOUR = 60L
private const val HOURS_PER_DAY = 24L
private const val DAYS_PER_WEEK = 7L

/** "Ahora", "Hace 5 min", "Hace 3 h", "Ayer", "Hace 4 días" o "Hace más de una semana" (11_InicioAmigos). */
internal fun relativeTime(iso: String, now: Instant): UiText? {
    val then = try {
        Instant.parse(iso)
    } catch (_: DateTimeParseException) {
        return null
    }
    val minutes = Duration.between(then, now).toMinutes().coerceAtLeast(0)
    val hours = minutes / MINUTES_PER_HOUR
    val days = hours / HOURS_PER_DAY
    return when {
        minutes < 1 -> UiText.Res(R.string.feed_time_now)
        hours < 1 -> UiText.Res(R.string.feed_time_minutes, listOf(minutes))
        days < 1 -> UiText.Res(R.string.feed_time_hours, listOf(hours))
        days == 1L -> UiText.Res(R.string.feed_time_yesterday)
        days < DAYS_PER_WEEK -> UiText.Res(R.string.feed_time_days, listOf(days))
        else -> UiText.Res(R.string.feed_time_long_ago)
    }
}
