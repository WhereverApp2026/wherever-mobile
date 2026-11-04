package com.wherever.mobile.feature.auth.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.components.WhereverPrimaryButton
import com.wherever.mobile.feature.auth.ui.components.AuthErrorBanner

private const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"

@Composable
fun PermissionsScreen(onComplete: () -> Unit, viewModel: PermissionsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationsSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    var locationGranted by remember {
        mutableStateOf(hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    var notificationsGranted by remember {
        mutableStateOf(
            !notificationsSupported || hasPermission(context, POST_NOTIFICATIONS_PERMISSION)
        )
    }
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> locationGranted = granted }
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> notificationsGranted = granted }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { onComplete() }
    }
    PermissionsContent(
        state = state,
        locationGranted = locationGranted,
        notificationsGranted = notificationsGranted,
        notificationsSupported = notificationsSupported,
        onRequestLocation = {
            locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        },
        onRequestNotifications = {
            notificationLauncher.launch(POST_NOTIFICATIONS_PERMISSION)
        },
        onFinish = viewModel::complete
    )
}

@Composable
private fun PermissionsContent(
    state: PermissionsUiState,
    locationGranted: Boolean,
    notificationsGranted: Boolean,
    notificationsSupported: Boolean,
    onRequestLocation: () -> Unit,
    onRequestNotifications: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OnboardingHeader(
            step = 4,
            title = stringResource(R.string.onboarding_permissions_title),
            subtitle = stringResource(R.string.onboarding_permissions_subtitle)
        )
        PermissionCards(
            locationGranted = locationGranted,
            notificationsGranted = notificationsGranted,
            notificationsSupported = notificationsSupported,
            onRequestLocation = onRequestLocation,
            onRequestNotifications = onRequestNotifications
        )
        AuthErrorBanner(error = state.error)
        WhereverPrimaryButton(
            text = if (state.isCompleting) {
                stringResource(R.string.auth_action_loading)
            } else {
                stringResource(R.string.onboarding_action_finish)
            },
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_permissions_finish"),
            enabled = !state.isCompleting
        )
    }
}

@Composable
private fun PermissionCards(
    locationGranted: Boolean,
    notificationsGranted: Boolean,
    notificationsSupported: Boolean,
    onRequestLocation: () -> Unit,
    onRequestNotifications: () -> Unit
) {
    PermissionCard(
        title = stringResource(R.string.onboarding_permissions_location_title),
        description = stringResource(R.string.onboarding_permissions_location_description),
        status = permissionStatus(locationGranted),
        action = if (locationGranted) null else onRequestLocation,
        testTag = "onboarding_permission_location"
    )
    PermissionCard(
        title = stringResource(R.string.onboarding_permissions_notifications_title),
        description = stringResource(R.string.onboarding_permissions_notifications_description),
        status = notificationStatus(notificationsSupported, notificationsGranted),
        action = if (notificationsGranted || !notificationsSupported) null else onRequestNotifications,
        testTag = "onboarding_permission_notifications"
    )
    PermissionCard(
        title = stringResource(R.string.onboarding_permissions_camera_title),
        description = stringResource(R.string.onboarding_permissions_camera_description),
        status = stringResource(R.string.onboarding_permissions_camera_optional),
        action = null,
        testTag = "onboarding_permission_camera"
    )
}

@Composable
private fun notificationStatus(supported: Boolean, granted: Boolean): String = stringResource(
    when {
        !supported -> R.string.onboarding_permissions_not_required
        granted -> R.string.onboarding_permissions_granted
        else -> R.string.onboarding_permissions_denied
    }
)

@Composable
private fun permissionStatus(granted: Boolean): String = stringResource(
    if (granted) {
        R.string.onboarding_permissions_granted
    } else {
        R.string.onboarding_permissions_denied
    }
)

@Composable
private fun PermissionCard(title: String, description: String, status: String, action: (() -> Unit)?, testTag: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (action == null) Mint else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (action != null) {
                OutlinedButton(onClick = action) {
                    Text(text = stringResource(R.string.onboarding_permissions_allow))
                }
            }
        }
    }
}
