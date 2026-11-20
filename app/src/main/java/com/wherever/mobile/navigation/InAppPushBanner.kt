package com.wherever.mobile.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Elevations
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.WhereverShapes
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.notifications.InAppPush

/** 88_PushBanner: aviso arriba de la pantalla actual con Ver y Más tarde. */
@Composable
fun InAppPushBanner(push: InAppPush?, onOpen: () -> Unit, onLater: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = push != null,
        enter = slideInVertically { -it },
        exit = slideOutVertically { -it },
        modifier = modifier
    ) {
        val shown = push ?: return@AnimatedVisibility
        Surface(
            shape = WhereverShapes.Card,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = Elevations.Overlay,
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = ScreenPadding, vertical = Spacing.S)
                .fillMaxWidth()
                .testTag("in_app_push")
        ) {
            Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        shown.title ?: stringResource(R.string.notifications_default_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = Spacing.S)
                    )
                }
                Text(
                    shown.body ?: stringResource(R.string.notifications_default_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    WhereverTextLink(stringResource(R.string.in_app_push_later), onLater)
                    WhereverTextLink(stringResource(R.string.in_app_push_open), onOpen)
                }
            }
        }
    }
}
