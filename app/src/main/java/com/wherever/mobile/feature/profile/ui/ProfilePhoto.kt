package com.wherever.mobile.feature.profile.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.InitialAvatar
import com.wherever.mobile.core.designsystem.RouteBlue
import java.io.File

/** Foto de perfil o, si no hay, la inicial. */
@Composable
fun ProfileAvatar(name: String, photoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    if (photoUrl != null) {
        AsyncImage(
            model = photoUrl,
            contentDescription = stringResource(R.string.photo_avatar_a11y, name),
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(CircleShape).testTag("profile_photo")
        )
    } else {
        InitialAvatar(name, size, modifier)
    }
}

/**
 * Avatar que se toca para cambiar la foto: galería con el Photo Picker del sistema (sin permiso de
 * almacenamiento) o cámara con `TakePicture` y un `FileProvider` (sin permiso `CAMERA`, porque la
 * app no usa la cámara directamente). specs/nv-50.
 */
@Composable
fun EditableProfileAvatar(
    name: String,
    photoUrl: String?,
    state: ProfilePhotoUiState,
    actions: ProfilePhotoActions,
    size: Dp = 88.dp
) {
    val context = LocalContext.current
    var pendingCamera by rememberSaveable { mutableStateOf<String?>(null) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        actions.onPicked(uri?.toString())
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        actions.onPicked(pendingCamera.takeIf { taken })
        pendingCamera = null
    }
    val changeLabel = stringResource(R.string.photo_change)
    Box(
        modifier = Modifier
            .size(size)
            .clickable(enabled = !state.uploading, onClick = actions.onOpen)
            .semantics {
                role = Role.Button
                contentDescription = changeLabel
            }
            .testTag("profile_photo_edit")
    ) {
        ProfileAvatar(name, state.preview ?: photoUrl, size)
        if (state.uploading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(size).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f))
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
            }
        } else {
            // Fuera del recorte circular, para que el lápiz se vea completo.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(RouteBlue)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
    if (state.choosing) {
        AlertDialog(
            onDismissRequest = actions.onClose,
            title = { Text(stringResource(R.string.photo_change)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.testTag("photo_gallery")
                    ) { Text(stringResource(R.string.photo_from_gallery)) }
                    TextButton(
                        onClick = {
                            val uri = context.newCameraUri()
                            pendingCamera = uri.toString()
                            camera.launch(uri)
                        },
                        modifier = Modifier.testTag("photo_camera")
                    ) { Text(stringResource(R.string.photo_from_camera)) }
                }
            },
            confirmButton = { TextButton(onClick = actions.onClose) { Text(stringResource(R.string.common_cancel)) } }
        )
    }
}

data class ProfilePhotoActions(val onOpen: () -> Unit, val onClose: () -> Unit, val onPicked: (String?) -> Unit)

/** Archivo temporal en `cache/fotos/`, el único directorio que comparte el `FileProvider`. */
private fun Context.newCameraUri(): Uri {
    // Solo se guarda la última foto de cámara: la anterior ya se subió o se descartó.
    val dir = File(cacheDir, "fotos").apply {
        mkdirs()
        listFiles()?.forEach { it.delete() }
    }
    val file = File(dir, "camara_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "$packageName.fotos", file)
}

@Composable
internal fun rememberPhotoActions(viewModel: ProfilePhotoViewModel): ProfilePhotoActions = remember(viewModel) {
    ProfilePhotoActions(viewModel::openChooser, viewModel::closeChooser, viewModel::onPicked)
}
