package com.wherever.mobile.feature.auth.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatCountdown
import com.wherever.mobile.feature.auth.domain.EmailLink

/** 04_RevisaCorreo */
@Composable
fun CheckEmailRoute(onBack: () -> Unit, onLink: (EmailLink) -> Unit, viewModel: CheckEmailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) { viewModel.links.collect(onLink) }
    CheckEmailContent(
        state,
        onResend = viewModel::resend,
        onOpenMail = { openMailApp(context) },
        onBack = onBack,
        onLinkChange = viewModel::onLinkChange,
        onOpenLink = viewModel::openPastedLink
    )
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
    onBack: () -> Unit,
    onLinkChange: (String) -> Unit = {},
    onOpenLink: () -> Unit = {}
) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.check_email_open),
                onClick = onOpenMail,
                icon = Icons.Outlined.Email,
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
        Spacer(Modifier.height(Spacing.L))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.M)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Sizes.AvatarL)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Icon(
                    Icons.Outlined.Email,
                    contentDescription = null,
                    tint = Mint,
                    modifier = Modifier.size(Sizes.IconXXL)
                )
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
                modifier = Modifier.padding(Spacing.L)
            )
        }
        PastedLinkField(state, onLinkChange, onOpenLink)
        if (state.resent) InfoBanner(stringResource(R.string.check_email_resent))
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

/** Si el enlace abre en el navegador (o en otro teléfono), se pega aquí (nv-08, D11). */
@Composable
private fun PastedLinkField(state: CheckEmailUiState, onLinkChange: (String) -> Unit, onOpenLink: () -> Unit) {
    LabeledTextField(
        label = stringResource(R.string.check_email_link_label),
        value = state.pastedLink,
        onValueChange = onLinkChange,
        error = state.linkError?.asString(),
        supporting = stringResource(R.string.check_email_link_help),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = { onOpenLink() }),
        tag = "check_email_link"
    )
    if (state.pastedLink.isNotBlank()) {
        WhereverOutlinedButton(
            text = stringResource(R.string.check_email_link_open),
            onClick = onOpenLink,
            modifier = Modifier.fillMaxWidth(),
            tag = "check_email_link_open"
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CheckEmailPreview() {
    WhereverTheme {
        CheckEmailContent(CheckEmailUiState(CheckEmailMode.RECOVERY, "tu•••@ejemplo.com", secondsLeft = 45), {}, {}, {})
    }
}
