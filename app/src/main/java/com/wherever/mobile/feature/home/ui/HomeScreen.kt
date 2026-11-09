package com.wherever.mobile.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.MainTab
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.WhereverBottomBar
import com.wherever.mobile.core.designsystem.WhereverTheme

/** Inicio provisional: encabezado de 10_Inicio sin contenido. El feed necesita endpoints (specs/013). */
@Composable
fun HomeRouteScreen(onProfile: () -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    HomeContent(firstName = profile?.name?.substringBefore(' '), city = profile?.city?.name, onProfile = onProfile)
}

@Composable
private fun HomeContent(firstName: String?, city: String?, onProfile: () -> Unit) {
    Scaffold(
        bottomBar = { WhereverBottomBar(MainTab.HOME, onHome = {}, onProfile = onProfile) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Ink, RouteBlue)))
                    .statusBarsPadding()
                    .padding(ScreenPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (city != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White)
                            Text(text = city, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Text(
                        text = if (firstName != null) {
                            stringResource(R.string.home_greeting_name, firstName)
                        } else {
                            stringResource(R.string.home_greeting)
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        modifier = Modifier.testTag("home_greeting")
                    )
                }
            }
            InfoBanner(
                stringResource(R.string.home_placeholder),
                modifier = Modifier.padding(horizontal = ScreenPadding)
            )
        }
    }
}

@Preview
@Composable
private fun HomePreview() {
    WhereverTheme(darkTheme = false) { HomeContent("Ana", "Guadalajara", {}) }
}
