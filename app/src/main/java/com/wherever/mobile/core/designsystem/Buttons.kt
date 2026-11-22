package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Strokes

private val ButtonHeight = Sizes.ButtonHeight

/** El botón naranja: uno solo por pantalla (constitución §4). */
@Composable
fun WhereverPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    tag: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Sunset,
            contentColor = Ink,
            disabledContainerColor = Sunset.copy(alpha = 0.45f),
            disabledContentColor = Ink.copy(alpha = 0.7f)
        ),
        modifier = modifier.fillMaxWidth().height(ButtonHeight).testTag(tag)
    ) {
        LoadingSwap(loading) { showSpinner ->
            if (showSpinner) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Sizes.ProgressSmall),
                    color = Ink,
                    strokeWidth = Strokes.Thin
                )
            } else {
                ButtonContent(text, icon)
            }
        }
    }
}

@Composable
fun WhereverOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null,
    tag: String = ""
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        border = BorderStroke(Strokes.Hairline, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        modifier = modifier.height(ButtonHeight).testTag(tag)
    ) {
        ButtonContent(text, icon)
    }
}

/** Botón secundario "Continuar con Google" (03_Login y 05_Registro): blanco, borde suave y la "G" a color. */
@Composable
fun WhereverGoogleButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, tag: String = "") {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(Strokes.Hairline, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.fillMaxWidth().height(ButtonHeight).testTag(tag)
    ) {
        Image(painterResource(R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(Sizes.IconM))
        Spacer(Modifier.width(Spacing.M))
        Text(text = text, style = ButtonText)
    }
}

@Composable
fun WhereverDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    tag: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Danger, contentColor = Color.White),
        modifier = modifier.fillMaxWidth().height(ButtonHeight).testTag(tag)
    ) {
        LoadingSwap(loading) { showSpinner ->
            if (showSpinner) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Sizes.ProgressSmall),
                    color = Color.White,
                    strokeWidth = Strokes.Thin
                )
            } else {
                ButtonContent(text, null)
            }
        }
    }
}

@Composable
fun WhereverTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = color),
        // Sin relleno lateral: el texto queda alineado con el margen de su columna (nv-55).
        contentPadding = PaddingValues(horizontal = Spacing.None, vertical = Spacing.S),
        modifier = modifier.height(Sizes.TouchTarget)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BackCircleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedIconButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(Strokes.Hairline, MaterialTheme.colorScheme.outline),
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.size(Sizes.TouchTarget)
    ) {
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = stringResource(R.string.common_back))
    }
}

/** Ícono en círculo blanco con borde, como el engrane y la campana de 81_Perfil. */
@Composable
fun CircleIconButton(icon: ImageVector, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedIconButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(Strokes.Hairline, MaterialTheme.colorScheme.outline),
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.size(Sizes.TouchTarget)
    ) {
        Icon(icon, contentDescription = description)
    }
}

@Composable
private fun ButtonContent(text: String, icon: ImageVector?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(Sizes.IconM))
            Spacer(Modifier.width(Spacing.S))
        }
        Text(text = text, style = ButtonText)
    }
}
