package com.wherever.mobile.feature.settings.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.NavRow
import com.wherever.mobile.core.designsystem.PhaseCrossfade
import com.wherever.mobile.core.designsystem.ScreenPhase
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.ToggleRow
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.profile.domain.Privacy
import com.wherever.mobile.feature.profile.domain.PrivacyPatch

/** 28_PrivSocial con los interruptores que sí existen en la API (duda D04). */
@Composable
fun PrivacyRoute(onBack: () -> Unit, onMyData: () -> Unit, viewModel: PrivacyViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PhaseCrossfade(if (state.privacy == null) ScreenPhase.LOADING else ScreenPhase.CONTENT) { phase ->
        val privacy = state.privacy
        if (phase == ScreenPhase.LOADING || privacy == null) {
            LoadingState()
        } else {
            PrivacyContent(state, privacy, viewModel::update, viewModel::onSensitiveDataChange, onBack, onMyData)
        }
    }
    if (state.confirmSensitiveOff) {
        ConfirmDialog(
            title = stringResource(R.string.privacy_sensitive_off_title),
            message = stringResource(R.string.privacy_sensitive_off_message),
            confirmText = stringResource(R.string.privacy_sensitive_off_confirm),
            onConfirm = viewModel::confirmSensitiveOff,
            onDismiss = viewModel::dismissSensitiveOff
        )
    }
}

@Composable
private fun PrivacyContent(
    state: PrivacyUiState,
    privacy: Privacy,
    onUpdate: (PrivacyPatch) -> Unit,
    onSensitiveChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onMyData: () -> Unit
) {
    val enabled = !state.saving
    FormScreen {
        TopTitleRow(stringResource(R.string.privacy_title), onBack)
        state.error?.let { ErrorBanner(it.asString()) }
        SurfaceCard {
            ToggleRow(
                title = stringResource(R.string.privacy_location),
                subtitle = stringResource(R.string.privacy_location_detail),
                checked = privacy.location,
                onCheckedChange = { onUpdate(PrivacyPatch(location = it)) },
                enabled = enabled,
                tag = "privacy_location"
            )
            ToggleRow(
                title = stringResource(R.string.privacy_learning),
                subtitle = stringResource(R.string.privacy_learning_detail),
                checked = privacy.personalizedLearning,
                onCheckedChange = { onUpdate(PrivacyPatch(personalizedLearning = it)) },
                enabled = enabled
            )
            ToggleRow(
                title = stringResource(R.string.privacy_mood),
                subtitle = stringResource(R.string.privacy_mood_detail),
                checked = privacy.mood,
                onCheckedChange = { onUpdate(PrivacyPatch(mood = it)) },
                enabled = enabled
            )
            ToggleRow(
                title = stringResource(R.string.privacy_usage),
                subtitle = stringResource(R.string.privacy_usage_detail),
                checked = privacy.usageData,
                onCheckedChange = { onUpdate(PrivacyPatch(usageData = it)) },
                enabled = enabled
            )
            ToggleRow(
                title = stringResource(R.string.privacy_searchable),
                subtitle = stringResource(R.string.privacy_searchable_detail),
                checked = privacy.searchableByPhone,
                onCheckedChange = { onUpdate(PrivacyPatch(searchableByPhone = it)) },
                enabled = enabled,
                tag = "privacy_searchable"
            )
            ToggleRow(
                title = stringResource(R.string.privacy_searchable_user),
                subtitle = stringResource(R.string.privacy_searchable_user_detail),
                checked = privacy.searchableByUser,
                onCheckedChange = { onUpdate(PrivacyPatch(searchableByUser = it)) },
                enabled = enabled,
                tag = "privacy_searchable_user"
            )
            ToggleRow(
                title = stringResource(R.string.privacy_sensitive),
                subtitle = stringResource(R.string.privacy_sensitive_detail),
                checked = privacy.sensitiveData,
                onCheckedChange = onSensitiveChange,
                enabled = enabled,
                tag = "privacy_sensitive"
            )
        }
        SurfaceCard {
            NavRow(stringResource(R.string.privacy_my_data), onMyData, tag = "privacy_my_data")
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PrivacyPreview() {
    WhereverTheme {
        PrivacyContent(
            state = PrivacyUiState(privacy = PREVIEW_PROFILE.privacy),
            privacy = PREVIEW_PROFILE.privacy,
            onUpdate = {},
            onSensitiveChange = {},
            onBack = {},
            onMyData = {}
        )
    }
}
