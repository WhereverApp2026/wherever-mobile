package com.wherever.mobile.feature.memories.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-31 (UI): 76_RecVacio con Ver mis planes; Eliminar abre la confirmación. */
@RunWith(AndroidJUnit4::class)
class MemoriesContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun vacioMuestraVerMisPlanes() {
        val events = mutableListOf<String>()
        compose.setContent {
            WhereverTheme {
                AlbumContent(
                    MemoriesUiState(loading = false),
                    AlbumNavigation({}, {}, onCamera = { events += "camera" }, onPlans = { events += "plans" }),
                    {},
                    {},
                    {}
                )
            }
        }

        compose.onNodeWithTag("album_empty").assertIsDisplayed()
        compose.onNodeWithTag("album_plans").performScrollTo().performClick()
        compose.onNodeWithTag("album_empty_camera").performScrollTo().performClick()
        assertEquals(listOf("plans", "camera"), events)
    }

    @Test
    fun eliminarAbreLaConfirmacion() {
        var state by mutableStateOf(
            PostcardDetailUiState(loading = false, view = previewPostcardView(PostcardStyle.CLASSIC, "Café Raíz"))
        )
        var confirmed = false
        compose.setContent {
            WhereverTheme {
                Box {
                    PostcardDetailContent(
                        state,
                        PostcardDetailActions(
                            onBack = {},
                            onShare = {},
                            onDownload = {},
                            onDelete = { state = state.copy(confirmDelete = true) },
                            onRetry = {}
                        )
                    )
                    if (state.confirmDelete) {
                        DeletePostcardDialog(onConfirm = { confirmed = true }, onDismiss = {})
                    }
                }
            }
        }

        compose.onNodeWithTag("postcard_delete_dialog").assertDoesNotExist()
        compose.onNodeWithTag("postcard_delete").performClick()
        compose.onNodeWithTag("postcard_delete_dialog").assertIsDisplayed()
        assertEquals(false, confirmed)
    }
}
