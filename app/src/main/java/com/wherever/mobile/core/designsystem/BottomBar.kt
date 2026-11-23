package com.wherever.mobile.core.designsystem

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.wherever.mobile.R

enum class MainTab { HOME, EXPLORE, PLANS, PROFILE }

/**
 * Opciones de la hoja 15_Crear (docs/diseno_boceto2.md §4.3), en el orden de Figma.
 * Las que aún no tienen pantalla se muestran con "Próximamente" y no se pueden tocar.
 */
enum class CreateOption(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    @DrawableRes val icon: Int,
    val tint: Color,
    val background: Color,
    val available: Boolean
) {
    AI_PLAN(R.string.create_ai_plan, R.string.create_ai_plan_hint, R.drawable.ic_wh_sparkle, Sunset, SunsetTint, true),
    MANUAL_PLAN(
        R.string.create_manual_plan,
        R.string.create_manual_plan_hint,
        R.drawable.ic_wh_pencil,
        Grape,
        GrapeTint,
        true
    ),
    WITH_FRIENDS(
        R.string.create_with_friends,
        R.string.create_with_friends_hint,
        R.drawable.ic_wh_users,
        RouteBlue,
        RouteBlueTint,
        true
    ),
    MOMENT(R.string.create_moment, R.string.create_moment_hint, R.drawable.ic_wh_camera, Mint, MintTint, true),
    REVIEW(R.string.create_review, R.string.create_review_hint, R.drawable.ic_wh_shield_check, Ink, GoldTint, true)
}

/**
 * Barra inferior del Boceto 2 (10_Inicio): Inicio, Explorar, "+" naranja, Planes y Perfil.
 * El "+" abre la hoja 15_Crear; [onCreate] recibe la opción elegida (solo las disponibles).
 */
@Composable
fun WhereverBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onCreate: (CreateOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreate by rememberSaveable { mutableStateOf(false) }
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.navigationBarsPadding().testTag("bottom_bar")
    ) {
        BarItem(selected == MainTab.HOME, rememberVectorPainter(Icons.Outlined.Home), R.string.tab_home) {
            onSelect(MainTab.HOME)
        }
        BarItem(selected == MainTab.EXPLORE, painterResource(R.drawable.ic_wh_compass), R.string.tab_explore) {
            onSelect(MainTab.EXPLORE)
        }
        CreateBarButton(onClick = { showCreate = true })
        BarItem(selected == MainTab.PLANS, rememberVectorPainter(Icons.Outlined.DateRange), R.string.tab_plans) {
            onSelect(MainTab.PLANS)
        }
        BarItem(selected == MainTab.PROFILE, rememberVectorPainter(Icons.Outlined.Person), R.string.tab_profile) {
            onSelect(MainTab.PROFILE)
        }
    }
    if (showCreate) {
        CreateSheet(
            onDismiss = { showCreate = false },
            onSelect = {
                showCreate = false
                onCreate(it)
            }
        )
    }
}

@Composable
private fun RowScope.BarItem(selected: Boolean, icon: Painter, @StringRes label: Int, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(Sizes.IconL)) },
        label = { Text(stringResource(label)) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

/** Círculo naranja de 56 dp con anillo del color de la barra; sin texto, como en Figma. */
@Composable
private fun RowScope.CreateBarButton(onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).align(Alignment.CenterVertically)) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = Sunset,
            contentColor = Ink,
            border = BorderStroke(Strokes.Thick, MaterialTheme.colorScheme.surface),
            modifier = Modifier.size(Sizes.AvatarM).testTag("bottom_bar_create")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.tab_create),
                    modifier = Modifier.size(Sizes.IconXL)
                )
            }
        }
    }
}

/** Hoja 15_Crear: "¿Qué quieres hacer?" con cinco filas de 64 dp. */
@Composable
private fun CreateSheet(onDismiss: () -> Unit, onSelect: (CreateOption) -> Unit) {
    WhereverBottomSheet(onDismissRequest = onDismiss, tag = "create_sheet") {
        Text(
            text = stringResource(R.string.create_sheet_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        CreateOptionsList(onSelect)
    }
}

/** Las cinco filas de 15_Crear; aparte de la hoja para probarlas (la hoja se cuelga en pruebas en MIUI). */
@Composable
internal fun CreateOptionsList(onSelect: (CreateOption) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        CreateOption.entries.forEach { option -> CreateRow(option, onClick = { onSelect(option) }) }
    }
}

@Composable
private fun CreateRow(option: CreateOption, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.L),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.Thumbnail)
            .clickable(enabled = option.available, role = Role.Button, onClick = onClick)
            .testTag("create_option_${option.name.lowercase()}")
    ) {
        Surface(shape = WhereverShapes.Field, color = option.background, modifier = Modifier.size(Sizes.CreateTile)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(option.icon),
                    contentDescription = null,
                    tint = option.tint,
                    modifier = Modifier.size(Sizes.IconL)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(option.title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(if (option.available) option.subtitle else R.string.create_coming_soon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.XS)
            )
        }
        if (option.available) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
