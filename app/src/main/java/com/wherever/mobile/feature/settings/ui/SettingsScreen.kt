package com.wherever.mobile.feature.settings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.ToggleRow
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverDialog
import com.wherever.mobile.core.designsystem.WhereverTextButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.profile.domain.CityRef
import com.wherever.mobile.feature.profile.domain.NotificationSettings
import com.wherever.mobile.feature.profile.domain.Preferences
import com.wherever.mobile.feature.profile.domain.Privacy
import com.wherever.mobile.feature.profile.domain.QuietHours
import com.wherever.mobile.feature.profile.domain.Tastes
import com.wherever.mobile.feature.profile.domain.UserProfile

private val THEMES = listOf("claro", "oscuro", "sistema")

data class SettingsNavigation(
    val onBack: () -> Unit,
    val onPersonalData: () -> Unit,
    val onTastes: () -> Unit,
    val onSessions: () -> Unit,
    val onChangeEmail: () -> Unit,
    val onChangePassword: () -> Unit,
    val onPrivacy: () -> Unit,
    val onLoggedOut: () -> Unit
)

/** 85_Ajustes */
@Composable
fun SettingsRoute(navigation: SettingsNavigation, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.actions() }
    var confirmLogout by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel) { viewModel.loggedOut.collect { navigation.onLoggedOut() } }
    val loading = state.profile == null || state.loggingOut
    PhaseCrossfade(if (loading) ScreenPhase.LOADING else ScreenPhase.CONTENT) { phase ->
        val profile = state.profile
        if (phase == ScreenPhase.LOADING || profile == null) {
            LoadingState()
        } else {
            SettingsContent(state, profile, actions, navigation, onLogout = { confirmLogout = true })
        }
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

/** Cambios de 85_Ajustes; la pantalla solo los envía (sin lógica en el Composable). */
internal data class SettingsActions(
    val onTheme: (String) -> Unit,
    val onGroups: (Boolean) -> Unit,
    val onCoupons: (Boolean) -> Unit,
    val onReviews: (Boolean) -> Unit,
    val onPromotions: (Boolean) -> Unit,
    val onQuietHours: (QuietHours) -> Unit,
    val onUseLocation: (Boolean) -> Unit
)

private fun SettingsViewModel.actions() = SettingsActions(
    onTheme = ::setTheme,
    onGroups = ::setGroups,
    onCoupons = ::setCoupons,
    onReviews = ::setReviews,
    onPromotions = ::setPromotions,
    onQuietHours = ::setQuietHours,
    onUseLocation = ::setUseLocation
)

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    profile: UserProfile,
    actions: SettingsActions,
    navigation: SettingsNavigation,
    onLogout: () -> Unit
) {
    FormScreen {
        TopTitleRow(stringResource(R.string.settings_title), navigation.onBack)
        state.error?.let { ErrorBanner(it.asString()) }
        SectionLabel(stringResource(R.string.settings_section_account))
        SurfaceCard {
            NavRow(stringResource(R.string.settings_personal_data), navigation.onPersonalData, value = profile.name)
            NavRow(
                stringResource(R.string.settings_email),
                navigation.onChangeEmail,
                value = profile.email,
                tag = "settings_email"
            )
            NavRow(stringResource(R.string.settings_password), navigation.onChangePassword, tag = "settings_password")
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
            Toggles(profile, state.saving, actions)
            QuietHoursRow(profile, enabled = !state.saving, onSelect = actions.onQuietHours)
        }
        SectionLabel(stringResource(R.string.settings_section_theme))
        SegmentedControl(
            options = listOf(
                stringResource(R.string.settings_theme_light),
                stringResource(R.string.settings_theme_dark),
                stringResource(R.string.settings_theme_system)
            ),
            selectedIndex = THEMES.indexOf(profile.preferences.theme).coerceAtLeast(0),
            onSelect = { actions.onTheme(THEMES[it]) }
        )
        SectionLabel(stringResource(R.string.settings_section_privacy))
        SurfaceCard {
            NavRow(stringResource(R.string.privacy_title), navigation.onPrivacy, tag = "settings_privacy")
            ToggleRow(
                title = stringResource(R.string.settings_use_location),
                checked = profile.privacy.location,
                onCheckedChange = actions.onUseLocation,
                enabled = !state.saving
            )
        }
        Box(modifier = Modifier.fillMaxWidth().padding(top = Spacing.S), contentAlignment = Alignment.Center) {
            // 85_Ajustes: "Cerrar sesión" va en gris, no en azul de enlace.
            WhereverTextLink(
                stringResource(R.string.settings_logout),
                onLogout,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** No hay interruptor de reservaciones: la API no permite apagarlas (duda D06). */
@Composable
private fun Toggles(profile: UserProfile, saving: Boolean, actions: SettingsActions) {
    val notifications = profile.preferences.notifications
    ToggleRow(
        title = stringResource(R.string.settings_notif_groups),
        checked = notifications.groups,
        onCheckedChange = actions.onGroups,
        enabled = !saving,
        tag = "settings_notif_groups"
    )
    ToggleRow(
        title = stringResource(R.string.settings_notif_coupons),
        checked = notifications.coupons,
        onCheckedChange = actions.onCoupons,
        enabled = !saving
    )
    ToggleRow(
        title = stringResource(R.string.settings_notif_reviews),
        checked = notifications.reviews,
        onCheckedChange = actions.onReviews,
        enabled = !saving
    )
    ToggleRow(
        title = stringResource(R.string.settings_notif_promotions),
        checked = notifications.promotions,
        onCheckedChange = actions.onPromotions,
        enabled = !saving
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Opciones de horario silencioso (US-148). La API recibe "HH:MM"; desde = hasta lo apaga. */
private val QUIET_PRESETS = listOf(
    QuietHours.OFF,
    QuietHours("22:00", "08:00"),
    QuietHours("23:00", "07:00"),
    QuietHours("00:00", "09:00")
)

@Composable
private fun quietLabel(hours: QuietHours?): String = if (hours == null || !hours.enabled) {
    stringResource(R.string.settings_quiet_off)
} else {
    stringResource(R.string.settings_quiet_range, hours.from, hours.to)
}

@Composable
private fun QuietHoursRow(profile: UserProfile, enabled: Boolean, onSelect: (QuietHours) -> Unit) {
    var choosing by remember { mutableStateOf(false) }
    val from = profile.preferences.quietFrom
    val to = profile.preferences.quietTo
    val current = if (from != null && to != null) QuietHours(from, to) else null
    NavRow(
        stringResource(R.string.settings_quiet_hours),
        { if (enabled) choosing = true },
        value = quietLabel(current),
        tag = "settings_quiet_hours"
    )
    if (choosing) {
        WhereverDialog(
            onDismissRequest = { choosing = false },
            title = { Text(stringResource(R.string.settings_quiet_hours)) },
            text = {
                Column {
                    Text(
                        stringResource(R.string.settings_quiet_detail),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    QUIET_PRESETS.forEach { preset ->
                        val selected = preset == current || (!preset.enabled && current?.enabled != true)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = selected, role = Role.RadioButton) {
                                    choosing = false
                                    if (!selected) onSelect(preset)
                                }
                                .padding(vertical = Spacing.S)
                                .testTag("quiet_${preset.from}_${preset.to}")
                        ) {
                            RadioButton(selected = selected, onClick = null)
                            Text(quietLabel(preset), modifier = Modifier.padding(start = Spacing.M))
                        }
                    }
                }
            },
            confirmButton = {
                WhereverTextButton(onClick = { choosing = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

/** Perfil de ejemplo para las vistas previas de Ajustes y Privacidad. */
internal val PREVIEW_PROFILE = UserProfile(
    id = "1",
    name = "Ana López",
    email = "ana@ejemplo.com",
    emailVerified = true,
    mode = "explorar",
    city = CityRef("1", "Guadalajara"),
    tastes = Tastes(
        categories = listOf("tacos", "cafe", "museos"),
        budgetMin = 100,
        budgetMax = 400,
        restrictions = emptyList(),
        allergies = ""
    ),
    preferences = Preferences(
        "a_pie",
        "sistema",
        "km",
        "22:00",
        "08:00",
        NotificationSettings(true, true, true, true, false)
    ),
    privacy = Privacy(true, true, false, true, false, false),
    points = 50,
    level = "Explorador",
    weekStreak = 0,
    deleteAt = null,
    version = 1
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun SettingsPreview() {
    WhereverTheme {
        SettingsContent(
            state = SettingsUiState(profile = PREVIEW_PROFILE),
            profile = PREVIEW_PROFILE,
            actions = SettingsActions({}, {}, {}, {}, {}, {}, {}),
            navigation = SettingsNavigation({}, {}, {}, {}, {}, {}, {}, {}),
            onLogout = {}
        )
    }
}
