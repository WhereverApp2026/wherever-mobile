package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/** Hoja inferior única de la app (14_MeApunto y las demás hojas de la maqueta): esquinas de 24 dp y margen lateral. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhereverBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String = "sheet",
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = WhereverShapes.Sheet,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag(tag)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
            modifier = Modifier.padding(
                horizontal = ScreenPadding
            ).padding(bottom = Spacing.XXL).navigationBarsPadding(),
            content = content
        )
    }
}
