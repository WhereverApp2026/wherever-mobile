package com.wherever.mobile.feature.reviews.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.reviews.domain.PlaceBrief
import com.wherever.mobile.feature.reviews.domain.PlaceReview
import com.wherever.mobile.feature.reviews.domain.ReportReason
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-32 (UI): Útil cambia el contador; Reportar abre los motivos. */
@RunWith(AndroidJUnit4::class)
class PlaceReviewsContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val review = PlaceReview("r1", "ana", null, 5, "Rico", emptyList(), 3, null, "2026-11-19T18:00:00Z")

    private var state by mutableStateOf(
        PlaceReviewsUiState(
            placeName = "Café Raíz",
            place = PlaceBrief("p1", "Café Raíz", null, 4.8, 126),
            loading = false,
            reviews = listOf(review)
        )
    )
    private val sent = mutableListOf<ReportReason>()

    private fun show() {
        compose.setContent {
            WhereverTheme {
                // La hoja de motivos tapa la pantalla, como la WhereverBottomSheet real: debajo de una pantalla que
                // ocupa todo el alto quedaba fuera de la vista y la prueba fallaba en el emulador de la CI.
                val form = state.report
                if (form == null) {
                    PlaceReviewsContent(
                        state,
                        PlaceReviewsActions(
                            onBack = {},
                            onQuery = {},
                            onUseful = { r ->
                                val marked = r.id !in state.usefulMarked
                                state = state.copy(
                                    usefulMarked = if (marked) state.usefulMarked + r.id else state.usefulMarked - r.id,
                                    reviews = state.reviews.map { it.copy(useful = it.useful + if (marked) 1 else -1) }
                                )
                            },
                            onReport = { state = state.copy(report = ReviewReportForm(it.id)) },
                            onLoadMore = {},
                            onRetry = {}
                        )
                    )
                } else {
                    ReportReasons(
                        form,
                        onReason = { state = state.copy(report = form.copy(reason = it)) },
                        onSend = { form.reason?.let(sent::add) }
                    )
                }
            }
        }
    }

    @Test
    fun utilCambiaElContador() {
        show()
        val count = compose.onNodeWithTag("place_review_useful_count_r1", useUnmergedTree = true)
        count.assertTextEquals(context.getString(R.string.reviews_useful, 3))

        compose.onNodeWithTag("place_review_useful_r1").performScrollTo().performClick()
        count.assertTextEquals(context.getString(R.string.reviews_useful, 4))

        compose.onNodeWithTag("place_review_useful_r1").performClick()
        count.assertTextEquals(context.getString(R.string.reviews_useful, 3))
    }

    @Test
    fun reportarAbreLosMotivos() {
        show()
        compose.onNodeWithTag("review_report_reasons").assertDoesNotExist()

        compose.onNodeWithTag("place_review_report_r1").performScrollTo().performClick()
        compose.onNodeWithTag("review_report_reasons").assertIsDisplayed()
        compose.onNodeWithTag("review_report_send").assertIsNotEnabled()

        compose.onNodeWithTag("review_reason_spam").performClick()
        compose.onNodeWithTag("review_report_send").assertIsEnabled().performClick()
        assertEquals(listOf(ReportReason.SPAM), sent)
    }
}
