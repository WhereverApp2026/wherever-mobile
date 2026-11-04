package com.wherever.mobile.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InitialAvatar
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.NavRow
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.profile.domain.CitySuggestion

/** 82_EditarPerfil y 84_DatosPers en una sola pantalla (duda D09). */
@Composable
fun EditProfileRoute(
    onBack: () -> Unit,
    onTastes: () -> Unit,
    onPrivacy: () -> Unit,
    onDeleteAccount: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.saved.collect { onBack() } }
    if (state.loading) {
        LoadingState()
    } else {
        EditProfileContent(
            state = state,
            onNameChange = viewModel::onNameChange,
            onCityQuery = viewModel::onCityQueryChange,
            onCitySelect = viewModel::onCitySelect,
            onSave = viewModel::save,
            onCancel = onBack,
            onTastes = onTastes,
            onPrivacy = onPrivacy,
            onDeleteAccount = onDeleteAccount
        )
    }
}

@Composable
private fun EditProfileContent(
    state: EditProfileUiState,
    onNameChange: (String) -> Unit,
    onCityQuery: (String) -> Unit,
    onCitySelect: (CitySuggestion) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onTastes: () -> Unit,
    onPrivacy: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.common_save),
                onClick = onSave,
                enabled = state.canSave,
                loading = state.saving,
                tag = "edit_profile_save"
            )
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WhereverTextLink(stringResource(R.string.common_cancel), onCancel)
            Text(text = stringResource(R.string.edit_profile_title), style = MaterialTheme.typography.titleLarge)
            // Mantiene el título centrado, como en la maqueta.
            Box(Modifier)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            InitialAvatar(state.name.ifBlank { state.originalName }, size = 88.dp)
        }
        LabeledTextField(
            label = stringResource(R.string.field_name),
            value = state.name,
            onValueChange = onNameChange,
            leadingIcon = Icons.Filled.Person,
            error = state.nameError?.asString(),
            tag = "edit_profile_name"
        )
        LabeledTextField(
            label = stringResource(R.string.field_email),
            value = state.email,
            onValueChange = {},
            readOnly = true,
            leadingIcon = Icons.Filled.Email,
            trailingContent = if (state.emailVerified) {
                { StatusPill(stringResource(R.string.edit_profile_verified)) }
            } else {
                null
            },
            tag = "edit_profile_email"
        )
        CityField(state.city, onCityQuery, onCitySelect, tag = "edit_profile_city")
        state.error?.let { ErrorBanner(it.asString()) }
        SurfaceCard {
            NavRow(stringResource(R.string.edit_profile_tastes), onTastes)
            NavRow(stringResource(R.string.privacy_title), onPrivacy)
            NavRow(
                title = stringResource(R.string.delete_account_action),
                onClick = onDeleteAccount,
                titleColor = MaterialTheme.colorScheme.error,
                tag = "edit_profile_delete"
            )
        }
    }
}

@Preview
@Composable
private fun EditProfilePreview() {
    WhereverTheme(darkTheme = false) {
        EditProfileContent(
            EditProfileUiState(loading = false, name = "Ana López", email = "ana@ejemplo.com", emailVerified = true),
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            {}
        )
    }
}
