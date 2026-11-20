package com.wherever.mobile.feature.status.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme

/** Capa sobre la app: Actualizar (bloquea), mantenimiento o 89_SinConexion. "Atrás" cierra Sin conexión. */
@Composable
fun AppStatusOverlay(state: AppStatusUiState, onRetry: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    when {
        state.updateRequired -> UpdateRequiredContent(onUpdate = { openStore(context) })
        state.blocking -> {
            BackHandler(onBack = onDismiss)
            OfflineContent(state, onRetry)
        }
    }
}

@Composable
internal fun OfflineContent(state: AppStatusUiState, onRetry: () -> Unit) {
    val maintenance = state.maintenance && !state.offline
    val wifiOff = painterResource(R.drawable.ic_wifi_off)
    val refresh = rememberVectorPainter(Icons.Outlined.Refresh)
    StatusLayout(
        icon = if (maintenance) refresh else wifiOff,
        title = stringResource(if (maintenance) R.string.maintenance_title else R.string.offline_title),
        body = stringResource(if (maintenance) R.string.maintenance_body else R.string.offline_body),
        action = stringResource(R.string.common_retry),
        onAction = onRetry,
        loading = state.checking,
        tag = "app_offline"
    ) {
        state.notice?.let { notice ->
            InfoBanner(
                text = stringResource(
                    when (notice) {
                        StatusNotice.STILL_OFFLINE -> R.string.offline_still
                        StatusNotice.SERVER_DOWN -> R.string.offline_server_down
                    }
                ),
                tag = "app_offline_notice"
            )
        }
        // "Disponible sin internet": planes y reservas aún no se guardan en Room (docs/pendientes.md); sin datos
        // guardados la sección se oculta (specs/nv-16, estado Vacío).
    }
}

@Composable
internal fun UpdateRequiredContent(onUpdate: () -> Unit) {
    StatusLayout(
        icon = rememberVectorPainter(Icons.Outlined.Refresh),
        title = stringResource(R.string.update_title),
        body = stringResource(R.string.update_body),
        action = stringResource(R.string.update_action),
        onAction = onUpdate,
        loading = false,
        tag = "app_update"
    )
}

@Suppress("LongParameterList") // Las dos variantes cambian ícono, textos, acción y etiqueta.
@Composable
private fun StatusLayout(
    icon: Painter,
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit,
    loading: Boolean,
    tag: String,
    extra: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(ScreenPadding)
            .testTag(tag)
    ) {
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.L, Alignment.CenterVertically),
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(Sizes.AvatarXL)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Sizes.IconXXL)
                    )
                }
                Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text(
                    body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                extra()
            }
        }
        WhereverPrimaryButton(text = action, onClick = onAction, loading = loading, tag = "${tag}_action")
    }
}

private fun openStore(context: Context) {
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
    try {
        context.startActivity(market)
    } catch (_: ActivityNotFoundException) {
        val web = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
        context.startActivity(Intent(Intent.ACTION_VIEW, web))
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun OfflinePreview() {
    WhereverTheme { OfflineContent(AppStatusUiState(offline = true), onRetry = {}) }
}

@Preview
@Composable
internal fun OfflineNoticePreview() {
    WhereverTheme {
        OfflineContent(AppStatusUiState(offline = true, notice = StatusNotice.STILL_OFFLINE), onRetry = {})
    }
}

@Preview
@Composable
internal fun MaintenancePreview() {
    WhereverTheme { OfflineContent(AppStatusUiState(maintenance = true), onRetry = {}) }
}

@Preview
@Composable
internal fun UpdateRequiredPreview() {
    WhereverTheme { UpdateRequiredContent(onUpdate = {}) }
}
