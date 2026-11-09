package com.wherever.mobile.feature.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.NavRow
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.ToggleRow
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.feature.profile.domain.UserProfile

private val THEMES = listOf("claro", "oscuro", "sistema")

data class SettingsNavigation(
    val onBack: () -> Unit,
    val onPersonalData: () -> Unit,
    val onTastes: () -> Unit,
    val onSessions: () -> Unit,
    val onPrivacy: () -> Unit,
    val onLoggedOut: () -> Unit
)

/** 85_Ajustes */
@Composable
fun SettingsRoute(navigation: SettingsNavigation, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmLogout by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel) { viewModel.loggedOut.collect { navigation.onLoggedOut() } }
    val profile = state.profile
    if (profile == null || state.loggingOut) {
        LoadingState()
    } else {
        SettingsContent(state, profile, viewModel, navigation, onLogout = { confirmLogout = true })
    }
    if (confirmLogout) {
        ConfirmDialog(
            title = stringResource(R.string.settings_logout_title),
            message = stringResource(R.string.settings_logout_message),
            confirmText = stringResource(R.string.settings_logout),
            onConfirm = {
                confirmLogout = false
                viewModel.logout()
            },
            onDismiss = { confirmLogout = false }
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    profile: UserProfile,
    viewModel: SettingsViewModel,
    navigation: SettingsNavigation,
    onLogout: () -> Unit
) {
    FormScreen {
        TopTitleRow(stringResource(R.string.settings_title), navigation.onBack)
        state.error?.let { ErrorBanner(it.asString()) }
        SectionLabel(stringResource(R.string.settings_section_account))
        SurfaceCard {
            NavRow(stringResource(R.string.settings_personal_data), navigation.onPersonalData, value = profile.name)
            NavRow(stringResource(R.string.settings_sessions), navigation.onSessions, tag = "settings_sessions")
        }
        SectionLabel(stringResource(R.string.settings_section_preferences))
        SurfaceCard {
            NavRow(
                stringResource(R.string.settings_city),
                navigation.onPersonalData,
                value = profile.city?.name ?: stringResource(R.string.settings_no_city)
            )
            NavRow(
                stringResource(R.string.settings_tastes),
                navigation.onTastes,
                value = (profile.tastes?.categories?.size ?: 0).let {
                    pluralStringResource(R.plurals.settings_tastes_count, it, it)
                }
            )
            Toggles(profile, state.saving, viewModel)
        }
        SectionLabel(stringResource(R.string.settings_section_theme))
        SegmentedControl(
            options = listOf(
                stringResource(R.string.settings_theme_light),
                stringResource(R.string.settings_theme_dark),
                stringResource(R.string.settings_theme_system)
            ),
            selectedIndex = THEMES.indexOf(profile.preferences.theme).coerceAtLeast(0),
            onSelect = { viewModel.setTheme(THEMES[it]) }
        )
        SectionLabel(stringResource(R.string.settings_section_privacy))
        SurfaceCard {
            NavRow(stringResource(R.string.privacy_title), navigation.onPrivacy, tag = "settings_privacy")
            ToggleRow(
                title = stringResource(R.string.settings_use_location),
                checked = profile.privacy.location,
                onCheckedChange = viewModel::setUseLocation,
                enabled = !state.saving
            )
        }
        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
            WhereverTextLink(stringResource(R.string.settings_logout), onLogout)
        }
    }
}

/** No hay interruptor de reservaciones: la API no permite apagarlas (duda D06). */
@Composable
private fun Toggles(profile: UserProfile, saving: Boolean, viewModel: SettingsViewModel) {
    val notifications = profile.preferences.notifications
    ToggleRow(
        title = stringResource(R.string.settings_notif_groups),
        checked = notifications.groups,
        onCheckedChange = viewModel::setGroups,
        enabled = !saving,
        tag = "settings_notif_groups"
    )
    ToggleRow(
        title = stringResource(R.string.settings_notif_coupons),
        checked = notifications.coupons,
        onCheckedChange = viewModel::setCoupons,
        enabled = !saving
    )
    ToggleRow(
        title = stringResource(R.string.settings_notif_reviews),
        checked = notifications.reviews,
        onCheckedChange = viewModel::setReviews,
        enabled = !saving
    )
    ToggleRow(
        title = stringResource(R.string.settings_notif_promotions),
        checked = notifications.promotions,
        onCheckedChange = viewModel::setPromotions,
        enabled = !saving
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
