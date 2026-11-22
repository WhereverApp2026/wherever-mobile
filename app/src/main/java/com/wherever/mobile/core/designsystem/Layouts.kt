package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Spacing

val ScreenPadding: Dp = Spacing.XL

/**
 * Pantalla con scroll y fondo Niebla. `footer` queda fijo abajo, como en la maqueta, salvo cuando
 * la altura disponible es baja (teléfono en horizontal o teclado abierto): entonces va dentro del
 * scroll, para que los campos y el error no queden tapados (specs/nv-49).
 */
@Composable
fun FormScreen(
    modifier: Modifier = Modifier,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
    ) {
        val compact = maxHeight < COMPACT_FORM_HEIGHT
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.L)
            ) {
                content()
                if (footer != null && compact) FooterColumn(footer, Modifier)
            }
            if (footer != null && !compact) {
                FooterColumn(footer, Modifier.padding(horizontal = ScreenPadding, vertical = Spacing.M))
            }
        }
    }
}

/** Debajo de esta altura el pie fijo dejaría sin espacio a los campos. */
val COMPACT_FORM_HEIGHT: Dp = 520.dp

@Composable
private fun FooterColumn(footer: @Composable ColumnScope.() -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.S),
        content = footer
    )
}

/**
 * Envía un formulario: primero cierra el teclado y quita el foco, para que el resultado (cargando,
 * error o éxito) quede a la vista (specs/nv-49).
 */
@Composable
fun rememberFormSubmit(onSubmit: () -> Unit): () -> Unit {
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    return remember(onSubmit, keyboard, focus) {
        {
            keyboard?.hide()
            focus.clearFocus()
            onSubmit()
        }
    }
}

@Composable
fun TitleBlock(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Encabezado con regresar y título a la derecha (Ajustes, Sesiones, Privacidad). */
@Composable
fun TopTitleRow(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BackCircleButton(onClick = onBack)
        Spacer(Modifier.width(Spacing.M))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/** Regresar y barra de pasos de los formularios de alta. */
@Composable
fun StepHeader(stepLabel: String, step: Int, total: Int, onBack: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            BackCircleButton(onClick = onBack)
            Spacer(Modifier.width(Spacing.M))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Text(
                text = stepLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                repeat(total) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(Strokes.Thick)
                            .clip(CircleShape)
                            .background(if (index < step) RouteBlue else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

/** Logo de Wherever (docs/logo/logo-nuevo.jpg): cuadro blanco redondeado con el pin, la ruta y el nombre. */
@Composable
fun LogoMark(modifier: Modifier = Modifier, size: Dp = Sizes.AvatarL, contentDescription: String? = null) {
    Image(
        painter = painterResource(R.drawable.logo_wherever),
        contentDescription = contentDescription,
        modifier = modifier.size(size)
    )
}
