package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.components.WhereverPrimaryButton
import com.wherever.mobile.feature.auth.data.RegistrationOutcome
import com.wherever.mobile.feature.auth.domain.AuthField
import com.wherever.mobile.feature.auth.ui.components.AuthErrorBanner
import com.wherever.mobile.feature.auth.ui.components.AuthFooterAction
import com.wherever.mobile.feature.auth.ui.components.AuthHeader
import com.wherever.mobile.feature.auth.ui.components.AuthTextField
import com.wherever.mobile.feature.auth.ui.components.messageResource

@Composable
fun RegisterScreen(
    onRegistered: (RegistrationOutcome) -> Unit,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is RegisterEvent.Success) onRegistered(event.outcome)
        }
    }
    RegisterContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onBirthDateChange = viewModel::onBirthDateChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordConfirmationChange = viewModel::onPasswordConfirmationChange,
        onTermsChange = viewModel::onTermsChange,
        onTogglePassword = viewModel::togglePasswordVisibility,
        onSubmit = viewModel::submit,
        onBack = onBack,
        onSignIn = onSignIn
    )
}

@Composable
fun RegisterContent(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onTermsChange: (Boolean) -> Unit,
    onTogglePassword: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AuthHeader(
            title = stringResource(R.string.auth_register_title),
            subtitle = stringResource(R.string.auth_register_subtitle),
            onBack = onBack
        )
        RegisterIdentityFields(
            state = state,
            onNameChange = onNameChange,
            onBirthDateChange = onBirthDateChange,
            onEmailChange = onEmailChange
        )
        RegisterPasswordFields(
            state = state,
            onPasswordChange = onPasswordChange,
            onPasswordConfirmationChange = onPasswordConfirmationChange,
            onTogglePassword = onTogglePassword
        )
        RegisterTerms(state = state, onTermsChange = onTermsChange)
        AuthErrorBanner(error = state.error)
        WhereverPrimaryButton(
            text = if (state.isLoading) {
                stringResource(R.string.auth_action_loading)
            } else {
                stringResource(R.string.auth_action_create_account)
            },
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_submit"),
            enabled = !state.isLoading
        )
        AuthFooterAction(
            text = stringResource(R.string.auth_have_account),
            actionText = stringResource(R.string.auth_action_sign_in),
            onClick = onSignIn
        )
    }
}

@Composable
private fun RegisterIdentityFields(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onEmailChange: (String) -> Unit
) {
    AuthTextField(
        label = stringResource(R.string.auth_field_name),
        value = state.name,
        onValueChange = onNameChange,
        error = state.fieldErrors[AuthField.NAME],
        testTag = "register_name"
    )
    AuthTextField(
        label = stringResource(R.string.auth_field_birth_date),
        value = state.birthDate,
        onValueChange = onBirthDateChange,
        error = state.fieldErrors[AuthField.BIRTH_DATE],
        testTag = "register_birth_date"
    )
    AuthTextField(
        label = stringResource(R.string.auth_field_email),
        value = state.email,
        onValueChange = onEmailChange,
        keyboardType = KeyboardType.Email,
        error = state.fieldErrors[AuthField.EMAIL],
        testTag = "register_email"
    )
}

@Composable
private fun RegisterPasswordFields(
    state: RegisterUiState,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onTogglePassword: () -> Unit
) {
    AuthTextField(
        label = stringResource(R.string.auth_field_password),
        value = state.password,
        onValueChange = onPasswordChange,
        keyboardType = KeyboardType.Password,
        isPassword = true,
        passwordVisible = state.passwordVisible,
        onTogglePassword = onTogglePassword,
        error = state.fieldErrors[AuthField.PASSWORD],
        testTag = "register_password"
    )
    AuthTextField(
        label = stringResource(R.string.auth_field_password_confirmation),
        value = state.passwordConfirmation,
        onValueChange = onPasswordConfirmationChange,
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done,
        isPassword = true,
        passwordVisible = state.passwordVisible,
        onTogglePassword = onTogglePassword,
        error = state.fieldErrors[AuthField.PASSWORD_CONFIRMATION],
        testTag = "register_password_confirmation"
    )
}

@Composable
private fun RegisterTerms(state: RegisterUiState, onTermsChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Checkbox(
            checked = state.acceptedTerms,
            onCheckedChange = onTermsChange,
            modifier = Modifier.testTag("register_terms")
        )
        Text(
            text = stringResource(R.string.auth_terms_acceptance),
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
    state.fieldErrors[AuthField.TERMS]?.let { error ->
        Text(
            text = stringResource(error.messageResource()),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
