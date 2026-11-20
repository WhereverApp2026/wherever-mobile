package com.wherever.mobile.feature.feed.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.ui.UiText
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FeedTimeTest {
    private val now = Instant.parse("2026-11-20T12:00:00Z")

    @Test
    fun `cada rango usa su texto`() {
        assertEquals(UiText.Res(R.string.feed_time_now), relativeTime("2026-11-20T11:59:40Z", now))
        assertEquals(UiText.Res(R.string.feed_time_minutes, listOf(5L)), relativeTime("2026-11-20T11:55:00Z", now))
        assertEquals(UiText.Res(R.string.feed_time_hours, listOf(3L)), relativeTime("2026-11-20T09:00:00Z", now))
        assertEquals(UiText.Res(R.string.feed_time_yesterday), relativeTime("2026-11-19T10:00:00Z", now))
        assertEquals(UiText.Res(R.string.feed_time_days, listOf(4L)), relativeTime("2026-11-16T12:00:00Z", now))
        assertEquals(UiText.Res(R.string.feed_time_long_ago), relativeTime("2026-11-01T12:00:00Z", now))
    }

    @Test
    fun `fecha futura o ilegible no rompe`() {
        assertEquals(UiText.Res(R.string.feed_time_now), relativeTime("2026-11-20T13:00:00Z", now))
        assertNull(relativeTime("ayer", now))
    }
}
