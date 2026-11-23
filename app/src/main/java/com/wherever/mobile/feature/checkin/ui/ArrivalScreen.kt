package com.wherever.mobile.feature.checkin.ui

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverIcons
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatTime
import com.wherever.mobile.feature.checkin.domain.Checkin
import java.time.ZoneId

/** Llegada a una parada y 26_CheckinOk (specs/nv-30). */
@Composable
fun ArrivalRoute(navigation: ArrivalNavigation, viewModel: ArrivalViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scanPrompt = stringResource(R.string.arrival_scan_prompt)
    val scanner = rememberLauncherForActivityResult(ScanContract()) { result -> viewModel.onScanned(result.contents) }
    // La cámara y la ubicación se piden aquí, en contexto (constitución §6).
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scanner.launch(
                ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setPrompt(scanPrompt)
                    .setBeepEnabled(false).setOrientationLocked(false)
            )
        }
    }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.start()
    }
    LaunchedEffect(viewModel) { viewModel.start() }
    ArrivalContent(
        state = state,
        actions = ArrivalActions(
            onBack = navigation.onBack,
            onUseLocation = { locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
            onRetry = viewModel::start,
            onScan = { camera.launch(Manifest.permission.CAMERA) },
            onPostcard = navigation.onPostcard,
            onNext = navigation.onNext
        )
    )
}

@Composable
internal fun ArrivalContent(state: ArrivalUiState, actions: ArrivalActions, zone: ZoneId = ZoneId.systemDefault()) {
    when (state.phase) {
        ArrivalPhase.CHECKING -> LoadingState()
        ArrivalPhase.DONE -> CheckinOk(state, actions, zone)
        ArrivalPhase.NEEDS_PERMISSION, ArrivalPhase.NOT_YET -> NotYet(state, actions)
    }
}

/** Sin permiso o fuera de radio: usar la ubicación, escanear el código del mostrador o reintentar. */
@Composable
private fun NotYet(state: ArrivalUiState, actions: ArrivalActions) {
    val needsPermission = state.phase == ArrivalPhase.NEEDS_PERMISSION
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(if (needsPermission) R.string.arrival_use_location else R.string.arrival_scan),
                onClick = if (needsPermission) actions.onUseLocation else actions.onScan,
                icon = if (needsPermission) Icons.Outlined.LocationOn else WhereverIcons.QrScan,
                modifier = Modifier.fillMaxWidth(),
                tag = if (needsPermission) "arrival_use_location" else "arrival_scan"
            )
            WhereverOutlinedButton(
                text = stringResource(if (needsPermission) R.string.arrival_scan else R.string.arrival_retry),
                onClick = if (needsPermission) actions.onScan else actions.onRetry,
                modifier = Modifier.fillMaxWidth(),
                tag = if (needsPermission) "arrival_scan_alt" else "arrival_retry"
            )
        }
    ) {
        BackCircleButton(onClick = actions.onBack)
        Text(
            stringResource(R.string.arrival_title, state.placeName),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (needsPermission) {
            InfoBanner(
                stringResource(R.string.arrival_permission),
                icon = Icons.Outlined.LocationOn,
                tag = "arrival_permission"
            )
        }
        state.message?.let { ErrorBanner(it.asString()) }
    }
}

/** 26_CheckinOk: «¡Ya estás en…!», beneficios, Tomar una postal e Ir a la siguiente parada. */
@Composable
private fun CheckinOk(state: ArrivalUiState, actions: ArrivalActions, zone: ZoneId) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.arrival_postcard),
                onClick = actions.onPostcard,
                modifier = Modifier.fillMaxWidth(),
                tag = "arrival_postcard"
            )
            WhereverOutlinedButton(
                text = stringResource(
                    if (state.nextName !=
                        null
                    ) {
                        R.string.arrival_next
                    } else {
                        R.string.arrival_back_plan
                    }
                ),
                onClick = actions.onNext,
                modifier = Modifier.fillMaxWidth(),
                tag = "arrival_next"
            )
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.XXXL).testTag("arrival_ok")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(Sizes.BadgeL + Spacing.L).background(MintTint, CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Mint,
                    modifier = Modifier.size(Sizes.IconXXL)
                )
            }
            Text(
                stringResource(R.string.arrival_done_title, state.placeName),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Text(
                state.checkin?.let { stringResource(R.string.arrival_done_at, formatTime(it.createdAt, zone)) }
                    ?: stringResource(R.string.arrival_done_today),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        SurfaceCard(modifier = Modifier.testTag("arrival_benefits")) {
            Benefit(
                ImageVector.vectorResource(R.drawable.ic_wh_shield_check),
                stringResource(R.string.arrival_review_verified)
            )
            state.nextName?.let { next ->
                HorizontalDivider(Modifier.padding(horizontal = Spacing.L), color = MaterialTheme.colorScheme.outline)
                Benefit(
                    Icons.Outlined.Place,
                    state.nextMinutes?.let { stringResource(R.string.arrival_next_line_minutes, next, it) }
                        ?: stringResource(R.string.arrival_next_line, next)
                )
            }
        }
    }
}

@Composable
private fun Benefit(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.TouchTarget).padding(horizontal = Spacing.L)
    ) {
        Icon(icon, contentDescription = null, tint = RouteBlue, modifier = Modifier.size(Sizes.IconM))
        Spacer(Modifier.width(Spacing.M))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

internal fun sampleArrivalState() = ArrivalUiState(
    phase = ArrivalPhase.DONE,
    placeName = "Fonda Doña Meche",
    nextName = "Café Raíz",
    nextMinutes = 8,
    checkin = Checkin("c1", "n3", true, null, "2026-10-18T23:02:00Z")
)

@Preview(name = "26_CheckinOk claro")
@Preview(name = "26_CheckinOk oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ArrivalOkPreview() {
    WhereverTheme { ArrivalContent(sampleArrivalState(), ArrivalActions(), ZoneId.of("America/Mexico_City")) }
}

@Preview(name = "Llegada sin permiso")
@Composable
internal fun ArrivalPermissionPreview() {
    WhereverTheme {
        ArrivalContent(
            sampleArrivalState().copy(phase = ArrivalPhase.NEEDS_PERMISSION, checkin = null),
            ArrivalActions(),
            ZoneId.of("America/Mexico_City")
        )
    }
}
