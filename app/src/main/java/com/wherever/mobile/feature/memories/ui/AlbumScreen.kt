package com.wherever.mobile.feature.memories.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.CircleIconButton
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.memories.domain.Memory
import com.wherever.mobile.feature.memories.domain.MemoryStop
import com.wherever.mobile.feature.memories.domain.Postcard
import com.wherever.mobile.feature.memories.domain.PostcardState
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import com.wherever.mobile.feature.memories.domain.Visibility

/** Fotos por recuerdo en 74_RecPlan. */
private const val PLAN_THUMBS = 3

/** A dónde lleva el álbum. */
data class AlbumNavigation(
    val onBack: () -> Unit,
    val onPostcard: (String) -> Unit,
    val onCamera: () -> Unit,
    val onPlans: () -> Unit
)

/** 73_Album, 74_RecPlan y 76_RecVacio (specs/nv-31). Se abre desde Perfil. */
@Composable
fun AlbumRoute(navigation: AlbumNavigation, viewModel: MemoriesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    // Al volver de la postal (guardada o eliminada) el álbum se recarga sin esqueleto.
    LifecycleResumeEffect(viewModel) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AlbumContent(state, navigation, viewModel::onTab, viewModel::loadMore, viewModel::load)
        WhereverSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(ScreenPadding))
    }
}

@Composable
internal fun AlbumContent(
    state: MemoriesUiState,
    navigation: AlbumNavigation,
    onTab: (AlbumTab) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit
) {
    val error = state.error
    FormScreen {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(navigation.onBack)
            Spacer(Modifier.size(Spacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.memories_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (!state.loading && error == null) {
                    Text(
                        albumSubtitle(state.postcards.size, state.memories.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // La maqueta pone Compartir aquí; Compartir vive en cada postal y aquí se abre la cámara (D-31c).
            CircleIconButton(
                ImageVector.vectorResource(R.drawable.ic_camera),
                stringResource(R.string.memories_take_postcard),
                navigation.onCamera,
                Modifier.testTag("album_camera")
            )
        }
        SegmentedControl(
            options = listOf(stringResource(R.string.memories_tab_all), stringResource(R.string.memories_tab_plans)),
            selectedIndex = state.tab.ordinal,
            onSelect = { onTab(AlbumTab.entries[it]) }
        )
        when {
            state.loading -> ListSkeleton(rows = 3)
            error != null -> ErrorState(error.asString(), onRetry)
            state.empty -> EmptyMemories(navigation)
            state.tab == AlbumTab.ALL -> PostcardGrid(state.postcards, navigation.onPostcard)
            // Con postales pero sin recuerdos de plan, «Por plan» quedaba en blanco.
            state.memories.isEmpty() -> InfoBanner(
                stringResource(R.string.memories_by_plan_empty),
                tag = "album_by_plan_empty"
            )
            else -> state.memories.forEach { MemoryCard(it) }
        }
        val cursor = if (state.tab == AlbumTab.ALL) state.postcardsCursor else state.memoriesCursor
        if (!state.loading && error == null && cursor != null) {
            WhereverTextLink(
                stringResource(R.string.memories_load_more),
                onLoadMore,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

/** "1 postal de 0 planes", con plural en cada número. */
@Composable
private fun albumSubtitle(postcards: Int, plans: Int): String = stringResource(
    R.string.memories_subtitle,
    pluralStringResource(R.plurals.memories_postcards, postcards, postcards),
    pluralStringResource(R.plurals.memories_plans, plans, plans)
)

/** 73_Album: cuadrícula de dos columnas. */
@Composable
private fun PostcardGrid(postcards: List<Postcard>, onPostcard: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
        postcards.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
                row.forEach { card ->
                    AlbumThumb(
                        photo = card.imageUrl,
                        title = card.text.ifBlank { stringResource(R.string.memories_postcard_title) },
                        subtitle = if (card.state == PostcardState.READY) {
                            memoryDate(card.createdAt)
                        } else {
                            stringResource(R.string.memories_in_review)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.M))
                            .clickable(role = Role.Button) { onPostcard(card.id) }
                            .testTag("album_postcard_${card.id}")
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** 74_RecPlan: un recuerdo por plan con sus fotos. */
@Composable
private fun MemoryCard(memory: Memory) {
    SurfaceCard(modifier = Modifier.testTag("album_memory_${memory.id}")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(memory.title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.memories_plan_meta, memoryDate(memory.date), memory.stops.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    stringResource(R.string.memories_plan_postcards, memory.postcardIds.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                memory.stops.mapNotNull { it.photoUrl }.take(PLAN_THUMBS).forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).size(Sizes.ThumbnailL).clip(RoundedCornerShape(Radius.M))
                    )
                }
            }
        }
    }
}

/** 76_RecVacio */
@Composable
private fun EmptyMemories(navigation: AlbumNavigation) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.L),
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.XXL).testTag("album_empty")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(Sizes.AvatarL).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
        ) {
            Icon(
                painterResource(R.drawable.ic_camera),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Sizes.IconXL)
            )
        }
        Text(
            stringResource(R.string.memories_empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            stringResource(R.string.memories_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        WhereverPrimaryButton(
            stringResource(R.string.memories_empty_how),
            navigation.onCamera,
            tag = "album_empty_camera"
        )
        WhereverTextLink(
            stringResource(R.string.memories_empty_plans),
            navigation.onPlans,
            Modifier.testTag("album_plans")
        )
    }
}

private val PREVIEW_NAVIGATION = AlbumNavigation({}, {}, {}, {})

private fun previewPostcard(id: String, text: String) = Postcard(
    id = id,
    imageUrl = null,
    style = PostcardStyle.CLASSIC,
    text = text,
    visibility = Visibility.PRIVATE,
    state = PostcardState.READY,
    planId = null,
    placeId = null,
    version = 1,
    createdAt = "2026-10-11T18:00:00Z"
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AlbumPreview() {
    WhereverTheme {
        AlbumContent(
            MemoriesUiState(
                loading = false,
                postcards = listOf(
                    previewPostcard("1", "Café Raíz"),
                    previewPostcard("2", "Jardín del Cerro"),
                    previewPostcard("3", "Fonda Doña Meche")
                ),
                memories = listOf(Memory("m1", "p1", "Paseo en pareja", "2026-10-11", emptyList(), listOf("1")))
            ),
            PREVIEW_NAVIGATION,
            {},
            {},
            {}
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AlbumByPlanPreview() {
    WhereverTheme {
        AlbumContent(
            MemoriesUiState(
                tab = AlbumTab.BY_PLAN,
                loading = false,
                postcards = listOf(previewPostcard("1", "Café Raíz")),
                memories = listOf(
                    Memory(
                        "m1",
                        "p1",
                        "Paseo en pareja",
                        "2026-10-11",
                        listOf(MemoryStop("Café Raíz", "10:00", null)),
                        listOf("1", "2", "3", "4")
                    ),
                    Memory("m2", "p2", "Viernes de tacos", "2026-10-03", emptyList(), listOf("5", "6", "7"))
                )
            ),
            PREVIEW_NAVIGATION,
            {},
            {},
            {}
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AlbumEmptyPreview() {
    WhereverTheme { AlbumContent(MemoriesUiState(loading = false), PREVIEW_NAVIGATION, {}, {}, {}) }
}
