package com.wherever.mobile.feature.auth.ui

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.GoldTint
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.StepHeader
import com.wherever.mobile.core.designsystem.SunsetTint
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme

/** Permisos que pide "Permitir y continuar": ubicación aproximada y, desde Android 13, avisos. */
private val REQUESTED: Array<String> = buildList {
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
}.toTypedArray()

/** 08_Permisos (paso 3 de 3, specs/nv-11). */
@Composable
fun PermissionsRoute(onBack: () -> Unit, onFinished: () -> Unit, viewModel: PermissionsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) { if (state.done) onFinished() }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        viewModel.onSystemResult(
            location = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true,
            // Antes de Android 13 los avisos no necesitan permiso.
            notifications = result[Manifest.permission.POST_NOTIFICATIONS] ?: true
        )
    }
    PermissionsContent(
        state = state,
        actions = PermissionsActions(
            onAllow = { launcher.launch(REQUESTED) },
            onNotNow = viewModel::onNotNow,
            onRetry = viewModel::retry,
            onContinue = viewModel::continueWithoutSaving,
            onBack = onBack
        )
    )
}

private data class PermissionsActions(
    val onAllow: () -> Unit,
    val onNotNow: () -> Unit,
    val onRetry: () -> Unit,
    val onContinue: () -> Unit,
    val onBack: () -> Unit
)

@Composable
private fun PermissionsContent(state: PermissionsUiState, actions: PermissionsActions) {
    val failed = state.error != null
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(if (failed) R.string.common_retry else R.string.permissions_allow),
                onClick = if (failed) actions.onRetry else actions.onAllow,
                loading = state.saving,
                tag = "permissions_allow"
            )
            WhereverTextLink(
                text = stringResource(if (failed) R.string.permissions_continue else R.string.permissions_not_now),
                onClick = if (failed) actions.onContinue else actions.onNotNow,
                enabled = !state.saving,
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("permissions_not_now")
            )
        }
    ) {
        StepHeader(
            stringResource(R.string.permissions_step),
            step = ONBOARDING_STEPS,
            total = ONBOARDING_STEPS,
            onBack = actions.onBack
        )
        TitleBlock(stringResource(R.string.permissions_title), subtitle = stringResource(R.string.permissions_subtitle))
        state.error?.let { ErrorBanner(it.asString()) }
        PermissionCard(
            icon = rememberVectorPainter(Icons.Outlined.LocationOn),
            iconTint = MaterialTheme.colorScheme.primary,
            circle = MaterialTheme.colorScheme.primaryContainer,
            title = stringResource(R.string.permissions_location_title),
            body = stringResource(R.string.permissions_location_body),
            required = true,
            tag = "permissions_location"
        )
        PermissionCard(
            icon = rememberVectorPainter(Icons.Outlined.Notifications),
            iconTint = Ink,
            circle = GoldTint,
            title = stringResource(R.string.permissions_notifications_title),
            body = stringResource(R.string.permissions_notifications_body),
            required = false,
            tag = "permissions_notifications"
        )
        PermissionCard(
            icon = painterResource(R.drawable.ic_camera),
            iconTint = Mint,
            circle = MintTint,
            title = stringResource(R.string.permissions_camera_title),
            body = stringResource(R.string.permissions_camera_body),
            required = false,
            tag = "permissions_camera"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Sizes.IconM)
            )
            Text(
                stringResource(R.string.permissions_privacy_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Suppress("LongParameterList") // Cada tarjeta de la maqueta cambia ícono, colores, textos y etiqueta.
@Composable
private fun PermissionCard(
    icon: Painter,
    iconTint: Color,
    circle: Color,
    title: String,
    body: String,
    required: Boolean,
    tag: String
) {
    SurfaceCard(modifier = Modifier.testTag(tag)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.L),
            horizontalArrangement = Arrangement.spacedBy(Spacing.L),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(Sizes.AvatarList).background(circle, CircleShape)
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(Sizes.IconL))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (required) {
                StatusPill(stringResource(R.string.permissions_required), container = SunsetTint, contentColor = Ink)
            } else {
                StatusPill(
                    stringResource(R.string.permissions_optional),
                    container = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PermissionsPreview() {
    WhereverTheme {
        PermissionsContent(PermissionsUiState(), PermissionsActions({}, {}, {}, {}, {}))
    }
}
