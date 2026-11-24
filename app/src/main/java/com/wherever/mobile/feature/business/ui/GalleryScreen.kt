package com.wherever.mobile.feature.business.ui

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.GoldTint
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.WhereverBottomSheet
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverShapes
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.BusinessPhoto
import com.wherever.mobile.feature.business.domain.MAX_PHOTOS
import java.io.File
import kotlin.math.roundToInt

/** Tres fotos por fila, como la maqueta. */
private const val GALLERY_COLUMNS = 3

/** La foto que se arrastra crece un poco. */
private const val DRAG_SCALE = 1.05f

/** Portada a dos tercios de la altura de una foto grande. */
private const val COVER_RATIO = 0.66f

/** 87_NegGaleria y la hoja 88_NegAgregarFoto (specs/nv-38). */
@Composable
internal fun GalleryRoute(businessId: String, onBack: () -> Unit, viewModel: GalleryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(businessId) { viewModel.start(businessId) }
    val context = LocalContext.current
    var pendingCamera by rememberSaveable { mutableStateOf<String?>(null) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.onPicked(uri?.toString())
    }
    // Cámara del sistema con `TakePicture` y el `FileProvider` de fotos: sin permiso CAMERA (como nv-50).
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        viewModel.onPicked(pendingCamera.takeIf { taken })
        pendingCamera = null
    }
    GalleryContent(
        state,
        GalleryActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onMove = viewModel::move,
            onCover = viewModel::makeCover,
            onDelete = viewModel::delete,
            onOpenAdd = viewModel::openAdd,
            onCloseAdd = viewModel::closeAdd,
            onCamera = {
                val uri = context.newBusinessCameraUri()
                pendingCamera = uri.toString()
                camera.launch(uri)
            },
            onGallery = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onSave = viewModel::save,
            onMessageShown = viewModel::messageShown
        )
    )
}

@Composable
internal fun GalleryContent(state: GalleryUiState, actions: GalleryActions) {
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            actions.onMessageShown()
        }
    }
    val error = state.error
    FormScreen(
        footer = {
            WhereverSnackbarHost(snackbar)
            WhereverPrimaryButton(
                text = stringResource(R.string.gallery_save),
                onClick = actions.onSave,
                enabled = state.canSave,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
                tag = "gallery_save"
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBack)
            Spacer(Modifier.width(Spacing.M))
            Column {
                Text(stringResource(R.string.biz_profile_photos), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.gallery_count, state.photos.size, MAX_PHOTOS),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        when {
            state.loading -> ListSkeleton(rows = 3)
            error != null -> ErrorState(error.asString(), actions.onRetry, Modifier.heightIn(max = Sizes.HeroImage))
            else -> {
                Cover(state.photos.firstOrNull())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.gallery_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(R.string.gallery_drag_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PhotoGrid(state, actions)
                InfoBanner(
                    stringResource(R.string.gallery_tip),
                    container = GoldTint,
                    contentColor = Ink,
                    tag = "gallery_tip"
                )
            }
        }
    }
    if (state.addOpen) AddPhotoSheet(actions)
}

@Composable
private fun Cover(photo: BusinessPhoto?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Sizes.HeroImage * COVER_RATIO)
            .clip(WhereverShapes.Card)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("gallery_cover")
    ) {
        photo?.let {
            AsyncImage(
                model = it.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        StatusPill(stringResource(R.string.gallery_cover), modifier = Modifier.padding(Spacing.S))
    }
}

/** Todas las fotos (la primera también arriba como portada) y «Agregar». Mantener y arrastrar reordena. */
@Composable
private fun PhotoGrid(state: GalleryUiState, actions: GalleryActions) {
    var tile by remember { mutableStateOf(IntSize.Zero) }
    var dragging by remember { mutableIntStateOf(-1) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val cells: List<BusinessPhoto?> = state.photos + if (state.canAdd) listOf(null) else emptyList()
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        cells.chunked(GALLERY_COLUMNS).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                row.forEachIndexed { column, photo ->
                    val index = rowIndex * GALLERY_COLUMNS + column
                    val tileModifier = Modifier.weight(1f).aspectRatio(1f).onSizeChanged { tile = it }
                    if (photo == null) {
                        AddTile(state.uploading, actions.onOpenAdd, tileModifier)
                    } else {
                        PhotoTile(
                            photo = photo,
                            busy = photo.id in state.busy,
                            actions = actions,
                            modifier = tileModifier
                                .graphicsLayer {
                                    if (index == dragging) {
                                        translationX = offset.x
                                        translationY = offset.y
                                        scaleX = DRAG_SCALE
                                        scaleY = DRAG_SCALE
                                    }
                                }
                                .pointerInput(photo.id, index, state.photos.size, tile) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            dragging = index
                                            offset = Offset.Zero
                                        },
                                        onDragEnd = {
                                            val target = targetIndex(index, offset, tile, state.photos.size)
                                            dragging = -1
                                            offset = Offset.Zero
                                            actions.onMove(index, target)
                                        },
                                        onDragCancel = {
                                            dragging = -1
                                            offset = Offset.Zero
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            offset += amount
                                        }
                                    )
                                }
                        )
                    }
                }
                repeat(GALLERY_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** A qué lugar cae la foto según cuánto se arrastró (una celda por ancho o alto de foto). */
internal fun targetIndex(from: Int, offset: Offset, tile: IntSize, count: Int): Int {
    if (tile.width == 0 || tile.height == 0 || count == 0) return from
    val columns = (offset.x / tile.width).roundToInt()
    val rows = (offset.y / tile.height).roundToInt()
    return (from + columns + rows * GALLERY_COLUMNS).coerceIn(0, count - 1)
}

@Composable
private fun PhotoTile(photo: BusinessPhoto, busy: Boolean, actions: GalleryActions, modifier: Modifier) {
    var menu by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.clip(
            WhereverShapes.Card
        ).background(MaterialTheme.colorScheme.surfaceVariant).testTag("gallery_photo_${photo.id}")
    ) {
        AsyncImage(
            model = photo.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            WhereverIconButton(onClick = {
                menu = true
            }, enabled = !busy, modifier = Modifier.testTag("gallery_menu_${photo.id}")) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(
                        Sizes.IconL
                    ).clip(WhereverShapes.Pill).background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.gallery_options),
                        modifier = Modifier.size(Sizes.IconS)
                    )
                }
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.gallery_make_cover)) },
                    onClick = {
                        menu = false
                        actions.onCover(photo)
                    },
                    modifier = Modifier.testTag("gallery_cover_${photo.id}")
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.gallery_delete), color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        menu = false
                        actions.onDelete(photo)
                    },
                    modifier = Modifier.testTag("gallery_delete_${photo.id}")
                )
            }
        }
    }
}

