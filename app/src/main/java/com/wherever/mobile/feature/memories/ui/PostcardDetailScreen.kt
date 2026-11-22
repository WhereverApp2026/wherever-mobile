package com.wherever.mobile.feature.memories.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.memories.domain.PlaceInfo
import com.wherever.mobile.feature.memories.domain.PlanInfo
import com.wherever.mobile.feature.memories.domain.Postcard
import com.wherever.mobile.feature.memories.domain.PostcardState
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import com.wherever.mobile.feature.memories.domain.PostcardView
import com.wherever.mobile.feature.memories.domain.Visibility

/** 75_PostalVer y sus variantes por lugar (specs/nv-31). */
@Composable
fun PostcardDetailRoute(onBack: () -> Unit, viewModel: PostcardDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    LaunchedEffect(viewModel) { viewModel.deleted.collect { onBack() } }
    Box(modifier = Modifier.fillMaxSize()) {
        PostcardDetailContent(
            state,
            PostcardDetailActions(
                onBack = onBack,
                onShare = { state.view?.postcard?.imageUrl?.let { context.shareLink(it) } },
                onDownload = viewModel::download,
                onDelete = viewModel::askDelete,
                onRetry = viewModel::load
            )
        )
        WhereverSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(ScreenPadding))
    }
    if (state.confirmDelete) DeletePostcardDialog(viewModel::confirmDelete, viewModel::cancelDelete)
}

@Composable
internal fun DeletePostcardDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        title = stringResource(R.string.memories_delete_title),
        message = stringResource(R.string.memories_delete_message),
        confirmText = stringResource(R.string.memories_delete),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        modifier = Modifier.testTag("postcard_delete_dialog")
    )
}

/** Comparte el enlace de la imagen publicada (no de la cuarentena: `imagen_url` solo existe publicada). */
private fun Context.shareLink(url: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
    try {
        startActivity(Intent.createChooser(send, getString(R.string.memories_share)))
    } catch (_: ActivityNotFoundException) {
        // Sin apps para compartir: no hay nada más que hacer.
    }
}

@Composable
internal fun PostcardDetailContent(state: PostcardDetailUiState, actions: PostcardDetailActions) {
    val view = state.view
    val error = state.error
    when {
        state.loading -> LoadingState()
        state.gone -> FormScreen {
            TopTitleRow(stringResource(R.string.memories_postcard_title), actions.onBack)
            ErrorBanner(stringResource(R.string.memories_postcard_gone))
        }
        error != null || view == null -> ErrorState(error?.asString().orEmpty(), actions.onRetry)
        else -> PostcardBody(view, state, actions)
    }
}

@Composable
private fun PostcardBody(view: PostcardView, state: PostcardDetailUiState, actions: PostcardDetailActions) {
    val card = view.postcard
    FormScreen(
        footer = {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
                WhereverPrimaryButton(
                    text = stringResource(R.string.memories_share_short),
                    onClick = actions.onShare,
                    enabled = card.imageUrl != null,
                    icon = Icons.Outlined.Share,
                    modifier = Modifier.weight(1f),
                    tag = "postcard_share"
                )
                WhereverOutlinedButton(
                    text = stringResource(R.string.memories_download),
                    onClick = actions.onDownload,
                    enabled = card.imageUrl != null && !state.saving,
                    modifier = Modifier.weight(1f),
                    tag = "postcard_download"
                )
            }
            WhereverTextLink(
                stringResource(R.string.memories_delete),
                actions.onDelete,
                enabled = !state.deleting,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("postcard_delete")
            )
        }
    ) {
        TopTitleRow(stringResource(R.string.memories_postcard_title), actions.onBack)
        val placeName = view.place?.name.orEmpty()
        PostcardPreview(
            photo = card.imageUrl,
            style = card.style,
            title = card.text.ifBlank { stringResource(R.string.memories_greeting, placeName) },
            subtitle = listOf(placeName, memoryDate(card.createdAt)).filter { it.isNotBlank() }.joinToString(" · ")
        )
        if (card.state != PostcardState.READY) {
            Text(
                stringResource(R.string.memories_in_review),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (view.place != null || view.plan != null) {
            SurfaceCard {
                view.place?.let { place ->
                    InfoRow(
                        Icons.Outlined.LocationOn,
                        place.name,
                        listOf(place.category, place.address).filter { it.isNotBlank() }.joinToString(" · ")
                    )
                }
                view.plan?.let { plan ->
                    InfoRow(
                        Icons.Outlined.DateRange,
                        plan.title,
                        stringResource(R.string.memories_plan_meta, memoryDate(plan.date), plan.stops)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier.fillMaxWidth().heightIn(
            min = Sizes.RowMinHeight
        ).padding(horizontal = Spacing.L, vertical = Spacing.S)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(Sizes.AvatarS)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(Radius.M))
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Sizes.IconM)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Una vista previa por estilo: son las variantes `PostalVer · *` de la maqueta con otros datos. */
internal fun previewPostcardView(style: PostcardStyle, place: String) = PostcardView(
    postcard = Postcard(
        id = "c1",
        imageUrl = "https://ejemplo.invalid/postal.jpg",
        style = style,
        text = "",
        visibility = Visibility.PRIVATE,
        state = PostcardState.READY,
        planId = "p1",
        placeId = "n1",
        version = 1,
        createdAt = "2026-10-11T18:00:00Z"
    ),
    place = PlaceInfo("n1", place, "Cafetería", "Av. Hidalgo 23"),
    plan = PlanInfo("p1", "Paseo en pareja", "2026-10-11", stops = 3)
)

private val PREVIEW_ACTIONS = PostcardDetailActions({}, {}, {}, {}, {})

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PostcardDetailPreview() {
    WhereverTheme {
        PostcardDetailContent(
            PostcardDetailUiState(loading = false, view = previewPostcardView(PostcardStyle.CLASSIC, "Café Raíz")),
            PREVIEW_ACTIONS
        )
    }
}

@Preview
@Composable
internal fun PostcardDetailPolaroidPreview() {
    WhereverTheme {
        PostcardDetailContent(
            PostcardDetailUiState(
                loading = false,
                view = previewPostcardView(PostcardStyle.POLAROID, "Jardín del Cerro")
            ),
            PREVIEW_ACTIONS
        )
    }
}

@Preview
@Composable
internal fun PostcardDetailPassportPreview() {
    WhereverTheme {
        PostcardDetailContent(
            PostcardDetailUiState(loading = false, view = previewPostcardView(PostcardStyle.PASSPORT, "Mirador")),
            PREVIEW_ACTIONS
        )
    }
}

@Preview
@Composable
internal fun PostcardGonePreview() {
    WhereverTheme { PostcardDetailContent(PostcardDetailUiState(loading = false, gone = true), PREVIEW_ACTIONS) }
}
