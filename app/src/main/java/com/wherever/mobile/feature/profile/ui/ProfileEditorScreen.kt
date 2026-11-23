package com.wherever.mobile.feature.profile.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.SecureScreen
import com.wherever.mobile.feature.tastes.ui.tasteLabelRes

/** 82_EditarPerfil (Figma `298:8875`). */
@Composable
fun ProfileEditorRoute(
    onBack: () -> Unit,
    onTastes: () -> Unit,
    onPrivacy: () -> Unit,
    viewModel: ProfileEditorViewModel = hiltViewModel(),
    photoViewModel: ProfilePhotoViewModel = hiltViewModel()
) {
    SecureScreen()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val photoState by photoViewModel.state.collectAsStateWithLifecycle()
    val photoActions = rememberPhotoActions(photoViewModel)
    LaunchedEffect(viewModel) { viewModel.done.collect { onBack() } }
    if (state.loading) {
        LoadingState()
        return
    }
    ProfileEditorContent(
        state = state,
        actions = ProfileEditorActions(
            onCancel = onBack,
            onDone = viewModel::save,
            onName = viewModel::onName,
            onUsername = viewModel::onUsername,
            onBio = viewModel::onBio,
            onTastes = onTastes,
            onPrivacy = onPrivacy
        ),
        avatar = { EditableProfileAvatar(state.name, state.photoUrl, photoState, photoActions) },
        onChangePhoto = photoActions.onOpen
    )
}

@Composable
internal fun ProfileEditorContent(
    state: ProfileEditorUiState,
    actions: ProfileEditorActions,
    avatar: @Composable () -> Unit = { ProfileAvatar(state.name, state.photoUrl, Sizes.AvatarL) },
    onChangePhoto: () -> Unit = {}
) {
    FormScreen {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            WhereverTextLink(
                stringResource(R.string.common_cancel),
                actions.onCancel,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.edit_profile_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Box(modifier = Modifier.testTag("profile_editor_done")) {
                WhereverTextLink(stringResource(R.string.profile_editor_done), actions.onDone, enabled = !state.saving)
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.S),
            modifier = Modifier.fillMaxWidth()
        ) {
            avatar()
            WhereverTextLink(stringResource(R.string.photo_change), onChangePhoto)
        }
        LabeledTextField(
            label = stringResource(R.string.field_name),
            value = state.name,
            onValueChange = actions.onName,
            error = state.nameError?.asString(),
            tag = "profile_editor_name"
        )
        LabeledTextField(
            label = stringResource(R.string.profile_editor_username),
            value = state.username,
            onValueChange = actions.onUsername,
            prefix = "@",
            error = state.usernameError?.asString(),
            tag = "profile_editor_username"
        )
        LabeledTextField(
            label = stringResource(R.string.profile_editor_bio),
            value = state.bio,
            onValueChange = actions.onBio,
            placeholder = stringResource(R.string.profile_editor_bio_hint),
            supporting = if (state.bioError == null) {
                stringResource(R.string.group_detail_name_count, state.bio.length, MAX_BIO)
            } else {
                null
            },
            error = state.bioError?.asString(),
            tag = "profile_editor_bio"
        )
        Text(
            stringResource(R.string.edit_profile_tastes),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (state.tastes.isEmpty()) {
            WhereverTextLink(stringResource(R.string.profile_editor_add_tastes), actions.onTastes)
        } else {
            ChipGroup(modifier = Modifier.testTag("profile_editor_tastes")) {
                state.tastes.forEach { key ->
                    SelectChip(text = tasteText(key), selected = true, onClick = actions.onTastes)
                }
            }
        }
        state.error?.let { ErrorBanner(it.asString()) }
        SurfaceCard(modifier = Modifier.testTag("profile_editor_privacy")) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.M),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = actions.onPrivacy)
                    .padding(Spacing.L)
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.profile_editor_privacy), style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.profile_editor_privacy_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Etiqueta de un gusto; una clave desconocida se muestra legible (pan_dulce → Pan dulce). */
@Composable
private fun tasteText(key: String): String =
    tasteLabelRes(key)?.let { stringResource(it) } ?: key.replace('_', ' ').replaceFirstChar { it.uppercase() }

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ProfileEditorPreview() {
    WhereverTheme {
        ProfileEditorContent(
            ProfileEditorUiState(
                loading = false,
                name = "Osmar Reséndiz",
                username = "osmar",
                bio = "Cafés, tacos y planes sin prisa.",
                tastes = listOf("cafeterias", "tacos", "aire_libre")
            ),
            ProfileEditorActions()
        )
    }
}
