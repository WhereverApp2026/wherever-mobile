package com.wherever.mobile.feature.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.ToggleRow
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.feature.profile.domain.PrivacyPatch

/** 28_PrivSocial con los interruptores que sí existen en la API (duda D04). */
@Composable
fun PrivacyRoute(onBack: () -> Unit, viewModel: PrivacyViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val privacy = state.privacy
    if (privacy == null) {
        LoadingState()
        return
    }
    val enabled = !state.saving
    FormScreen {
        TopTitleRow(stringResource(R.string.privacy_title), onBack)
        state.error?.let { ErrorBanner(it.asString()) }
        SurfaceCard {
            ToggleRow(
                title = stringResource(R.string.privacy_location),
                subtitle = stringResource(R.string.privacy_location_detail),
                checked = privacy.location,
                onCheckedChange = { viewModel.update(PrivacyPatch(location = it)) },
                enabled = enabled,
                tag = "privacy_location"
            )
            ToggleRow(
                title = stringResource(R.string.privacy_learning),
                subtitle = stringResource(R.string.privacy_learning_detail),
                checked = privacy.personalizedLearning,
                onCheckedChange = { viewModel.update(PrivacyPatch(personalizedLearning = it)) },
                enabled = enabled
            )
            ToggleRow(
                title = stringResource(R.string.privacy_mood),
                subtitle = stringResource(R.string.privacy_mood_detail),
                checked = privacy.mood,
                onCheckedChange = { viewModel.update(PrivacyPatch(mood = it)) },
                enabled = enabled
            )
            ToggleRow(
                title = stringResource(R.string.privacy_usage),
                subtitle = stringResource(R.string.privacy_usage_detail),
                checked = privacy.usageData,
                onCheckedChange = { viewModel.update(PrivacyPatch(usageData = it)) },
                enabled = enabled
            )
            ToggleRow(
                title = stringResource(R.string.privacy_searchable),
                subtitle = stringResource(R.string.privacy_searchable_detail),
                checked = privacy.searchableByPhone,
                onCheckedChange = { viewModel.update(PrivacyPatch(searchableByPhone = it)) },
                enabled = enabled,
                tag = "privacy_searchable"
            )
            ToggleRow(
                title = stringResource(R.string.privacy_sensitive),
                subtitle = stringResource(R.string.privacy_sensitive_detail),
                checked = privacy.sensitiveData,
                onCheckedChange = viewModel::onSensitiveDataChange,
                enabled = enabled,
                tag = "privacy_sensitive"
            )
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
