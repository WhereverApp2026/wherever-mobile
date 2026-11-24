package com.wherever.mobile.feature.business.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-36 (UI): Publicar deshabilitado sin texto; tocar una reseña abre la respuesta. */
@RunWith(AndroidJUnit4::class)
class BusinessReviewsContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun publicarDeshabilitadoSinTexto() {
        var state by mutableStateOf(ReviewReplyUiState())
        compose.setContent { WhereverTheme { ReviewReplyContent(state, sampleReplyTarget(), ReviewReplyActions()) } }

        compose.onNodeWithTag("reply_publish").assertIsNotEnabled()
        state = state.copy(text = "   ")
        compose.onNodeWithTag("reply_publish").assertIsNotEnabled()
        state = state.copy(text = SAMPLE_REPLY_TEXT)
        compose.onNodeWithTag("reply_publish").assertIsEnabled()
    }

    @Test
    fun tocarResenaAbreResponder() {
        var opened: String? = null
        compose.setContent {
            WhereverTheme {
                BusinessReviewsContent(sampleBusinessReviews(), BusinessReviewsActions(onReply = { opened = it.id }))
            }
        }

        compose.onNodeWithTag("biz_review_r2").performScrollTo().performClick()
        assertEquals("r2", opened)
    }
}
