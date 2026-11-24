package com.wherever.mobile.feature.profile.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.PhaseCrossfade
import com.wherever.mobile.core.designsystem.ScreenPhase
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.SecureScreen

/** 84_DatosPers (Figma `298:7692`): nombre, correo, ciudad, foto y eliminar la cuenta. */
@Composable
fun EditProfileRoute(
    onBack: () -> Unit,
    onDeleteAccount: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
    photoViewModel: ProfilePhotoViewModel = hiltViewModel()
) {
    SecureScreen()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val photoState by photoViewModel.state.collectAsStateWithLifecycle()
    val photoActions = rememberPhotoActions(photoViewModel)
    LaunchedEffect(viewModel) { viewModel.saved.collect { onBack() } }
    PhaseCrossfade(if (state.loading) ScreenPhase.LOADING else ScreenPhase.CONTENT) { phase ->
        if (phase == ScreenPhase.LOADING) {
            LoadingState()
        } else {
            EditProfileContent(
                state = state,
                actions = PersonalDataActions(
                    onNameChange = viewModel::onNameChange,
                    onCityQuery = viewModel::onCityQueryChange,
                    onCitySelect = viewModel::onCitySelect,
                    onSave = viewModel::save,
                    onBack = onBack,
                    onDeleteAccount = onDeleteAccount
                ),
                avatar = {
                    EditableProfileAvatar(
                        state.name.ifBlank { state.originalName },
                        state.photoUrl,
                        photoState,
                        photoActions
                    )
                },
                onChangePhoto = photoActions.onOpen
            )
        }
    }
}

@Composable
internal fun EditProfileContent(
    state: EditProfileUiState,
    actions: PersonalDataActions,
    avatar: @Composable () -> Unit = {
        ProfileAvatar(state.name.ifBlank { state.originalName }, state.photoUrl, size = Sizes.AvatarL)
    },
    onChangePhoto: () -> Unit = {}
) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.personal_data_save),
                onClick = actions.onSave,
                enabled = state.canSave,
                loading = state.saving,
                tag = "edit_profile_save"
            )
        }
    ) {
        TopTitleRow(stringResource(R.string.personal_data_title), actions.onBack)
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
            onValueChange = actions.onNameChange,
            leadingIcon = Icons.Outlined.Person,
            error = state.nameError?.asString(),
            tag = "edit_profile_name"
        )
        LabeledTextField(
            label = stringResource(R.string.field_email),
            value = state.email,
            onValueChange = {},
            readOnly = true,
            leadingIcon = Icons.Outlined.Email,
            trailingContent = if (state.emailVerified) {
                { StatusPill(stringResource(R.string.edit_profile_verified)) }
            } else {
                null
            },
            tag = "edit_profile_email"
        )
        CityField(state.city, actions.onCityQuery, actions.onCitySelect, tag = "edit_profile_city")
        state.error?.let { ErrorBanner(it.asString()) }
        WhereverTextLink(
            stringResource(R.string.delete_account_action),
            actions.onDeleteAccount,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun EditProfilePreview() {
    WhereverTheme {
        EditProfileContent(
            EditProfileUiState(loading = false, name = "Ana López", email = "ana@ejemplo.com", emailVerified = true),
            PersonalDataActions()
        )
    }
}
