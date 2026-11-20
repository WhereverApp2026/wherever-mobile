package com.wherever.mobile.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

/**
 * Pestañas con subrayado (10_Inicio y 11_InicioAmigos: "Cerca de ti" / "Amigos"). Para filtros en píldora se usa
 * [SegmentedControl].
 */
@Composable
fun UnderlineTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tag: String = "tabs"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().selectableGroup().testTag(tag)) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                val indicator by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "tab_indicator"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = Sizes.TouchTarget)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
                        .testTag("${tag}_$index")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.TouchTarget - Strokes.Thick)
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(Strokes.Thick).background(indicator))
                }
            }
        }
        HorizontalDivider(thickness = Strokes.Hairline, color = MaterialTheme.colorScheme.outline)
    }
}
