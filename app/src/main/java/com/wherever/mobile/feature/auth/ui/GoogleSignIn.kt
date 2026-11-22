package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.WhereverGoogleButton

/**
 * "o" + "Continuar con Google" del Boceto 2 (03_Login y 05_Registro).
 * La API aún no tiene inicio de sesión con Google: al tocarlo se avisa que llega pronto.
 */
@Composable
internal fun GoogleSignInBlock(tag: String, modifier: Modifier = Modifier) {
    var showSoon by rememberSaveable { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.auth_or),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        WhereverGoogleButton(
            text = stringResource(R.string.auth_google),
            onClick = { showSoon = true },
            tag = tag
        )
        if (showSoon) {
            InfoBanner(text = stringResource(R.string.auth_google_soon), tag = "${tag}_soon")
        }
    }
}
