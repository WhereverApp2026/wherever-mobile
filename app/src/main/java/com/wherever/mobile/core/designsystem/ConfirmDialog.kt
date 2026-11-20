package com.wherever.mobile.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wherever.mobile.R

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    WhereverDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            WhereverTextButton(onClick = onConfirm, modifier = Modifier.testTag("confirm_dialog_accept")) {
                Text(confirmText)
            }
        },
        dismissButton = { WhereverTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
        modifier = modifier
    )
}
