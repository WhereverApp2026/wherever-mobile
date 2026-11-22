package com.wherever.mobile.feature.memories.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.memories.domain.POSTCARD_TEXT_MAX
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import java.io.File

/** 72_Postal (specs/nv-31): estilo, texto, Guardar en mi álbum y Compartir postal. */
@Composable
fun PostcardEditorRoute(onBack: () -> Unit, onSaved: () -> Unit, viewModel: PostcardEditorViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val greeting = stringResource(R.string.memories_greeting, state.placeName)
    LaunchedEffect(greeting) { viewModel.onDefaultTitle(greeting) }
    LaunchedEffect(viewModel) {
        viewModel.saved.collect { result ->
            if (result is PostcardSaved.Share) context.sharePostcard(result.fileUri)
            onSaved()
        }
    }
    PostcardEditorContent(
        state,
        PostcardEditorActions(onBack, viewModel::onStyle, viewModel::onText, viewModel::save, viewModel::share)
    )
}

/** Abre la hoja del sistema con la imagen dibujada (vía el `FileProvider` de `cache/fotos/`). */
private fun Context.sharePostcard(fileUri: String) {
    val file = fileUri.toUri().path?.let(::File) ?: return
    val shared = FileProvider.getUriForFile(this, "$packageName.fotos", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, shared)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        startActivity(
            Intent.createChooser(send, getString(R.string.memories_share)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        // Sin apps para compartir: la postal ya quedó en el álbum.
    }
}

@Composable
internal fun PostcardEditorContent(state: PostcardEditorUiState, actions: PostcardEditorActions) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.memories_save),
                onClick = actions.onSave,
                enabled = state.canSave,
                loading = state.saving,
                tag = "postcard_save"
            )
            WhereverOutlinedButton(
                text = stringResource(R.string.memories_share),
                onClick = actions.onShare,
                enabled = state.canSave,
                icon = Icons.Outlined.Share,
                modifier = Modifier.fillMaxWidth(),
                tag = "postcard_share"
            )
        }
    ) {
        TopTitleRow(stringResource(R.string.memories_editor_title), actions.onBack)
        PostcardPreview(
            photo = state.source.ifBlank { null },
            style = state.style,
            title = state.text.ifBlank { stringResource(R.string.memories_greeting, state.placeName) },
            subtitle = listOf(state.placeName, memoryDate(state.date)).filter { it.isNotBlank() }.joinToString(" · ")
        )
        Text(
            stringResource(R.string.memories_style),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        ChipGroup {
            PostcardStyle.entries.forEach { style ->
                SelectChip(
                    stringResource(style.label()),
                    state.style == style,
                    { actions.onStyle(style) },
                    tag = "postcard_style_${style.apiKey}"
                )
            }
        }
        LabeledTextField(
            label = stringResource(R.string.memories_text_label),
            value = state.text,
            onValueChange = actions.onText,
            placeholder = stringResource(R.string.memories_text_placeholder),
            supporting = stringResource(R.string.common_char_count, state.text.length, POSTCARD_TEXT_MAX),
            error = state.textError?.asString(),
            tag = "postcard_text"
        )
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PostcardEditorPreview() {
    WhereverTheme {
        PostcardEditorContent(
            PostcardEditorUiState(source = "file:///postal.jpg", placeName = "Café Raíz", date = "2026-10-11"),
            PostcardEditorActions({}, {}, {}, {}, {})
        )
    }
}
