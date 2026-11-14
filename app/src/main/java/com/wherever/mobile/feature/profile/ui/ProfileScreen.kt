package com.wherever.mobile.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.CircleIconButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.GoldTint
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.InitialAvatar
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.MainTab
import com.wherever.mobile.core.designsystem.PhaseCrossfade
import com.wherever.mobile.core.designsystem.ScreenPhase
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverBottomBar
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.profile.domain.NotificationSettings
import com.wherever.mobile.feature.profile.domain.Preferences
import com.wherever.mobile.feature.profile.domain.Privacy
import com.wherever.mobile.feature.profile.domain.UserProfile
import com.wherever.mobile.feature.tastes.ui.tasteLabelRes

/** 81_Perfil */
@Composable
fun ProfileRoute(
    onHome: () -> Unit,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    onVerifyEmail: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        bottomBar = { WhereverBottomBar(MainTab.PROFILE, onHome = onHome, onProfile = {}) }
    ) { padding ->
        val profile = state.profile
        val modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        val phase = when {
            profile != null -> ScreenPhase.CONTENT
            state.loading -> ScreenPhase.LOADING
            else -> ScreenPhase.ERROR
        }
        PhaseCrossfade(phase) { shown ->
            val current = state.profile
            when {
                shown == ScreenPhase.CONTENT && current != null -> ProfileContent(
                    profile = current,
                    errorBanner = { state.error?.let { ErrorBanner(it.asString()) } },
                    onEdit = onEdit,
                    onSettings = onSettings,
                    onVerifyEmail = { onVerifyEmail(current.email) },
                    modifier = modifier
                )
                shown == ScreenPhase.ERROR -> ErrorState(
                    message = state.error?.asString().orEmpty(),
                    onRetry = viewModel::refresh,
                    modifier = modifier
                )
                else -> LoadingState(modifier)
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    errorBanner: @Composable () -> Unit,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    onVerifyEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    FormScreen(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            CircleIconButton(Icons.Filled.Settings, stringResource(R.string.profile_settings), onSettings)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            InitialAvatar(profile.name, size = 88.dp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("profile_name")
                )
                Text(
                    text = profile.city?.name ?: profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        WhereverOutlinedButton(
            text = stringResource(R.string.profile_edit),
            onClick = onEdit,
            icon = Icons.Filled.Edit,
            modifier = Modifier.fillMaxWidth(),
            tag = "profile_edit"
        )
        if (!profile.emailVerified) {
            InfoBanner(stringResource(R.string.profile_verify_notice), container = GoldTint, contentColor = Ink)
            WhereverTextLink(stringResource(R.string.profile_verify_action), onVerifyEmail)
        }
        errorBanner()
        Stats(profile)
        profile.tastes?.let { tastes ->
            Text(text = stringResource(R.string.profile_tastes), style = MaterialTheme.typography.titleMedium)
            ChipGroup {
                tastes.categories.forEach { key -> StatusPill(tasteLabel(key)) }
            }
        }
    }
}

@Composable
private fun Stats(profile: UserProfile) {
    SurfaceCard {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Stat(profile.points.toString(), stringResource(R.string.profile_points))
            Stat(profile.level, stringResource(R.string.profile_level))
            Stat(profile.weekStreak.toString(), stringResource(R.string.profile_streak))
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * La etiqueta de Gustos. Si la API trae una clave desconocida (duda D05), se muestra legible:
 * pan_dulce -> Pan dulce.
 */
@Composable
private fun tasteLabel(key: String): String =
    tasteLabelRes(key)?.let { stringResource(it) } ?: key.replace('_', ' ').replaceFirstChar { it.uppercase() }

@Preview
@Composable
private fun ProfilePreview() {
    WhereverTheme(darkTheme = false) {
        ProfileContent(
            UserProfile(
                id = "1",
                name = "Ana López",
                email = "ana@ejemplo.com",
                emailVerified = false,
                mode = "explorar",
                city = null,
                tastes = null,
                preferences = Preferences(
                    "a_pie",
                    "sistema",
                    "km",
                    null,
                    null,
                    NotificationSettings(true, true, true, true, false)
                ),
                privacy = Privacy(true, true, false, true, false, false),
                points = 50,
                level = "Explorador",
                weekStreak = 0,
                deleteAt = null,
                version = 1
            ),
            {},
            {},
            {},
            {}
        )
    }
}
