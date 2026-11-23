package com.wherever.mobile.feature.business.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.CouponType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-37 (UI): la vista previa refleja tipo y valor; Publicar solo con un cupón válido. */
@RunWith(AndroidJUnit4::class)
class CouponsContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun vistaPreviaReflejaTipoYValor() {
        var state by mutableStateOf(sampleCouponForm())
        compose.setContent { WhereverTheme { CouponFormContent(state, SAMPLE_TODAY, CouponFormActions()) } }

        compose.onNodeWithTag("coupon_preview").performScrollTo()
        compose.onNode(hasText("15%")).assertExists()

        state = state.copy(type = CouponType.TWO_FOR_ONE, value = "")
        compose.onNode(hasText("2x1", substring = false)).assertExists()
    }

    @Test
    fun publicarSoloConCuponValido() {
        var state by mutableStateOf(sampleCouponForm().copy(title = ""))
        compose.setContent { WhereverTheme { CouponFormContent(state, SAMPLE_TODAY, CouponFormActions()) } }

        compose.onNodeWithTag("coupon_publish").assertIsNotEnabled()
        state = state.copy(title = "15% entre semana")
        compose.onNodeWithTag("coupon_publish").assertIsEnabled()
    }
}
