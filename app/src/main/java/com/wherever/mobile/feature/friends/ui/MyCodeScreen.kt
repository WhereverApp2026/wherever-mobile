package com.wherever.mobile.feature.friends.ui

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.QrCode
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverIcons
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.profile.ui.ProfileAvatar

/** 25_MiCodigo (US-234): pestañas Mi código y Escanear. */
@Composable
fun MyCodeRoute(onBack: () -> Unit, onPerson: (String) -> Unit, viewModel: MyCodeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareText = state.code?.let { stringResource(R.string.friends_share_text, it) }
    val scanPrompt = stringResource(R.string.my_code_scan_prompt)
    val scanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        viewModel.onScanned(result.contents)
    }
    val scanOptions = {
        ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt(scanPrompt)
            .setBeepEnabled(false)
            .setOrientationLocked(false)
    }
    // El permiso de cámara se pide aquí, en contexto: si se niega, la app lo explica en vez del
    // diálogo genérico del lector ("la cámara ha encontrado un problema").
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scanner.launch(scanOptions()) else viewModel.onCameraDenied()
    }
    LaunchedEffect(state.shareUri) {
        val uri = state.shareUri ?: return@LaunchedEffect
        val send = Intent(Intent.ACTION_SEND)
            .setType("image/png")
            .putExtra(Intent.EXTRA_STREAM, uri.toUri())
            .putExtra(Intent.EXTRA_TEXT, shareText)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        send.clipData = ClipData.newRawUri(null, uri.toUri())
        context.startActivity(Intent.createChooser(send, null))
        viewModel.shareHandled()
    }
    LaunchedEffect(state.foundPersonId) {
        val id = state.foundPersonId ?: return@LaunchedEffect
        onPerson(id)
        viewModel.personOpened()
    }
    MyCodeContent(
        state = state,
        actions = MyCodeActions(
            onBack = onBack,
            onTab = viewModel::selectTab,
            onRetry = viewModel::refresh,
            onShare = viewModel::share,
            onSave = viewModel::save,
            onScan = {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                if (granted) scanner.launch(scanOptions()) else cameraPermission.launch(Manifest.permission.CAMERA)
            }
        )
    )
}

data class MyCodeActions(
    val onBack: () -> Unit = {},
    val onTab: (MyCodeTab) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onScan: () -> Unit = {}
)

@Composable
fun MyCodeContent(state: MyCodeUiState, actions: MyCodeActions) {
    val showCode = state.tab == MyCodeTab.MY_CODE
    FormScreen(
        footer = {
            when {
                !showCode -> WhereverPrimaryButton(
                    text = stringResource(R.string.my_code_scan_action),
                    onClick = actions.onScan,
                    loading = state.looking,
                    icon = WhereverIcons.QrScan,
                    tag = "my_code_scan"
                )
                state.code != null -> {
                    WhereverPrimaryButton(
                        text = stringResource(R.string.my_code_share),
                        onClick = actions.onShare,
                        loading = state.sharing,
                        icon = Icons.Outlined.Share,
                        tag = "my_code_share"
                    )
                    WhereverOutlinedButton(
                        text = stringResource(R.string.my_code_save),
                        onClick = actions.onSave,
                        enabled = !state.saving,
                        icon = WhereverIcons.Download,
                        modifier = Modifier.fillMaxWidth(),
                        tag = "my_code_save"
                    )
                }
            }
        }
    ) {
        TopTitleRow(stringResource(R.string.my_code_title), actions.onBack)
        SegmentedControl(
            options = listOf(stringResource(R.string.my_code_title), stringResource(R.string.my_code_scan_tab)),
            selectedIndex = state.tab.ordinal,
            onSelect = { actions.onTab(MyCodeTab.entries[it]) },
            modifier = Modifier.testTag("my_code_tabs")
        )
        state.message?.let { InfoBanner(it.asString(), icon = Icons.Outlined.Check, tag = "my_code_message") }
        if (showCode) MyCodeTabContent(state, actions) else ScanTabContent(state)
    }
}

@Composable
private fun MyCodeTabContent(state: MyCodeUiState, actions: MyCodeActions) {
    when {
        state.loading -> LoadingState(Modifier.padding(vertical = Spacing.Huge))
        state.loadError != null -> ErrorState(state.loadError.asString(), actions.onRetry)
        state.empty -> ErrorState(stringResource(R.string.my_code_empty), actions.onRetry)
        else -> SurfaceCard(modifier = Modifier.testTag("my_code_card")) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.M),
                modifier = Modifier.fillMaxWidth().padding(Spacing.XXL)
            ) {
                ProfileAvatar(state.name, state.photoUrl, size = Sizes.AvatarM)
                Text(state.name, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                state.username?.let {
                    Text(
                        stringResource(R.string.friends_username_at, it),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                state.payload?.let { payload ->
                    QrCode(
                        content = payload,
                        description = stringResource(R.string.my_code_qr_a11y, state.code.orEmpty()),
                        modifier = Modifier.widthIn(max = Sizes.QrCode).fillMaxWidth().testTag("my_code_qr")
                    )
                }
                Text(
                    stringResource(R.string.my_code_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ScanTabContent(state: MyCodeUiState) {
    state.scanError?.let { ErrorBanner(it.asString(), Modifier.testTag("my_code_scan_error")) }
    SurfaceCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.fillMaxWidth().padding(Spacing.XXL)
        ) {
            Icon(
                WhereverIcons.QrScan,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Sizes.IconXXL)
            )
            Text(
                stringResource(R.string.my_code_scan_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.my_code_scan_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MyCodePreview() {
    WhereverTheme {
        MyCodeContent(
            MyCodeUiState(loading = false, name = "Osmar Reséndiz", username = "osmar", code = "K7M2P9QX"),
            MyCodeActions()
        )
    }
}

@Preview
@Composable
internal fun MyCodeScanPreview() {
    WhereverTheme {
        MyCodeContent(MyCodeUiState(tab = MyCodeTab.SCAN, loading = false, code = "K7M2P9QX"), MyCodeActions())
    }
}