@Composable
private fun AddTile(uploading: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(WhereverShapes.Card)
            .border(Strokes.Hairline, MaterialTheme.colorScheme.primary, WhereverShapes.Card)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(enabled = !uploading, onClick = onClick)
            .testTag("gallery_add")
    ) {
        Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            stringResource(if (uploading) R.string.gallery_uploading else R.string.gallery_add),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** 88_NegAgregarFoto. «Importar de Instagram» está diferido en el backend: no se dibuja (D-38a). */
@Composable
private fun AddPhotoSheet(actions: GalleryActions) {
    WhereverBottomSheet(onDismissRequest = actions.onCloseAdd, tag = "gallery_add_sheet") {
        AddPhotoOptions(actions)
    }
}

@Composable
private fun AddPhotoOptions(actions: GalleryActions) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.S),
        modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.L)
    ) {
        Text(
            stringResource(R.string.gallery_add_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        SheetOption(
            ImageVector.vectorResource(R.drawable.ic_wh_camera),
            R.string.gallery_camera,
            R.string.gallery_camera_hint,
            "gallery_camera",
            actions.onCamera
        )
        SheetOption(
            Icons.Outlined.Add,
            R.string.gallery_pick,
            R.string.gallery_pick_hint,
            "gallery_pick",
            actions.onGallery
        )
        WhereverOutlinedButton(
            text = stringResource(R.string.common_cancel),
            onClick = actions.onCloseAdd,
            modifier = Modifier.fillMaxWidth(),
            tag = "gallery_add_cancel"
        )
    }
}

@Composable
private fun SheetOption(icon: ImageVector, title: Int, hint: Int, tag: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.RowMinHeight).clickable(onClick = onClick).testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(
                Sizes.IconXL
            ).clip(WhereverShapes.Small).background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Sizes.IconS)
            )
        }
        Spacer(Modifier.width(Spacing.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(
                stringResource(hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
    }
}

/** Archivo temporal en `cache/fotos/`, el único directorio que comparte el `FileProvider` de fotos. */
private fun Context.newBusinessCameraUri(): Uri {
    val dir = File(cacheDir, "fotos").apply {
        mkdirs()
        listFiles()?.forEach { it.delete() }
    }
    val file = File(dir, "negocio_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "$packageName.fotos", file)
}

@Preview(name = "87_NegGaleria claro")
@Preview(name = "87_NegGaleria oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun GalleryPreview() {
    WhereverTheme { GalleryContent(sampleGallery(), GalleryActions()) }
}

@Preview(name = "88_NegAgregarFoto")
@Composable
internal fun AddPhotoSheetPreview() {
    WhereverTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) { AddPhotoOptions(GalleryActions()) }
    }
}
