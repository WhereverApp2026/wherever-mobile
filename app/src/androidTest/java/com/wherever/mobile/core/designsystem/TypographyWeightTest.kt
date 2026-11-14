package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * nv-45: cada peso se dibuja con su propia fuente. Con la fuente variable, el teléfono pintaba
 * 400, 600 y 700 con el mismo ancho, y los títulos y botones salían sin negrita.
 */
@RunWith(AndroidJUnit4::class)
class TypographyWeightTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun soraBoldIsWiderThanSemiBold() {
        val widths = measure(SoraFamily, FontWeight.SemiBold, FontWeight.Bold)
        assertTrue("Sora: $widths", widths.getValue(FontWeight.Bold) > widths.getValue(FontWeight.SemiBold))
    }

    @Test
    fun dmSansWeightsDiffer() {
        val widths = measure(DmSansFamily, FontWeight.Normal, FontWeight.Bold)
        assertTrue("DM Sans: $widths", widths.getValue(FontWeight.Bold) > widths.getValue(FontWeight.Normal))
    }

    private fun measure(family: FontFamily, vararg weights: FontWeight): Map<FontWeight, Int> {
        val widths = mutableMapOf<FontWeight, Int>()
        compose.setContent {
            Column {
                weights.forEach { weight ->
                    Text(
                        text = SAMPLE,
                        style = TextStyle(fontFamily = family, fontWeight = weight, fontSize = 30.sp),
                        onTextLayout = { widths[weight] = it.size.width }
                    )
                }
            }
        }
        compose.waitForIdle()
        return widths
    }

    private companion object {
        const val SAMPLE = "Tu dia completo"
    }
}
