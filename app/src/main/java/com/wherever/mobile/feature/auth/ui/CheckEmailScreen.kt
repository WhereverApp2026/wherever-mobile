package com.wherever.mobile.feature.auth.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatCountdown

/** 04_RevisaCorreo */
@Composable
fun CheckEmailRoute(onBack: () -> Unit, viewModel: CheckEmailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    CheckEmailContent(state, onResend = viewModel::resend, onOpenMail = { openMailApp(context) }, onBack = onBack)
}

private fun openMailApp(context: Context) {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_EMAIL)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // Sin app de correo: la pantalla ya indica qué revisar.
    }
}

@Composable
private fun CheckEmailContent(
    state: CheckEmailUiState,
    onResend: () -> Unit,
    onOpenMail: () -> Unit,
    onBack: () -> Unit
) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.check_email_open),
                onClick = onOpenMail,
                icon = Icons.Filled.Email,
                tag = "check_email_open"
            )
            WhereverTextLink(
                text = if (state.canResend) {
                    stringResource(R.string.check_email_resend)
                } else {
                    stringResource(R.string.check_email_resend_in, formatCountdown(state.secondsLeft))
                },
                onClick = onResend,
                enabled = state.canResend
            )
            if (state.mode == CheckEmailMode.RECOVERY) {
                WhereverTextLink(stringResource(R.string.check_email_other), onBack)
            }
        }
    ) {
        BackCircleButton(onClick = onBack)
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Icon(Icons.Filled.Email, contentDescription = null, tint = Mint, modifier = Modifier.size(40.dp))
            }
            Text(text = stringResource(R.string.check_email_title), style = MaterialTheme.typography.headlineLarge)
            Text(
                text = stringResource(
                    if (state.mode == CheckEmailMode.RECOVERY) {
                        R.string.check_email_body_recovery
                    } else {
                        R.string.check_email_body_verification
                    }
                ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = state.maskedEmail,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("check_email_address")
            )
            Text(
                text = stringResource(
                    if (state.mode == CheckEmailMode.RECOVERY) {
                        R.string.check_email_expires_recovery
                    } else {
                        R.string.check_email_expires_verification
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SurfaceCard {
            Text(
                text = stringResource(R.string.check_email_help),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(14.dp)
            )
        }
        if (state.resent) InfoBanner(stringResource(R.string.check_email_resent))
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

@Preview
@Composable
private fun CheckEmailPreview() {
    WhereverTheme(darkTheme = false) {
        CheckEmailContent(CheckEmailUiState(CheckEmailMode.RECOVERY, "tu•••@ejemplo.com", secondsLeft = 45), {}, {}, {})
    }
}
