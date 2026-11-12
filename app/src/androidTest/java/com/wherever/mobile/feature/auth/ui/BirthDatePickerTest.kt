package com.wherever.mobile.feature.auth.ui

import android.icu.text.DateFormat
import android.icu.text.DisplayContext
import android.icu.util.TimeZone
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * nv-42 (UI): en el calendario de Registro los días futuros salen deshabilitados y no se pueden escoger.
 * El día se busca por la misma descripción que arma Material 3 (esqueleto `yMMMMEEEEd` en UTC y el
 * idioma del dispositivo), no por un texto fijo.
 */
@RunWith(AndroidJUnit4::class)
class BirthDatePickerTest {
    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.of(2026, 11, 12)
    private var picked: LocalDate? = null

    private fun showPicker() {
        compose.setContent {
            WhereverTheme(darkTheme = false) {
                BirthDatePicker(
                    today = today,
                    onDismiss = {},
                    onPicked = { picked = it },
                    // Abre en el mes de hoy para que se vean días futuros.
                    initialDisplayedMonthMillis = today.toPickerMillis()
                )
            }
        }
    }

    private fun day(date: LocalDate) = compose.onNode(hasText(describe(date), substring = true))

    private fun describe(date: LocalDate): String {
        val format = DateFormat.getInstanceForSkeleton(
            DatePickerDefaults.YearMonthWeekdayDaySkeleton,
            Locale.getDefault()
        )
        format.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE)
        format.timeZone = TimeZone.GMT_ZONE
        return format.format(Date(date.toPickerMillis()))
    }

    @Test
    fun un_dia_futuro_sale_deshabilitado_y_no_se_puede_escoger() {
        showPicker()

        day(today.plusDays(8)).assertIsNotEnabled().performClick()
        day(today.plusDays(1)).assertIsNotEnabled()

        compose.onNodeWithTag("birth_date_accept").assertIsNotEnabled()
        assertNull(picked)
    }

    @Test
    fun hoy_y_los_dias_pasados_si_se_pueden_escoger() {
        showPicker()

        day(today).assertIsEnabled()
        day(today.minusDays(2)).assertIsEnabled().performClick()
        compose.onNodeWithTag("birth_date_accept").assertIsEnabled().performClick()

        assertEquals(today.minusDays(2), picked)
    }
}
