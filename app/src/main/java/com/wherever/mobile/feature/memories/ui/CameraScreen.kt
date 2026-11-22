package com.wherever.mobile.feature.memories.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.InkRaised
import com.wherever.mobile.core.designsystem.Mist
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import java.io.File

/**
 * 71_Camara (specs/nv-31). La foto la toma la cámara del sistema (`TakePicture`, sin CameraX: stack de la
 * constitución, D-31b). El permiso `CAMERA` se pide al tocar el disparador; sin él se elige de la galería.
 */
@Composable
fun CameraRoute(placeName: String, onClose: () -> Unit, onPicked: (String) -> Unit) {
    val context = LocalContext.current
    var pending by rememberSaveable { mutableStateOf<String?>(null) }
    var denied by rememberSaveable { mutableStateOf(false) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        val uri = pending
        pending = null
        if (taken && uri != null) onPicked(uri)
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = context.newPostcardUri()
            pending = uri.toString()
            camera.launch(uri)
        } else {
            denied = true
        }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onPicked(it.toString()) }
    }
    val takePhoto = remember(context) {
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                val uri = context.newPostcardUri()
                pending = uri.toString()
                camera.launch(uri)
            } else {
                permission.launch(Manifest.permission.CAMERA)
            }
        }
    }
    CameraContent(
        placeName = placeName,
        denied = denied,
        onClose = onClose,
        onShutter = takePhoto,
        onGallery = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    )
}

/** Archivo nuevo en `cache/fotos/`, el directorio que comparte el `FileProvider` de la app. */
private fun Context.newPostcardUri(): Uri {
    val dir = File(cacheDir, "fotos").apply { mkdirs() }
    val file = File(dir, "postal_camara_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "$packageName.fotos", file)
}

@Composable
internal fun CameraContent(
    placeName: String,
    denied: Boolean,
    onClose: () -> Unit,
    onShutter: () -> Unit,
    onGallery: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Ink).safeDrawingPadding().padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.L)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WhereverIconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.memories_close), tint = Mist)
            }
            Spacer(Modifier.weight(1f))
            if (placeName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(
                        InkRaised,
                        CircleShape
                    ).padding(horizontal = Spacing.M, vertical = Spacing.XS)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Mist,
                        modifier = Modifier.size(Sizes.IconS)
                    )
                    Text(placeName, style = MaterialTheme.typography.labelLarge, color = Mist)
                }
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(Sizes.TouchTarget))
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(Strokes.Thick, Mist, RoundedCornerShape(Radius.XL))
                .padding(Spacing.XXL)
        ) {
            Text(
                stringResource(if (denied) R.string.memories_camera_denied else R.string.memories_camera_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = Mist,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("camera_hint")
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            val gallery = stringResource(R.string.memories_camera_gallery)
            WhereverIconButton(onClick = onGallery, modifier = Modifier.testTag("camera_gallery")) {
                Icon(painterResource(R.drawable.ic_gallery), contentDescription = gallery, tint = Mist)
            }
            Spacer(Modifier.weight(1f))
            val take = stringResource(R.string.memories_camera_take)
            Box(
                modifier = Modifier
                    .size(Sizes.ProgressLarge + Spacing.L)
                    .border(Strokes.Thick, Mist, CircleShape)
                    .padding(Spacing.XS)
                    .background(Mist, CircleShape)
                    .clickable(role = Role.Button, onClick = onShutter)
                    .semantics { contentDescription = take }
                    .testTag("camera_shutter")
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(Sizes.TouchTarget))
        }
    }
}

@Preview
@Composable
internal fun CameraPreview() {
    WhereverTheme { CameraContent("Café Raíz", denied = false, onClose = {}, onShutter = {}, onGallery = {}) }
}
