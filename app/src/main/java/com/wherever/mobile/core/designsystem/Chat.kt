package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp

/**
 * Barra para escribir de 45_GrupoChat: campo píldora y botón azul de enviar (48 dp). El texto lo valida
 * el ViewModel; aquí solo se habilita el botón.
 */
@Composable
fun MessageComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String,
    sendDescription: String,
    canSend: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
            modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.S)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = CircleShape,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.weight(1f).testTag("chat_input")
            )
            FilledIconButton(
                onClick = onSend,
                enabled = canSend,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = RouteBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.size(Sizes.TouchTarget).testTag("chat_send")
            ) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = sendDescription)
            }
        }
    }
}

/** Avatares encimados (iniciales o foto) de 44_MisGrupos y la cabecera del chat. */
@Composable
fun AvatarStack(names: List<String>, size: Dp, modifier: Modifier = Modifier, max: Int = 3) {
    val shown = names.take(max)
    Box(modifier = modifier) {
        shown.forEachIndexed { index, name ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(x = size * index * OVERLAP)
                    .size(size)
                    .clip(CircleShape)
                    .background(avatarColor(index))
            ) {
                Text(
                    name.trim().take(1).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Box(Modifier.size(width = size + size * (shown.size - 1).coerceAtLeast(0) * OVERLAP, height = size))
    }
}

private const val OVERLAP = 0.7f

/** Colores de los avatares de la maqueta (verde, oro, tinta, azul, naranja). */
private fun avatarColor(index: Int): Color = listOf(Mint, Gold, Ink, RouteBlue, Sunset)[index % AVATAR_COLORS]

private const val AVATAR_COLORS = 5
