package com.wherever.mobile.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.WhereverIconButton

// Degradado de la portada en Figma: tinta 55 % → 5 % (a la mitad) → 60 %.
private const val SCRIM_TOP = 0.55f
private const val SCRIM_MIDDLE = 0.05f
private const val SCRIM_BOTTOM = 0.6f
private const val GLASS_ALPHA = 0.22f
private const val MAX_BADGE = 99

/**
 * Portada de 10_Inicio: foto con degradado y esquinas de abajo redondeadas, ciudad, campana, saludo y el
 * buscador con el botón naranja de IA. La foto es la del primer lugar recomendado; sin ella queda el degradado
 * azul de la marca.
 */
@Composable
internal fun HomeCover(
    state: HomeUiState,
    unread: Int,
    onSearch: () -> Unit,
    onAiPlan: () -> Unit,
    onNotifications: () -> Unit
) {
    val photo = state.recommendations.firstOrNull { it.coverUrl != null }?.coverUrl
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.HomeCover)
            .clip(RoundedCornerShape(bottomStart = Radius.XXXL, bottomEnd = Radius.XXXL))
            .background(Brush.verticalGradient(listOf(Ink, RouteBlue)))
            .testTag("home_cover")
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    listOf(Ink.copy(alpha = SCRIM_TOP), Ink.copy(alpha = SCRIM_MIDDLE), Ink.copy(alpha = SCRIM_BOTTOM))
                )
            )
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.statusBarsPadding().padding(ScreenPadding)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                state.city?.let { CityChip(it) }
                Spacer(Modifier.weight(1f))
                NotificationBell(unread, onNotifications)
            }
            Text(
                text = state.firstName?.let { stringResource(R.string.home_greeting_name, it) }
                    ?: stringResource(R.string.home_greeting),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.testTag("home_greeting")
            )
            if (state.weather?.adverse == true) {
                Text(
                    stringResource(R.string.home_weather_adverse),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            SearchBox(onSearch, onAiPlan)
        }
    }
}

@Composable
private fun CityChip(city: String) {
    Surface(shape = CircleShape, color = Color.White.copy(alpha = GLASS_ALPHA), contentColor = Color.White) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
            modifier = Modifier.padding(horizontal = Spacing.M, vertical = Spacing.S).testTag("home_city")
        ) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(Sizes.IconM))
            Text(
                city,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** "Tacos, relax, aventura…" abre Explorar con el teclado listo; la chispa naranja arma un plan con IA. */
@Composable
private fun SearchBox(onSearch: () -> Unit, onAiPlan: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.RowMinHeight)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = Spacing.L, end = Spacing.XS)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Sizes.TouchTarget)
                    .clickable(onClick = onSearch)
                    .semantics { role = Role.Button }
                    .testTag("home_search")
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(Spacing.M))
                Text(
                    stringResource(R.string.home_search_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                onClick = onAiPlan,
                shape = CircleShape,
                color = Sunset,
                contentColor = Ink,
                modifier = Modifier.size(Sizes.SearchAction).testTag("home_ai_plan")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painterResource(R.drawable.ic_wh_sparkle),
                        contentDescription = stringResource(R.string.create_ai_plan),
                        modifier = Modifier.size(Sizes.IconL)
                    )
                }
            }
        }
    }
}

/** Campana de avisos en círculo translúcido, con el número de no leídos (specs/nv-52). */
@Composable
private fun NotificationBell(unread: Int, onClick: () -> Unit) {
    val description = if (unread > 0) {
        pluralStringResource(R.plurals.home_notifications_unread, unread, unread)
    } else {
        stringResource(R.string.notifications_title)
    }
    Surface(shape = CircleShape, color = Color.White.copy(alpha = GLASS_ALPHA)) {
        WhereverIconButton(onClick = onClick, modifier = Modifier.testTag("home_notifications")) {
            BadgedBox(
                badge = {
                    if (unread > 0) {
                        Badge(containerColor = Sunset, contentColor = Color.White) {
                            Text(if (unread > MAX_BADGE) "$MAX_BADGE+" else unread.toString())
                        }
                    }
                }
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = description, tint = Color.White)
            }
        }
    }
}
